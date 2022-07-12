/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.extensions

import java.nio.file.Files
import java.nio.file.Path

inline fun Path.ifExists(block: (Path) -> Unit) {
    if (Files.exists(this)) {
        block(this);
    }
}

inline fun Path.ifNotExists(block: (Path) -> Unit) {
    if (!Files.exists(this)) {
        block(this);
    }
}

fun Path.readText(): String = Files.readString(this)

fun Path.writeText(text: String) = Files.writeString(this, text)

fun Path.writeBytes(bytes: ByteArray) = this.toFile().writeBytes(bytes)

fun Path.outputStream() = this.toFile().outputStream()

fun Path.inputStream() = this.toFile().inputStream()

val Path.exists: Boolean
    get() = Files.exists(this)

val Path.notExists: Boolean
    get() = Files.notExists(this)

