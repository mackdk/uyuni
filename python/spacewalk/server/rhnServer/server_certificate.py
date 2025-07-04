#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2008--2016 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
#
# Classes and functions needed for handling certificates
# The only really exportable item is the Certificate class
#

import sys
import hashlib
import time
import secrets
import socket

try:
    #  python 2
    import xmlrpclib
except ImportError:
    #  python3
    import xmlrpc.client as xmlrpclib

from spacewalk.common.rhnLog import log_debug, log_error
from spacewalk.common.rhnException import rhnFault
from .server_lib import getServerSecret
from .server_lib import check_entitlement_by_machine_id


def gen_secret():
    """Generate a secret"""
    seed = repr(time.time())
    # pylint: disable-next=redefined-builtin
    sum = hashlib.new("sha256", seed.encode())
    # feed some random numbers
    # pylint: disable-next=unused-variable
    for k in range(1, secrets.SystemRandom().randint(5, 15)):
        sum.update(repr(secrets.SystemRandom().random()).encode())
    sum.update(socket.gethostname().encode())
    ret = sum.hexdigest()
    del sum
    return ret


class Checksum:
    """Functions for handling system_id strings"""

    def __init__(self, secret, *args, **kwargs):
        algo = "sha256"
        if "algo" in kwargs:
            algo = kwargs["algo"]
        self.sum = hashlib.new(algo, secret.encode())
        if len(args) > 0:
            self.feed(*args)

    def feed(self, arg):
        # sys.stderr.write("arg = %s, type = %s\n" % (arg, type(arg)))
        # pylint: disable-next=unidiomatic-typecheck
        if type(arg) == type(()) or type(arg) == type([]):
            for s in arg:
                self.sum.update(s.encode())
        else:
            # pylint: disable-next=unidiomatic-typecheck
            if type(arg) == type(0):
                arg = str(arg)
            self.sum.update(str(arg).encode())

    def __repr__(self):
        t = self.sum.hexdigest()
        return t

    __str__ = __repr__


class Certificate:
    """Main certificate class"""

    # pylint: disable-next=invalid-name
    CheckSumFields = [
        "username",
        "os_release",
        "operating_system",
        "architecture",
        "system_id",
        "type",
    ]

    def __init__(self):
        """init data
        normally we include in the attrs:
        username, os_release, os, arch, system_id and fields
        """
        self.attrs = {}
        for k in Certificate.CheckSumFields:
            self.attrs[k] = None
        self.__fields = []
        self.__secret = None
        self.__checksum = None

    def __getitem__(self, key):
        """function that make it look like a dictionary for easy access"""
        return self.attrs.get(key)

    def __setitem__(self, name, value):
        """function that make it look like a dictionary for easy access
        updates the values of the attributes list with new values
        """
        self.attrs[name] = value
        if name in Certificate.CheckSumFields:
            if name not in self.__fields:
                self.__fields.append(name)
        else:  # non essential, take None values as ""
            if value is None:
                self.attrs[name] = ""
        return 0

    def __repr__(self):
        """string format"""
        return (
            # pylint: disable-next=consider-using-f-string
            "<Certificate instance>: Attrs: %s, Fields: %s, Secret: %s, Checksum: %s"
            % (self.attrs, self.__fields, self.__secret, self.__checksum)
        )

    __str__ = __repr__

    def certificate(self):
        """convert to XML"""
        dump = self.attrs
        dump["checksum"] = self.__checksum
        dump["fields"] = self.__fields
        try:
            x = xmlrpclib.dumps((dump,))
        except TypeError:
            e = sys.exc_info()[1]
            # pylint: disable-next=consider-using-f-string
            log_error("Could not marshall certificate for %s" % dump)
            e.args = e.args + (
                dump,
            )  # Carry on the information for the exception reporting
            raise
        # pylint: disable-next=consider-using-f-string
        return '<?xml version="1.0"?>\n%s' % x

    def compute_checksum(self, secret, algo="sha256"):
        """Update the checksum"""
        log_debug(4, secret, self.attrs)
        csum = Checksum(secret, algo=algo)
        for f in self.__fields:
            csum.feed(self.attrs[f])
        # feed the fields list last
        csum.feed(self.__fields)
        return str(csum)

    def set_secret(self, secret):
        """set the secret of the entry and recompute the checksum"""
        log_debug(4, "secret", secret)
        self.__secret = secret
        self.__checksum = self.compute_checksum(secret)

    def reload(self, text):
        """load data from a text certificate passed on by a client"""
        log_debug(4)
        text_id = text.strip()
        if not text_id:
            return -1
        # Now decode this certificate
        try:
            sysid, junk = xmlrpclib.loads(str(text_id))
        # pylint: disable-next=bare-except
        except:
            return -1
        else:
            s = sysid[0]
            del junk
        if "system_id" not in s or "fields" not in s:
            # pylint: disable-next=consider-using-f-string
            log_error("Got certificate with missing entries: %s" % s)
            return -1
        # check the certificate some more
        for k in s["fields"]:
            if k not in s:
                log_error(
                    # pylint: disable-next=consider-using-f-string
                    "Certificate lists unknown %s as a checksum field" % k,
                    # pylint: disable-next=consider-using-f-string
                    "cert data: %s" % s,
                )
                return -1

        # clear out the state
        # pylint: disable-next=unnecessary-dunder-call
        self.__init__()

        # at this point we know the certificate is sane enough for the
        # following processing
        for k in list(s.keys()):
            if k == "fields":
                self.__fields = s[k]
                continue
            if k == "checksum":
                self.__checksum = s[k]
                continue
            self.attrs[k] = s[k]
        # okay, the certificate is now loaded
        return 0

    def __validate_checksum(self, secret):
        """compute the current checksum against a secret and check it against
        the current checksum
        """
        if len(self.__checksum) == 64:
            csum = self.compute_checksum(secret, algo="sha256")
        elif len(self.__checksum) == 32:
            csum = self.compute_checksum(secret, algo="md5")
        # pylint: disable-next=possibly-used-before-assignment
        if not csum == self.__checksum:
            # fail, current checksum does not match
            log_error(
                # pylint: disable-next=consider-using-f-string
                "Checksum check failed: %s != %s" % (csum, self.__checksum),
                # pylint: disable-next=consider-using-f-string
                "fields = %s" % str(self.__fields),
                # pylint: disable-next=consider-using-f-string
                "attrs = %s" % str(self.attrs),
            )
            return 0
        return 1

    def valid(self):
        log_debug(4)
        # check for anonymous

        if self.attrs.get("machine_id"):
            entitlements = check_entitlement_by_machine_id(self.attrs.get("machine_id"))
            log_debug(
                4,
                "found entitlements for machine_id",
                self.attrs.get("machine_id"),
                entitlements,
            )
            if "salt_entitled" in entitlements:
                raise rhnFault(
                    48,
                    """
    This system is already registered as a Salt Minion. If you want to register it as a traditional client
    please delete it first via the web UI or API and then register it using the traditional tools.
                """,
                )

        if (
            "type" in self.attrs
            and self.attrs["type"]
            and self.attrs["type"].upper() == "ANONYMOUS"
        ):
            raise rhnFault(
                28,
                """
            You need to re-register your system with SUSE Multi-Linux Manager.
            Previously you have chosen to skip the creation of a system profile
            with SUSE Multi-Linux Manager and this trial feature is no longer available now.
            """,
            )  # we don't support anonymous anymore
        # now we have a real server. Get its secret
        sid = self.attrs["system_id"]
        secret = getServerSecret(sid)
        if secret is None:
            # no secret, can't validate
            # pylint: disable-next=consider-using-f-string
            log_debug(1, "Server id %s not found in database" % sid)
            return 0
        return self.__validate_checksum(secret)
