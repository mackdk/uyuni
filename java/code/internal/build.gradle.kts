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
group = "com.suse.manager"
version = "4.3-SNAPSHOT"

plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
            include("com/redhat/rhn/internal/doclet/**/*.java")
        }
        resources {
            setSrcDirs(listOf("src"))
            include("com/redhat/rhn/internal/doclet/**/*")
            exclude("**/*.java")
        }
    }
}

tasks.compileJava {
    mustRunAfter(":java:processResources")
}

dependencies {

    implementation(project(":java"))

    fromObs("openSUSE:Leap:15.4", "standard") {
        implementation("suse:commons-collections:3.2.2").fromPackage("apache-commons-collections")
        implementation("suse:commons-lang3:3.8.1").fromPackage("apache-commons-lang3")
        implementation("suse:commons-logging:1.2").fromPackage("apache-commons-logging").matchingJar("apache-commons-logging\\.jar")
        implementation("suse:log4j-api:2.17.1").fromPackage("log4j")
        implementation("suse:log4j-core:2.17.1").fromPackage("log4j")
    }

    fromObs("systemsmanagement:Uyuni:Master:Other", "openSUSE_Leap_15.4") {
        implementation("suse:velocity-engine-core:2.3")
    }
}
