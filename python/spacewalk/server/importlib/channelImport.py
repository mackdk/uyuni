#  pylint: disable=missing-module-docstring,invalid-name
#
# Copyright (c) 2008--2017 Red Hat, Inc.
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
# Channel import process
#

from .importLib import (
    Import,
    InvalidArchError,
    InvalidChannelError,
    InvalidChannelFamilyError,
    MissingParentChannelError,
)
from spacewalk.satellite_tools.syncLib import log


# pylint: disable-next=missing-class-docstring
class ChannelImport(Import):
    # pylint: disable-next=redefined-outer-name,redefined-outer-name
    def __init__(self, batch, backend):
        Import.__init__(self, batch, backend)
        self.arches = {}
        self.families = {}
        self.dists = {}
        self.will_commit = 1
        self.releases = {}
        self.channels = {}
        self.checksum_types = {}

    def preprocess(self):
        # Processes the batch to a form more suitable for database
        # operations
        for channel in self.batch:
            self.__processChannel(channel)

    def __processChannel(self, channel):
        # Processes a package
        arch = channel["channel_arch"]
        if arch not in self.arches:
            self.arches[arch] = None
        for family in channel["families"]:
            self.families[family["label"]] = None
        # Dists
        if "dists" in channel and channel["dists"] is not None:
            for dist in channel["dists"]:
                self.arches[dist["channel_arch"]] = None
        # Product Names
        if "release" in channel and channel["release"] is not None:
            for release in channel["release"]:
                self.arches[release["channel_arch"]] = None
        if "receiving_updates" not in channel or channel["receiving_updates"] is None:
            channel["receiving_updates"] = "N"
        # Yum repo checksum type
        if (
            channel["checksum_type"]
            and channel["checksum_type"] not in self.checksum_types
        ):
            self.checksum_types[channel["checksum_type"]] = None

        # bug #528227
        # Print a warning in case the sync would move the channel between orgs
        if "org_id" in channel and channel["org_id"]:
            org_id = self.backend.lookupChannelOrg(channel["label"])

            if org_id and int(channel["org_id"]) != org_id["org_id"]:
                log(
                    1,
                    # pylint: disable-next=consider-using-f-string
                    "WARNING: Channel %s is already present in orgid %s."
                    % (channel["label"], org_id["org_id"]),
                )
                log(
                    1,
                    # pylint: disable-next=consider-using-f-string
                    "         Running synchronization will move the channel to orgid %s."
                    % channel["org_id"],
                )
                log(1, "")

    def fix(self):
        self.backend.lookupChannelArches(self.arches)
        self.backend.lookupChannelFamilies(self.families)
        self.backend.lookupChecksumTypes(self.checksum_types)
        # Fix
        for channel in self.batch:
            self.__postprocessChannel(channel)

    def __postprocessChannel(self, channel):
        if channel.ignored:
            return
        arch = channel["channel_arch"]
        if self.arches[arch] is None:
            # Mark it as ignored
            channel.ignored = 1
            # pylint: disable-next=consider-using-f-string
            raise InvalidArchError(arch, "Unsupported channel arch %s" % arch)
        channel["channel_arch_id"] = self.arches[arch]
        if channel["checksum_type"]:
            channel["checksum_type_id"] = self.checksum_types[channel["checksum_type"]]
        else:
            channel["checksum_type_id"] = None

        if "product_name" in channel:
            channel["product_name_id"] = self.backend.lookupProductNames(
                channel["product_name"]
            )
        # pylint: disable-next=redefined-outer-name
        families = []
        for family in channel["families"]:
            # Link back the channel to families
            channel_family_id = self.families[family["label"]]

            if channel_family_id is None:
                # Still cant get the id, Unknown channel family
                raise InvalidChannelFamilyError(family["label"])

            families.append({"channel_family_id": self.families[family["label"]]})
        channel["families"] = families
        # Dists
        self.__postprocessChannelMaps(channel, "dists")
        # release
        self.__postprocessChannelMaps(channel, "release")

    # pylint: disable-next=redefined-builtin
    def __postprocessChannelMaps(self, channel, map):
        if map in channel and channel[map] is not None:
            # pylint: disable-next=redefined-builtin
            for dict in channel[map]:
                arch = dict["channel_arch"]
                if self.arches[arch] is None:
                    # Mark it as ignored
                    channel.ignored = 1
                    # pylint: disable-next=consider-using-f-string
                    raise InvalidArchError(arch, "Unsupported channel arch %s" % arch)
                dict["channel_arch_id"] = self.arches[arch]

    def submit(self):
        parentChannels = {}
        # Split the batch into null and non-null parent channels
        nullParentBatch = []
        nonNullParentBatch = []
        channel_trusts = []
        for channel in self.batch:
            if channel.ignored:
                continue
            if "trust_list" in channel and channel["trust_list"]:
                self.backend.clearChannelTrusts(channel["label"])
                for trust in channel["trust_list"]:
                    if (
                        "org_id" in channel
                        and channel["org_id"]
                        and self.backend.orgTrustExists(
                            channel["org_id"], trust["org_trust_id"]
                        )
                    ):
                        channel_trusts.append(
                            {
                                "channel-label": channel["label"],
                                "org-id": trust["org_trust_id"],
                            }
                        )
            parent = channel["parent_channel"]
            if not parent:
                nullParentBatch.append(channel)
                continue
            nonNullParentBatch.append(channel)
            # And save the parent channel's label in a hash too
            parentChannels[parent] = None
        # Process the easy case of null parent channels
        try:
            self.backend.processChannels(nullParentBatch, True)
        except:
            self.backend.rollback()
            raise

        # Find the parent channels ids
        for channel in nullParentBatch:
            if channel.ignored:
                continue
            label = channel["label"]
            if label not in parentChannels:
                # This channel is not a parent channel to anybody
                continue
            parentChannels[label] = channel.id

        # Build an extra hash for the channels with unknown ids
        unknownChannels = {}
        for k, v in list(parentChannels.items()):
            if v is None:
                unknownChannels[k] = None

        # And look them up
        self.backend.lookupChannels(unknownChannels)

        # Copy the ids back into parentChannels, to make life easier
        missingParents = []
        for k, v in list(unknownChannels.items()):
            if v is None:
                missingParents.append(k)
            else:
                parentChannels[k] = v["id"]
        if missingParents:
            raise MissingParentChannelError(
                missingParents,
                # pylint: disable-next=consider-using-f-string
                "Invalid import, this parents need to be imported: %s"
                % str(", ".join(missingParents)),
            )

        # Fix up the parent channels
        for channel in nonNullParentBatch:
            parent = channel["parent_channel"]
            if parent not in parentChannels:
                # Unknown parent channel
                channel.ignored = 1
                continue
            # Replace the label with the id
            channel["parent_channel"] = parentChannels[parent]

        # And process these channels too
        try:
            self.backend.processChannels(nonNullParentBatch, False)
        except:
            self.backend.rollback()
            raise

        # Process the channel trusts
        if len(channel_trusts) > 0:
            self.backend.processChannelTrusts(channel_trusts)

        # Finally go back and add the products and content sources, if any
        for channel in self.batch:
            if channel.ignored:
                continue

            if ("channel_product" in channel and channel["channel_product"]) or (
                "product_name" in channel and channel["product_name"]
            ):
                self.backend.processChannelProduct(channel)

            self.backend.processChannelContentSources(channel)

        # Sometimes we may want to turn commits off
        if self.will_commit:
            self.backend.commit()


# pylint: disable-next=missing-class-docstring
class ChannelFamilyImport(Import):
    def preprocess(self):
        self.__filterCustomChannelFamilies()
        # We have to look up the channels for this channel family first
        self.channels = {}
        # pylint: disable-next=redefined-outer-name
        for cf in self.batch:
            for c in cf["channels"]:
                self.channels[c] = None

    def fix(self):
        self.backend.lookupChannels(self.channels)
        # pylint: disable-next=redefined-outer-name
        for cf in self.batch:
            channel_ids = cf["channel_ids"] = []
            for c in cf["channels"]:
                chash = self.channels[c]
                if chash is None:
                    # Skip
                    continue
                cid = chash["id"]
                channel_ids.append(cid)

    def submit(self):
        try:
            self.backend.processChannelFamilies(self.batch)
            self.backend.processChannelFamilyMembers(self.batch)
            self.backend.processChannelFamilyPermissions(self.batch)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()

    def __filterCustomChannelFamilies(self):
        """Filter out private channel families from ISS syncs. WebUI
        creates these for us at the org creation time.
        """
        new_batch = []
        # pylint: disable-next=redefined-outer-name
        for cf in self.batch:
            if not cf["label"].startswith("private-channel-family"):
                new_batch.append(cf)
        self.batch = new_batch


# pylint: disable-next=missing-class-docstring
class DistChannelMapImport(Import):
    # pylint: disable-next=redefined-outer-name,redefined-outer-name
    def __init__(self, batch, backend):
        Import.__init__(self, batch, backend)
        self.arches = {}
        self.channels = {}

    def preprocess(self):
        # Processes the batch to a form more suitable for database
        # operations
        # pylint: disable-next=redefined-outer-name
        for dcm in self.batch:
            self.arches[dcm["arch"]] = None
            self.channels[dcm["channel"]] = None

    def fix(self):
        # Look up arches and channels
        self.backend.lookupChannelArches(self.arches)
        self.backend.lookupChannels(self.channels)
        # pylint: disable-next=redefined-outer-name
        for dcm in self.batch:
            arch = self.arches[dcm["arch"]]
            if arch is None:
                # Invalid arch
                dcm.ignored = 1
                raise InvalidArchError(
                    dcm["arch"],
                    # pylint: disable-next=consider-using-f-string
                    "Invalid dist_channel_map arch %s" % dcm["arch"],
                )
            channel = self.channels[dcm["channel"]]
            if channel is None:
                dcm.ignored = 1
                raise InvalidChannelError(
                    dcm["channel"],
                    # pylint: disable-next=consider-using-f-string
                    "Invalid dist_channel_map channel %s" % dcm["channel"],
                )
            dcm["arch"] = arch
            dcm["channel_id"] = channel["id"]
            dcm["org_id"] = None

    def submit(self):
        try:
            self.backend.processDistChannelMap(self.batch)
        except:
            self.backend.rollback()
            raise
        self.backend.commit()


# for testing only
if __name__ == "__main__":
    import sys
    from spacewalk.server import rhnSQL
    from backendOracle import OracleBackend
    from .importLib import Collection, ChannelFamily, DistChannelMap

    backend = OracleBackend()
    # pylint: disable-next=using-constant-test
    if 1:
        batch = Collection()
        dcms = [
            {
                "os": "Red Hat Linux",
                "release": "7.2",
                "arch": "i386",
                "channel": "redhat-linux-i386-7.2",
            },
            {
                "os": "Red Hat Linux",
                "release": "6.2",
                "arch": "i386",
                "channel": "redhat-linux-i386-6.2",
            },
        ]

        for dcm in dcms:
            x = DistChannelMap()
            x.populate(dcm)
            batch.append(x)
        rhnSQL.initDB()
        backend.init()
        dcmimp = DistChannelMapImport(batch, backend)
        dcmimp.run()
        sys.exit(0)
    # pylint: disable-next=using-constant-test
    if 0:
        batch = Collection()
        families = [
            {
                "name": "Cisco Linux",
                "label": "cisco",
                "product_url": "http://www.redhat.com/products/ADSFASDFASDF",
            },
            {
                "name": "Misa Linux",
                "label": "misa",
                "product_url": "http://people.redhat.com/misa/ASDFASDFASDF",
            },
        ]
        for fam in families:
            cf = ChannelFamily()
            cf.populate(fam)
            batch.append(cf)
        rhnSQL.initDB()
        backend.init()
        cfimp = ChannelFamilyImport(batch, backend)
        cfimp.run()
        sys.exit(0)
