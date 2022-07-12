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
import groovy.lang.Closure
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.extra

fun DependencyHandler.fromObs(project: String, repository: String, block : ObsDependencyHandler.() -> Unit ) : ObsDependencyHandler {
    return ObsDependencyHandler(project, repository, this).apply {
        block()
    }
}

fun Dependency?.fromPackage(packageName: String) : Dependency? =
    setObsProperty(this, "obsPackage", packageName, "package name")

fun Dependency?.withArch(architecture: String) : Dependency? =
    setObsProperty(this, "obsArch", architecture, "package architecture")

fun Dependency?.matchingJar(jarFilter: String) : Dependency? =
    setObsProperty(this, "obsJar", jarFilter, "JAR filter")

private fun setObsProperty(dependency: Dependency?, key: String, value: String, description: String): Dependency? {
    if (dependency == null) {
        return null
    }

    if (dependency !is ExtensionAware || !dependency.extra.has("obsRepository")) {
        throw IllegalStateException("Unable to set ${description}: dependency ${dependency.name} is not from OBS.")
    }

    dependency.extra.set(key, value)
    return dependency
}

fun ExtraPropertiesExtension.setIfAbsent(key: String, value: Any?) {
    if (!this.has(key)) {
        this.set(key, value)
    }
}

class ObsDependencyHandler(private val project: String, private val repo: String, private val handler: DependencyHandler) :
    DependencyHandler by handler {

    override fun add(configurationName: String, dependencyNotation: Any): Dependency? {
        return handler.add(configurationName, dependencyNotation)?.also { setObsProperties(it) }
    }

    override fun add(configurationName: String, dependencyNotation: Any, configureClosure: Closure<*>): Dependency {
        return handler.add(configurationName, dependencyNotation, configureClosure).also { setObsProperties(it) }
    }

    private fun setObsProperties(dependency: Dependency) {
        if (dependency is ExtensionAware) {
            dependency.extra.set("obsRepository", "${project.replace(":", ":/")}/$repo")
            dependency.extra.setIfAbsent("obsPackage", null)
            dependency.extra.setIfAbsent("obsArch", "noarch")
            dependency.extra.setIfAbsent("obsJar", null)
        } else {
            throw IllegalStateException("Dependency is not an extensible type.")
        }
    }
}

class ObsDependency(private val project: String, private val repo: String, private val handler: Dependency?): Dependency by handler!! {

    private val dependency: ExtensionAware

    init {
        if (handler == null) {
            throw IllegalStateException("Dependency is not an extensible type.")
        }

        if (handler !is ExtensionAware) {
            throw IllegalStateException("Dependency is not an extensible type.")
        }

        dependency = handler

        dependency.extra.set("obsRepository", "${project.replace(":", ":/")}/$repo")
        dependency.extra.set("obsPackage", null)
        dependency.extra.set("obsArch", "noarch")
        dependency.extra.set("obsJar", null)
    }

    fun fromPackage(obsPackage: String): ObsDependency {
        dependency.extra.set("obsPackage", obsPackage)
        return this
    }

    fun withArch(archictecture: String): ObsDependency {
        dependency.extra.set("obsArch", archictecture)
        return this
    }

    fun matchingJar(jarFilter: String ): ObsDependency {
        dependency.extra.set("obsJar", jarFilter)
        return this
    }
}
