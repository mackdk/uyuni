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
package com.suse

import com.suse.extensions.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.artifacts.Dependency
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.StopExecutionException
import org.gradle.kotlin.dsl.extra
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.GZIPInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader
import javax.xml.xpath.XPathFactory

object ObsDependencyDownloader {

    private const val BASE_URL = "https://download.opensuse.org/repositories"

    private val LOGGER = Logging.getLogger(ObsDependencyDownloader.javaClass)

    private val CACHE_DIR = Paths.get(System.getProperty("user.home"), ".cache", "ObsDependencyDownloader")

    private val PACKAGE_DIR = CACHE_DIR.resolve("package-content")

    private val PACKAGE_CACHE = CACHE_DIR.resolve("cache.json");

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance()

    private val xmlInputFactory = XMLInputFactory.newInstance()

    private val xPathFactory = XPathFactory.newInstance()

    fun downloadAll(localMavenUri: URI, dependenciesList: List<Dependency>) {

        if (dependenciesList.isEmpty()) {
            return
        }

        // Extract only the dependencies that are extended with the OBS properties and are not available in the local
        // Maven repository
        val obsDependenciesMap = dependenciesList.filter { it is ExtensionAware && it.extra.has("obsRepository") && Paths.get(localMavenUri).resolve("${it.group}/${it.name}/${it.version}/${it.name}-${it.version}.jar").notExists }
            .map { ObsDependency(it) }
            .groupBy { it.obsRepository }

        if (obsDependenciesMap.isEmpty()) {
            return
        }

        // Ensure cache directories exist before starting the process
        CACHE_DIR.ifNotExists { Files.createDirectories(it) }
        PACKAGE_DIR.ifNotExists { Files.createDirectories(it) }

        val repositoryCacheMap : MutableMap<String, RepositoryCache> =
            PACKAGE_CACHE.takeIf { it.exists }?.let { Json.decodeFromString(it.readText()) } ?: HashMap()

        try {
            obsDependenciesMap.forEach { repository, dependencies ->
                LOGGER.info("Evaluating repository ${repository}")
                val cache = updateRepositoryCache(repositoryCacheMap, repository)

                dependencies.forEach { dep ->

                    LOGGER.info("\t> Looking for package ${dep.packageName} of repository ${dep.obsRepository}")
                    val rpmPackage = extractPackage(dep.packageName, dep.arch, cache) ?: run {
                        throw StopExecutionException("Unable to satisfy requirement ${dep.artifactName}: unable to find ${dep.packageName} (${dep.arch}) in OBS repository ${dep.obsRepository}.")
                    }

                    if (needsDownload(localMavenUri, dep, rpmPackage, cache)) {

                        LOGGER.info("\t> Downloading RPM package ${rpmPackage.location}")
                        val packageFile =
                            runCatching { downloadPackage(dep.name, dep.obsRepository, rpmPackage.location) }
                                .getOrElse { throw StopExecutionException("Unable to download package $BASE_URL/${dep.obsRepository}/${rpmPackage.location}.\nMaybe it was updated, clean the local repository index cache and try again.") }

                        LOGGER.info("\t> Installing jar dependency")
                        val jarPath = extractJar(packageFile, dep.jarFilter) ?: throw StopExecutionException(
                            "Unable to satisfy requirement ${dep.artifactName}: unable to find jar for ${dep.name} version ${dep.version} (regex: ${dep.jarFilter}) within the RPM package."
                        )

                        installJar(dep, localMavenUri, jarPath)
                        cache.cacheJarPackageChecksum(rpmPackage)

                        Files.deleteIfExists(packageFile)

                        LOGGER.quiet("Dependency ${dep.artifactName} downloaded and installed successfully")
                    }
                }
            }
        } finally {
            // Store the package cache
            PACKAGE_CACHE.writeText(Json.encodeToString(repositoryCacheMap))
        }

        LOGGER.quiet("All dependencies from OBS repositories are available and up to date")
    }

    private fun updateRepositoryCache(cacheMap: MutableMap<String, RepositoryCache>, repository: String): RepositoryCache {

        LOGGER.debug("\t\t> Parsing remote repomd.xml")
        val repomd = documentBuilderFactory.newDocumentBuilder().parse("$BASE_URL/$repository/repodata/repomd.xml")

        val primaryLocation = xPathFactory.newXPath().compile("/repomd/data[@type='primary']/location/@href").evaluate(repomd)
        val primaryChecksum = xPathFactory.newXPath().compile("/repomd/data[@type='primary']/checksum/text()").evaluate(repomd)

        var repoCache = cacheMap.getOrPut(repository, { RepositoryCache(repository, primaryLocation) })
        if (repoCache.indexFile.notExists || repoCache.checksum != primaryChecksum) {
            repoCache.checksum = primaryChecksum

            LOGGER.debug("\t\t> Retrieving updated primary.xml")

            GZIPInputStream(URI("$BASE_URL/$repository/$primaryLocation").toURL().openStream()).use { input ->
                repoCache.indexFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return repoCache
    }


    private fun extractPackage(packageName: String, packageArch: String, cache: RepositoryCache): Rpm? {
        val matchingPackages = ArrayList<Rpm>()

        var pkg = cache.getRemotePackage(packageName, packageArch)
        if (pkg != null) {
            return pkg;
        }

        cache.indexFile.inputStream().use { inputStream ->
            var (name, arch, location) = listOf("", "", "")
            var (epoch, version, release) = listOf("", "", "")
            var (checksumType, checksum) = listOf("", "")

            xmlInputFactory.createXMLStreamReader(inputStream).use { reader ->
                while (reader.hasNext()) {
                    when (reader.next()) {
                        // Collect the information at the start of the tag
                        XMLStreamReader.START_ELEMENT -> {
                            when (reader.localName) {
                                "name" -> name = reader.elementText
                                "arch" -> arch = reader.elementText
                                "version" -> {
                                    epoch = reader.getAttributeValue(null, "epoch")
                                    version = reader.getAttributeValue(null, "ver")
                                    release = reader.getAttributeValue(null, "rel")
                                }
                                "location" -> location = reader.getAttributeValue(null, "href")
                                "checksum" -> {
                                    checksumType = reader.getAttributeValue(null, "type")
                                    checksum = reader.elementText
                                }
                            }
                        }

                        // Evaluate if the package is the one we are looking for
                        XMLStreamReader.END_ELEMENT -> {
                            when (reader.localName) {
                                "package" -> if (name == packageName && arch == packageArch) {
                                    val rpm = Rpm(name, arch, epoch, version, release, checksumType, checksum, location)
                                    LOGGER.debug("\t\tFound version ${rpm.fullVersion} for package ${rpm.name} of architecture ${rpm.arch}")
                                    matchingPackages.add(rpm)
                                }
                            }
                        }
                    }
                }
            }
        }

        // TODO Manage package multiple versions
        return matchingPackages.singleOrNull()?.apply { cache.cacheRemotePackage(packageName, packageArch, this) }
    }


    private fun needsDownload(localMavenUri: URI, depedency: ObsDependency, rpmPackage: Rpm, repositoryCache: RepositoryCache): Boolean {
        if (Paths.get(localMavenUri).resolve(depedency.localFile).notExists) {
            return true
        }

        val localChecksum = repositoryCache.getJarPackageChecksum(rpmPackage)
        if (localChecksum == null) {
            return true
        }

        return rpmPackage.checksum != localChecksum;
    }

    private fun downloadPackage(dependency: String, repository: String, rpmPackage: String): Path {
        val tempRpmFile = Files.createTempFile(dependency, ".rpm")

        URI("$BASE_URL/$repository/$rpmPackage").toURL().openStream().use {
            tempRpmFile.writeBytes(it.readBytes())
        }

        return tempRpmFile
    }

    private fun extractJar(packageFile: Path, jarRegex: Regex): Path? {
        // Extract the listing in the format file>linkTo
        val packageJars = "rpm -qp --queryformat [%{FILENAMES}>%{FILELINKTOS}\\n] $packageFile".execute().text
            .split("\n")
            .asSequence()
            .filter { it.endsWith(">") } // Due to the format, regular files will end with >
            .map { it.removeSuffix(">") } // Remove the >
            .filter { it.startsWith("/usr") && it.endsWith(".jar") } // Consider only jars in /usr
            .map { it.substring(1) } // Make the path relative
            .onEach { LOGGER.debug("\t\tFound jar: $it") }
            .toList()

        val jarFile = packageJars.singleOrNull() ?: packageJars.singleOrNull { it.contains(jarRegex) } ?: return null
        "rpm2cpio $packageFile".execute().pipeTo("cpio -id ./$jarFile".execute(PACKAGE_DIR)).waitFor()

        return PACKAGE_DIR.resolve(Path.of(jarFile)).takeIf { it.exists }
    }

    private fun installJar(dep: ObsDependency, localMavenUri: URI, jarPath: Path) {
        val jarFile = Paths.get(localMavenUri).resolve(dep.localFile)

        // Create the local folder if not existing
        Files.createDirectories(jarFile.parent)

        // Move the jar file
        Files.move(jarPath, jarFile, StandardCopyOption.REPLACE_EXISTING)

        // Create the pom
        jarFile.parent.resolve("${dep.name}-${dep.version}.pom").writeText(
            """
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>${dep.group}</groupId>
  <artifactId>${dep.name}</artifactId>
  <version>${dep.version}</version>
  <description>POM created by ObsDependencyDownloader</description>
</project>
        """.trimIndent()
        )
    }

    @Serializable
    private data class RepositoryCache(val name: String, var location: String) {

        public var checksum: String? = null
            get() = field
            set(value) {
                field = value

                // The checksum has changed invalidate the packages
                packageMap.clear()
            }

        public val indexFile get() = CACHE_DIR.resolve(name).resolve("primary.xml")

        private val packageMap: MutableMap<String, Rpm> = HashMap()

        private val artifactChecksumMap: MutableMap<String, String> = HashMap()

        init {
            indexFile.parent.ifNotExists { Files.createDirectories(it) }
        }

        public fun getRemotePackage(name: String, arch: String) = packageMap.get("$name.$arch")

        public fun cacheRemotePackage(name: String, arch: String, rpmPackage: Rpm) {
            packageMap.put("$name.$arch", rpmPackage);
        }

        public fun getJarPackageChecksum(rpmPackage: Rpm) = artifactChecksumMap.get(rpmPackage.fullPackageName)

        public fun cacheJarPackageChecksum(rpmPackage: Rpm) {
            artifactChecksumMap.put(rpmPackage.fullPackageName, rpmPackage.checksum);
        }
    }

    @Serializable
    private data class Rpm(val name: String, val arch: String, val epoch: String, val version: String, val release: String, val checksumType: String, val checksum: String, val location: String) {

        public val fullVersion: String
            get() = epoch.takeIf { it.isNotBlank() && it != "0" }?.let { "$epoch:$version-$release" } ?: "$version-$release"

        public val fullPackageName get() = "$name-$fullVersion.$arch.rpm"

    }

    private class ObsDependency(private val dep: Dependency) : Dependency by dep {

        val obsRepository: String
        val packageName: String
        val arch: String
        val jarFilter: Regex
        val artifactName: String

        val localFile get() = "${this.group}/${this.name}/${this.version}/${this.name}-${this.version}.jar"

        init {
            if (dep !is ExtensionAware) {
                throw IllegalStateException("Wrapped dependency must be an ExtensionAware instance")
            }

            artifactName = "${dep.name}-${dep.version}.jar"
            obsRepository = dep.extra["obsRepository"] as? String
                ?: throw IllegalStateException("Dependency is missing required obsRepository property")
            packageName = dep.extra["obsPackage"] as? String ?: dep.name
            arch = dep.extra["obsArch"] as? String ?: "noarch"
            jarFilter = buildRegex(dep.name, dep.version, dep.extra["obsJar"] as? String)
        }

        private fun buildRegex(name: String, version: String?, customFilter: String? = null): Regex {
            val filter = customFilter ?: name.plus(version?.let { "(-$it){0,1}" } ?: "")

            if (!filter.endsWith(".jar")) {
                return Regex("$filter\\.jar")
            }

            return Regex(filter)
        }

    }
}
