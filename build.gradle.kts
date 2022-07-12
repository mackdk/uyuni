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
import com.suse.ObsDependencyDownloader
import com.suse.extensions.ifExists
import java.nio.file.Path

Path.of("user.gradle.properties").ifExists {
    rootProject.loadProperties(it)
}

allprojects {

    repositories {
        mavenLocal {
            name = "SUSE from OBS"
            content {
                includeGroup("suse")
            }
        }

        mavenCentral {
            content {
                excludeGroup("suse")
            }
        }
    }

    afterEvaluate {
        if (plugins.findPlugin(JavaPlugin::class) != null) {
            val suseLocalMavenRepository = repositories["SUSE from OBS"] as MavenArtifactRepository
            val dependencies = configurations.flatMap { it.dependencies }

            ObsDependencyDownloader.downloadAll(suseLocalMavenRepository.url, dependencies)
        }

    }
}



