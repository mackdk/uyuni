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
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.extra
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

fun ExtensionAware.loadProperties(propertyFile: Path) {
    println("Loading property file $propertyFile")
    Files.newInputStream(propertyFile).use {
        val prop = Properties()
        prop.load(it)
        prop.forEach { key, value ->
            this.extra.set(key as String, convertValue(value as? String))
            println("Loading property $key with value $value of type " + this.extra.get(key)?.javaClass)
        }
    }
}

private fun convertValue(value: String?) : Any? {
    if (value == null) {
        return null
    }

    return when {
        Regex("^[+-]?\\b[0-9]+\\b$").matches(value) -> value.toInt()
        Regex("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$").matches(value) -> value.toFloat()
        Regex("^(true|false)$").matches(value) -> value.toBoolean()
        else -> value
    }
}
