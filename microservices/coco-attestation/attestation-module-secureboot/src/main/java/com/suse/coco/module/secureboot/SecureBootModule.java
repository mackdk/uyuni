/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.coco.module.secureboot;

import com.suse.coco.module.AttestationModule;
import com.suse.coco.module.AttestationWorker;

import java.util.List;

public class SecureBootModule implements AttestationModule {
    /**
     * Result type for report generated by SNPGuest
     */
    public static final int SECURE_BOOT = 2;

    @Override
    public String getName() {
        return SecureBootModule.class.getName();
    }

    @Override
    public int getSupportedType() {
        return SECURE_BOOT;
    }

    @Override
    public AttestationWorker getWorker() {
        return new SecureBootWorker();
    }

    @Override
    public List<String> getAdditionalMappers() {
        return List.of("mappers/secureboot.xml");
    }
}