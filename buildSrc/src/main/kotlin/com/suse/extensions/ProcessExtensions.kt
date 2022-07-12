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

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import java.nio.file.Path

fun String.execute() : Process = ProcessGroovyMethods.execute(this)

fun String.execute(workDir: Path) : Process = ProcessGroovyMethods.execute(this, null as Array<String>?, workDir.toFile())

fun Process.pipeTo(other : Process) : Process = ProcessGroovyMethods.pipeTo(this, other)

val Process.text: String get() = ProcessGroovyMethods.getText(this) ?: ""
