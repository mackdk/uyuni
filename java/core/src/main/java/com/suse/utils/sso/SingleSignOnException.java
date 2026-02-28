/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.utils.sso;

/**
 * Exception to be thrown in case of problems during SSO processing.
 */
public class SingleSignOnException extends Exception {

    /**
     * Constructor expecting a custom message.
     * @param message the message
     */
    public SingleSignOnException(String message) {
        super(message);
    }

    /**
     * Constructor expecting a custom message and cause.
     * @param message the message
     * @param cause the cause
     */
    public SingleSignOnException(String message, Throwable cause) {
        super(message, cause);
    }
}
