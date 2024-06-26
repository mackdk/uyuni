#!/usr/bin/python
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

try:
    #  python 2
    import xmlrpclib
except ImportError:
    #  python3
    import xmlrpc.client as xmlrpclib
import unittest

SERVER = "scripts.back-webqa.redhat.com"


# pylint: disable-next=missing-class-docstring
class ErrataImportTests(unittest.TestCase):
    def testUtf8Erratum(self):
        # pylint: disable-next=global-variable-not-assigned
        global SERVER

        # To actually make this one repeatable, need to munge the
        # erratum_hash['advisory_name'] or erratum_hash['revision'] fields...

        erratum_hash = {
            "advisory_name": "RHSA-2007:0008",
            "bugs": [
                {
                    "id": 218055,
                    "status": "ON_QA",
                    "summary": "CVE-2006-6107 D-Bus denial of service",
                }
            ],
            "crossref": "",
            "cve": "CVE-2006-6107",
            "description": "D-BUS is a system for sending messages between applications. It is used\r\nboth for the systemwide message bus service, and as a\r\nper-user-login-session messaging facility.\r\n\r\nKimmo H\xc3\xa4m\xc3\xa4l\xc3\xa4inen discovered a flaw in the way D-BUS processes certain\r\nmessages. It is possible for a local unprivileged D-BUS process to disrupt\r\nthe ability of another D-BUS process to receive messages. (CVE-2006-6107)\r\n\r\nUsers of dbus are advised to upgrade to these updated packages, which\r\ncontain backported patches to correct this issue.",
            "errata_files": [
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/SRPMS/dbus-0.22-12.EL.8.src.rpm",
                    "checksum_type": "md5",
                    "checksum": "379fdd3f9afb34124fa9b88deb440e3f",
                    "rhn_channel": ["rh-x86_64-desktop-4", "rh-i386-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/x86_64/dbus-glib-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "8c41138bbf9127bbb2d799f566ce3a8a",
                    "rhn_channel": ["rh-x86_64-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/x86_64/dbus-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "ac83105ce8b120ec537a3ea54da1e37d",
                    "rhn_channel": ["rh-x86_64-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/x86_64/dbus-python-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "920cf9a273c521118e374230690a3df6",
                    "rhn_channel": ["rh-x86_64-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/x86_64/dbus-x11-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "847b2400eee82a36e3542b2f4f2d4947",
                    "rhn_channel": ["rh-x86_64-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/x86_64/dbus-devel-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "1b248af405670382e31b06c4fa52fa36",
                    "rhn_channel": ["rh-x86_64-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/x86_64/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rh-x86_64-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/x86_64/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rh-x86_64-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/i386/dbus-devel-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "b8a46001a416b2e36f5da1e6868c91ec",
                    "rhn_channel": ["rh-i386-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/i386/dbus-python-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "8e5eadeb5be39e139885336011551656",
                    "rhn_channel": ["rh-i386-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/i386/dbus-x11-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "2f9d064981b12a7f4cb8cf74d6142de5",
                    "rhn_channel": ["rh-i386-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/i386/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rh-i386-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4Desktop/en/os/i386/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rh-i386-desktop-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/SRPMS/dbus-0.22-12.EL.8.src.rpm",
                    "checksum_type": "md5",
                    "checksum": "379fdd3f9afb34124fa9b88deb440e3f",
                    "rhn_channel": [
                        "rhel-x86_64-ws-4",
                        "rhel-i386-ws-4",
                        "rhel-ia64-ws-4",
                    ],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/x86_64/dbus-glib-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "8c41138bbf9127bbb2d799f566ce3a8a",
                    "rhn_channel": ["rhel-x86_64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/x86_64/dbus-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "ac83105ce8b120ec537a3ea54da1e37d",
                    "rhn_channel": ["rhel-x86_64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/x86_64/dbus-python-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "920cf9a273c521118e374230690a3df6",
                    "rhn_channel": ["rhel-x86_64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/x86_64/dbus-x11-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "847b2400eee82a36e3542b2f4f2d4947",
                    "rhn_channel": ["rhel-x86_64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/x86_64/dbus-devel-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "1b248af405670382e31b06c4fa52fa36",
                    "rhn_channel": ["rhel-x86_64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/x86_64/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-x86_64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/x86_64/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-x86_64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/ia64/dbus-glib-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "b07996f3ebf2331958a1adfd230302cc",
                    "rhn_channel": ["rhel-ia64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/ia64/dbus-python-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "c7406fea694e12487aa8213142ed66ea",
                    "rhn_channel": ["rhel-ia64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/ia64/dbus-x11-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "7097ef62d6917170005f000a14a54fe7",
                    "rhn_channel": ["rhel-ia64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/ia64/dbus-devel-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "a294a24161855aa73d4a9d83e4f3a107",
                    "rhn_channel": ["rhel-ia64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/ia64/dbus-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "dd584d93cd98e8ebc3331e5c5d938b87",
                    "rhn_channel": ["rhel-ia64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/ia64/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-ia64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/ia64/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-ia64-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/i386/dbus-devel-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "b8a46001a416b2e36f5da1e6868c91ec",
                    "rhn_channel": ["rhel-i386-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/i386/dbus-python-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "8e5eadeb5be39e139885336011551656",
                    "rhn_channel": ["rhel-i386-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/i386/dbus-x11-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "2f9d064981b12a7f4cb8cf74d6142de5",
                    "rhn_channel": ["rhel-i386-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/i386/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-i386-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4WS/en/os/i386/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-i386-ws-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/SRPMS/dbus-0.22-12.EL.8.src.rpm",
                    "checksum_type": "md5",
                    "checksum": "379fdd3f9afb34124fa9b88deb440e3f",
                    "rhn_channel": [
                        "rhel-x86_64-es-4",
                        "rhel-i386-es-4",
                        "rhel-ia64-es-4",
                    ],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/x86_64/dbus-glib-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "8c41138bbf9127bbb2d799f566ce3a8a",
                    "rhn_channel": ["rhel-x86_64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/x86_64/dbus-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "ac83105ce8b120ec537a3ea54da1e37d",
                    "rhn_channel": ["rhel-x86_64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/x86_64/dbus-python-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "920cf9a273c521118e374230690a3df6",
                    "rhn_channel": ["rhel-x86_64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/x86_64/dbus-x11-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "847b2400eee82a36e3542b2f4f2d4947",
                    "rhn_channel": ["rhel-x86_64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/x86_64/dbus-devel-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "1b248af405670382e31b06c4fa52fa36",
                    "rhn_channel": ["rhel-x86_64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/x86_64/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-x86_64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/x86_64/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-x86_64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/ia64/dbus-glib-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "b07996f3ebf2331958a1adfd230302cc",
                    "rhn_channel": ["rhel-ia64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/ia64/dbus-python-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "c7406fea694e12487aa8213142ed66ea",
                    "rhn_channel": ["rhel-ia64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/ia64/dbus-x11-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "7097ef62d6917170005f000a14a54fe7",
                    "rhn_channel": ["rhel-ia64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/ia64/dbus-devel-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "a294a24161855aa73d4a9d83e4f3a107",
                    "rhn_channel": ["rhel-ia64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/ia64/dbus-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "dd584d93cd98e8ebc3331e5c5d938b87",
                    "rhn_channel": ["rhel-ia64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/ia64/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-ia64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/ia64/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-ia64-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/i386/dbus-devel-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "b8a46001a416b2e36f5da1e6868c91ec",
                    "rhn_channel": ["rhel-i386-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/i386/dbus-python-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "8e5eadeb5be39e139885336011551656",
                    "rhn_channel": ["rhel-i386-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/i386/dbus-x11-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "2f9d064981b12a7f4cb8cf74d6142de5",
                    "rhn_channel": ["rhel-i386-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/i386/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-i386-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4ES/en/os/i386/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-i386-es-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/SRPMS/dbus-0.22-12.EL.8.src.rpm",
                    "checksum_type": "md5",
                    "checksum": "379fdd3f9afb34124fa9b88deb440e3f",
                    "rhn_channel": [
                        "rhel-i386-as-4",
                        "rhel-ia64-as-4",
                        "rhel-ppc-as-4",
                        "rhel-s390x-as-4",
                        "rhel-s390-as-4",
                        "rhel-x86_64-as-4",
                    ],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ppc/dbus-devel-0.22-12.EL.8.ppc.rpm",
                    "checksum_type": "md5",
                    "checksum": "d4adf9454e5303fdcaab8c43805a212c",
                    "rhn_channel": ["rhel-ppc-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ppc/dbus-0.22-12.EL.8.ppc.rpm",
                    "checksum_type": "md5",
                    "checksum": "acaed9d78ce157ef8b15e19692c832c1",
                    "rhn_channel": ["rhel-ppc-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ppc/dbus-python-0.22-12.EL.8.ppc.rpm",
                    "checksum_type": "md5",
                    "checksum": "60c70fee76a3a98c6cf46629901b2ed3",
                    "rhn_channel": ["rhel-ppc-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ppc/dbus-glib-0.22-12.EL.8.ppc.rpm",
                    "checksum_type": "md5",
                    "checksum": "1a66a5a36be6167dff2558866ab34d9c",
                    "rhn_channel": ["rhel-ppc-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ppc/dbus-x11-0.22-12.EL.8.ppc.rpm",
                    "checksum_type": "md5",
                    "checksum": "02a34c40ade9386f829e0bbf12dc8036",
                    "rhn_channel": ["rhel-ppc-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ppc/dbus-0.22-12.EL.8.ppc64.rpm",
                    "checksum_type": "md5",
                    "checksum": "87db84625d2e27f3b0c168e2f1e34a18",
                    "rhn_channel": ["rhel-ppc-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ppc/dbus-glib-0.22-12.EL.8.ppc64.rpm",
                    "checksum_type": "md5",
                    "checksum": "e28bef04fa98091747deef3b121fec18",
                    "rhn_channel": ["rhel-ppc-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390x/dbus-python-0.22-12.EL.8.s390x.rpm",
                    "checksum_type": "md5",
                    "checksum": "6afc6054de436384a71951c4ca7c1083",
                    "rhn_channel": ["rhel-s390x-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390x/dbus-glib-0.22-12.EL.8.s390x.rpm",
                    "checksum_type": "md5",
                    "checksum": "5608a1394e595ee7560bc2080b54524e",
                    "rhn_channel": ["rhel-s390x-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390x/dbus-0.22-12.EL.8.s390x.rpm",
                    "checksum_type": "md5",
                    "checksum": "38a9c1c9838f1fc0ffe7e8c62259a4e9",
                    "rhn_channel": ["rhel-s390x-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390x/dbus-devel-0.22-12.EL.8.s390x.rpm",
                    "checksum_type": "md5",
                    "checksum": "d17fd60137f8fc012826cb5c2fb1c798",
                    "rhn_channel": ["rhel-s390x-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390x/dbus-x11-0.22-12.EL.8.s390x.rpm",
                    "checksum_type": "md5",
                    "checksum": "aa63335eff72a01edf6c3c8709257100",
                    "rhn_channel": ["rhel-s390x-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390x/dbus-0.22-12.EL.8.s390.rpm",
                    "checksum_type": "md5",
                    "checksum": "2aec70890676846f00be1fd5ed9f4a9c",
                    "rhn_channel": ["rhel-s390x-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390x/dbus-glib-0.22-12.EL.8.s390.rpm",
                    "checksum_type": "md5",
                    "checksum": "79ebed9e812ce4760fcbd4bb7fa8efb7",
                    "rhn_channel": ["rhel-s390x-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/x86_64/dbus-glib-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "8c41138bbf9127bbb2d799f566ce3a8a",
                    "rhn_channel": ["rhel-x86_64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/x86_64/dbus-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "ac83105ce8b120ec537a3ea54da1e37d",
                    "rhn_channel": ["rhel-x86_64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/x86_64/dbus-python-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "920cf9a273c521118e374230690a3df6",
                    "rhn_channel": ["rhel-x86_64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/x86_64/dbus-x11-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "847b2400eee82a36e3542b2f4f2d4947",
                    "rhn_channel": ["rhel-x86_64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/x86_64/dbus-devel-0.22-12.EL.8.x86_64.rpm",
                    "checksum_type": "md5",
                    "checksum": "1b248af405670382e31b06c4fa52fa36",
                    "rhn_channel": ["rhel-x86_64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/x86_64/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-x86_64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/x86_64/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-x86_64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390/dbus-python-0.22-12.EL.8.s390.rpm",
                    "checksum_type": "md5",
                    "checksum": "f1be5d2e04c8e0698caddc9d0af40ab2",
                    "rhn_channel": ["rhel-s390-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390/dbus-x11-0.22-12.EL.8.s390.rpm",
                    "checksum_type": "md5",
                    "checksum": "11ca54506fedf365fab62e025d7b742b",
                    "rhn_channel": ["rhel-s390-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390/dbus-0.22-12.EL.8.s390.rpm",
                    "checksum_type": "md5",
                    "checksum": "2aec70890676846f00be1fd5ed9f4a9c",
                    "rhn_channel": ["rhel-s390-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390/dbus-glib-0.22-12.EL.8.s390.rpm",
                    "checksum_type": "md5",
                    "checksum": "79ebed9e812ce4760fcbd4bb7fa8efb7",
                    "rhn_channel": ["rhel-s390-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/s390/dbus-devel-0.22-12.EL.8.s390.rpm",
                    "checksum_type": "md5",
                    "checksum": "ba507082ec7e13a57cbf9d2addf18e9d",
                    "rhn_channel": ["rhel-s390-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ia64/dbus-glib-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "b07996f3ebf2331958a1adfd230302cc",
                    "rhn_channel": ["rhel-ia64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ia64/dbus-python-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "c7406fea694e12487aa8213142ed66ea",
                    "rhn_channel": ["rhel-ia64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ia64/dbus-x11-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "7097ef62d6917170005f000a14a54fe7",
                    "rhn_channel": ["rhel-ia64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ia64/dbus-devel-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "a294a24161855aa73d4a9d83e4f3a107",
                    "rhn_channel": ["rhel-ia64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ia64/dbus-0.22-12.EL.8.ia64.rpm",
                    "checksum_type": "md5",
                    "checksum": "dd584d93cd98e8ebc3331e5c5d938b87",
                    "rhn_channel": ["rhel-ia64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ia64/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-ia64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/ia64/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-ia64-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/i386/dbus-devel-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "b8a46001a416b2e36f5da1e6868c91ec",
                    "rhn_channel": ["rhel-i386-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/i386/dbus-python-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "8e5eadeb5be39e139885336011551656",
                    "rhn_channel": ["rhel-i386-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/i386/dbus-x11-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "2f9d064981b12a7f4cb8cf74d6142de5",
                    "rhn_channel": ["rhel-i386-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/i386/dbus-glib-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "5ba2eefce27c72524c7c5cdb1b6e2224",
                    "rhn_channel": ["rhel-i386-as-4"],
                },
                {
                    "ftppath": "/ftp/pub/redhat/linux/updates/enterprise/4AS/en/os/i386/dbus-0.22-12.EL.8.i386.rpm",
                    "checksum_type": "md5",
                    "checksum": "e81002d1ca5787e89458cd7d5bb04dd5",
                    "rhn_channel": ["rhel-i386-as-4"],
                },
            ],
            "errata_type": "RHSA",
            "erratum_deployed_by": "jorris@redhat.com",
            "issue_date": "2007-02-07",
            "keywords": "",
            "multilib": "",
            "obsoletes": "",
            "oval": '<?xml version="1.0" encoding="UTF-8"?>\n\n<oval_definitions xmlns="http://oval.mitre.org/XMLSchema/oval-definitions-5" xmlns:oval="http://oval.mitre.org/XMLSchema/oval-common-5" xmlns:oval-def="http://oval.mitre.org/XMLSchema/oval-definitions-5" xmlns:unix-def="http://oval.mitre.org/XMLSchema/oval-definitions-5#unix" xmlns:red-def="http://oval.mitre.org/XMLSchema/oval-definitions-5#linux" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://oval.mitre.org/XMLSchema/oval-common-5 oval-common-schema.xsd http://oval.mitre.org/XMLSchema/oval-definitions-5 oval-definitions-schema.xsd http://oval.mitre.org/XMLSchema/oval-definitions-5#unix unix-definitions-schema.xsd http://oval.mitre.org/XMLSchema/oval-definitions-5#linux linux-definitions-schema.xsd">\n  <generator>\n    <oval:product_name>Red Hat Errata System</oval:product_name>\n    <oval:schema_version>5.2</oval:schema_version>\n    <oval:timestamp>2007-02-07T12:21:43</oval:timestamp>\n  </generator>\n\n  <definitions>\n    <definition id="oval:com.redhat.rhsa:def:20070008" version="201" class="patch">\n      <metadata>\n        <title>RHSA-2007:0008: dbus security update (Moderate)\n    </title>\n    <affected family="unix">\n            <platform>Red Hat Enterprise Linux 4</platform>\n             </affected>\n        <reference source="RHSA" ref_id="RHSA-2007:0008-00" ref_url="https://rhn.redhat.com/errata/RHSA-2007-0008.html"/>\n    <description>D-BUS is a system for sending messages between applications. It is used\r\nboth for the systemwide message bus service, and as a\r\nper-user-login-session messaging facility.\r\n\r\nKimmo H\xc3\xa4m\xc3\xa4l\xc3\xa4inen discovered a flaw in the way D-BUS processes certain\r\nmessages. It is possible for a local unprivileged D-BUS process to disrupt\r\nthe ability of another D-BUS process to receive messages. (CVE-2006-6107)\r\n\r\nUsers of dbus are advised to upgrade to these updated packages, which\r\ncontain backported patches to correct this issue.</description>\n\n<!-- ~~~~~~~~~~~~~~~~~~~~   advisory details   ~~~~~~~~~~~~~~~~~~~ -->\n\n<advisory from="secalert@redhat.com">\n\n        <severity>Moderate</severity>\n\n        <rights>Copyright 2007 Red Hat, Inc.</rights>\n        <issued date="2007-02-07"/>\n        <updated date="2007-02-07"/>\n            <cve href="http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2006-6107">CVE-2006-6107</cve>\n                <bugzilla href="http://bugzilla.redhat.com/218055" id="218055">CVE-2006-6107 D-Bus denial of service</bugzilla>\n    </advisory>\n      </metadata>\n      <criteria operator="AND">\n <criterion test_ref="oval:com.redhat.rhsa:tst:20070008001" comment="Red Hat Enterprise Linux 4 is installed" />\n <criteria operator="OR">\n  \n <criteria operator="AND">\n <criterion test_ref="oval:com.redhat.rhsa:tst:20070008010" comment="dbus-x11 is earlier than 0:0.22-12.EL.8" /><criterion test_ref="oval:com.redhat.rhsa:tst:20070008011" comment="dbus-x11 is signed with Red Hat master key" />\n  \n</criteria><criteria operator="AND">\n <criterion test_ref="oval:com.redhat.rhsa:tst:20070008008" comment="dbus-python is earlier than 0:0.22-12.EL.8" /><criterion test_ref="oval:com.redhat.rhsa:tst:20070008009" comment="dbus-python is signed with Red Hat master key" />\n  \n</criteria><criteria operator="AND">\n <criterion test_ref="oval:com.redhat.rhsa:tst:20070008006" comment="dbus-devel is earlier than 0:0.22-12.EL.8" /><criterion test_ref="oval:com.redhat.rhsa:tst:20070008007" comment="dbus-devel is signed with Red Hat master key" />\n  \n</criteria><criteria operator="AND">\n <criterion test_ref="oval:com.redhat.rhsa:tst:20070008002" comment="dbus is earlier than 0:0.22-12.EL.8" /><criterion test_ref="oval:com.redhat.rhsa:tst:20070008003" comment="dbus is signed with Red Hat master key" />\n  \n</criteria><criteria operator="AND">\n <criterion test_ref="oval:com.redhat.rhsa:tst:20070008004" comment="dbus-glib is earlier than 0:0.22-12.EL.8" /><criterion test_ref="oval:com.redhat.rhsa:tst:20070008005" comment="dbus-glib is signed with Red Hat master key" />\n  \n</criteria>\n</criteria>\n</criteria>\n    </definition>\n  </definitions>\n  <tests>\n    <!-- ~~~~~~~~~~~~~~~~~~~~~   rpminfo tests   ~~~~~~~~~~~~~~~~~~~~~ -->\n    <rpminfo_test id="oval:com.redhat.rhsa:tst:20070008001"  version="201" comment="Red Hat Enterprise Linux 4 is installed" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008001" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008002" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008002"  version="201" comment="dbus is earlier than 0:0.22-12.EL.8" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008002" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008003" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008003"  version="201" comment="dbus is signed with Red Hat master key" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008002" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008001" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008004"  version="201" comment="dbus-glib is earlier than 0:0.22-12.EL.8" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008003" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008003" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008005"  version="201" comment="dbus-glib is signed with Red Hat master key" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008003" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008001" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008006"  version="201" comment="dbus-devel is earlier than 0:0.22-12.EL.8" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008004" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008003" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008007"  version="201" comment="dbus-devel is signed with Red Hat master key" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008004" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008001" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008008"  version="201" comment="dbus-python is earlier than 0:0.22-12.EL.8" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008005" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008003" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008009"  version="201" comment="dbus-python is signed with Red Hat master key" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008005" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008001" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008010"  version="201" comment="dbus-x11 is earlier than 0:0.22-12.EL.8" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008006" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008003" />\n</rpminfo_test><rpminfo_test id="oval:com.redhat.rhsa:tst:20070008011"  version="201" comment="dbus-x11 is signed with Red Hat master key" check="at least one">\n  <object object_ref="oval:com.redhat.rhsa:obj:20070008006" />\n  <state state_ref="oval:com.redhat.rhsa:ste:20070008001" />\n</rpminfo_test>\n  </tests>\n\n  <objects>\n    <!-- ~~~~~~~~~~~~~~~~~~~~   rpminfo objects   ~~~~~~~~~~~~~~~~~~~~ -->\n    <rpminfo_object id="oval:com.redhat.rhsa:obj:20070008006"  version="201">\n  <name>dbus-x11</name>\n</rpminfo_object><rpminfo_object id="oval:com.redhat.rhsa:obj:20070008005"  version="201">\n  <name>dbus-python</name>\n</rpminfo_object><rpminfo_object id="oval:com.redhat.rhsa:obj:20070008004"  version="201">\n  <name>dbus-devel</name>\n</rpminfo_object><rpminfo_object id="oval:com.redhat.rhsa:obj:20070008001"  version="201">\n  <name>redhat-release</name>\n</rpminfo_object><rpminfo_object id="oval:com.redhat.rhsa:obj:20070008002"  version="201">\n  <name>dbus</name>\n</rpminfo_object><rpminfo_object id="oval:com.redhat.rhsa:obj:20070008003"  version="201">\n  <name>dbus-glib</name>\n</rpminfo_object>\n  </objects>\n  <states>\n    <!-- ~~~~~~~~~~~~~~~~~~~~   rpminfo states   ~~~~~~~~~~~~~~~~~~~~~ -->\n    <rpminfo_state id="oval:com.redhat.rhsa:ste:20070008001"  version="201">\n  <signature_keyid  operation="equals">219180cddb42a60e</signature_keyid>\n</rpminfo_state><rpminfo_state id="oval:com.redhat.rhsa:ste:20070008002"  version="201">\n  <version  operation="pattern match">^4\\D</version>\n</rpminfo_state><rpminfo_state id="oval:com.redhat.rhsa:ste:20070008003"  version="201">\n  <evr datatype="evr_string" operation="less_than">0:0.22-12.EL.8</evr>\n</rpminfo_state>\n  </states>\n</oval_definitions>\n\n',
            "packages": "",
            "product": "Red Hat Enterprise Linux",
            "reference": "http://www.redhat.com/security/updates/classification/#moderate",
            "revision": 3,
            "security_impact": "Moderate",
            "solution": "Before applying this update, make sure all previously released errata\r\nrelevant to your system have been applied.\r\n\r\nThis update is available via Red Hat Satellite.  To use Red Hat Satellite,\r\nlaunch the Red Hat Update Agent with the following command:\r\n\r\nup2date\r\n\r\nThis will start an interactive process that will result in the appropriate\r\nRPMs being upgraded on your system.",
            "synopsis": "Moderate: dbus security update",
            "topic": "Updated dbus packages that fix a security issue are now available for Red\r\nHat Enterprise Linux 4.\r\n\r\nThis update has been rated as having moderate security impact by the Red\r\nHat Security Response Team.",
            "update_date": "2007-02-07",
        }

        url = "http://" + SERVER + "/BUGZILLA"
        s = xmlrpclib.Server(url)
        s.bugzilla_errata.submit_errata(erratum_hash)


unittest.main()
