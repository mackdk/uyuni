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
import org.gradle.api.tasks.testing.TestResult.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.*
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.gradle.internal.logging.text.StyledTextOutput.*
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf

group = "com.suse.manager"
version = "4.3-SNAPSHOT"

plugins {
    war
    checkstyle
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("code/src"))

            // Fix for wrong folder structure
            exclude("**/*Test.java")
            exclude("**/test/*.java")
            exclude("**/testing/**/*.java")

            exclude("com/suse/manager/webui/utils/SparkTestUtils.java")
        }
        resources {
            setSrcDirs(listOf("code/src"))

            // Fix for wrong folder structure
            exclude("**/test")
            exclude("**/testing")

            exclude("**/*.tld")
        }
        output.setResourcesDir(file("$buildDir/classes/java/main"))
    }

    // Fix for wrong folder structure
    test {
        java {
            setSrcDirs(listOf("code/src"))

            include("**/*Test.java")
            include("**/test/*.java")
            include("**/testing/**/*.java")

            include("com/suse/manager/webui/utils/SparkTestUtils.java")
        }
        resources {
            setSrcDirs(listOf("code/src"))
        }
        output.setResourcesDir(file("$buildDir/classes/java/test"))
    }
}

checkstyle {
    toolVersion = "8.30"
    configFile = file("buildconf/checkstyle.xml")

    isShowViolations = true
    isIgnoreFailures = true
    maxErrors = 0

    configProperties = mapOf(
        "checkstyle.suppressions.file" to file("buildconf/checkstyle-suppressions.xml"),
        "checkstyle.header.file" to file("buildconf/LICENSE.txt"),
        "checkstyle.cache.file" to file("$buildDir/checkstyle-cachefile"),
        "javadoc.method.scope" to "public",
        "javadoc.var.scope" to "package",
        "javadoc.type.scope" to "package",
        "javadoc.lazy" to false
    )
}

tasks.compileJava {
    finalizedBy("checkstyleMain")
}

tasks.compileTestJava {
    finalizedBy("checkstyleTest")
}

tasks.jar {
    archiveFileName.set("rhn.jar")

    // Copy the webapp main configuration xml
    from("code/webapp") {
        include("**/web.xml", "**/struts-config.xml")
        includeEmptyDirs = false
    }

    // Find tag libs in the classpath and move them all to META-INF
    from("code/src") {
        include("**/*.tld")

        eachFile {
            logger.quiet("Found TLD $name")
            path = "META-INF/$name"
        }

        includeEmptyDirs = false
    }
}

tasks.war {
    archiveBaseName.set("${rootProject.name}-${project.name}")

    from(tasks.jar) {
        into("WEB-INF/lib")
    }

    from("code/webapp")

    from("conf") {
        include("rhn-tomcat9.xml")
        rename("rhn-tomcat9.xml", "context.xml")
        into("META-INF")
    }

    from("code/build/generated/resources/apidocs") {
        include("**/*.jsp")
        includeEmptyDirs = false
    }

    exclude("main")

    // Excluding from the war branding jar and all the main classes, since we are bundling rhn.jar
    setClasspath(classpath?.filter { it.name == "main" || it.name.startsWith("branding-") } ?: files())
}

tasks.test {

    onlyIf {
        !(project.findProperty("java.skipTests") as? Boolean ?: false)
    }

    useJUnitPlatform()

    testLogging {
        setEvents(listOf(FAILED, SKIPPED))

        showExceptions = true
        showCauses = true
        showStackTraces = true

        // set options for log level DEBUG and INFO
        debug {
            setEvents(listOf(FAILED, PASSED, SKIPPED, STANDARD_ERROR, STANDARD_OUT))
            exceptionFormat = FULL
        }

        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat

        afterSuite { descriptor, result ->

            if (descriptor.parent == null) {
                val out = serviceOf<StyledTextOutputFactory>().create("output")
                out.withStyle(Style.Normal).text("\nTest run ")

                when (result.resultType) {
                    ResultType.SUCCESS -> out.withStyle(Style.SuccessHeader).text("SUCCEEDED")
                    ResultType.FAILURE -> out.withStyle(Style.FailureHeader).text("FAILED")
                    ResultType.SKIPPED -> out.withStyle(Style.Info).text("SKIPPED")
                    else -> out.withStyle(Style.Normal).text("${result.resultType}")
                }

                out.withStyle(Style.Normal)
                    .println(": ${result.successfulTestCount} succeeded, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped.")
            }
        }
    }

    systemProperties["rhn.config.dir"] = "buildconf/test/"
    systemProperties["log4j.threshold"] = "debug"

    jvmArgs = listOf("-ea")
}

tasks.register<Copy>("deploy") {
    dependsOn("war")

    from(zipTree(tasks.war.get().archiveFile.get()))
    into("$buildDir/tmp/webapp")

    doLast {
    }
}

// Our compileOnly dependencies are needed to run test
configurations {
    configurations.testImplementation.get().apply {
        extendsFrom(configurations.compileOnly.get())
    }
}

dependencies {
    implementation(project(":branding"))

    fromObs("openSUSE:Leap:15.4", "standard") {
        implementation("suse:asm:3.3.2").fromPackage("asm3").matchingJar("asm3-all")
        implementation("suse:antlr:2.7.7").fromPackage("antlr-java")
        implementation("suse:bcel:5.2")
        implementation("suse:c3p0:0.9.5.5")
        implementation("suse:cglib:3.2.4").fromPackage("cglib").matchingJar("cglib.jar")
        implementation("suse:classpathx-mail-1.3.1-monolithic:1.1.2").fromPackage("classpathx-mail")
        implementation("suse:client:2.1").fromPackage("ongres-scram-client")
        implementation("suse:common:2.1").fromPackage("ongres-scram")
        implementation("suse:stringprep:1.1").fromPackage("ongres-stringprep")
        implementation("suse:saslprep:1.1").fromPackage("ongres-stringprep-saslprep")
        implementation("suse:commons-beanutils:1.9.4").fromPackage("apache-commons-beanutils")
        implementation("suse:commons-cli:1.4").fromPackage("apache-commons-cli")
        implementation("suse:commons-codec:1.11").fromPackage("apache-commons-codec")
        implementation("suse:commons-collections:3.2.2").fromPackage("apache-commons-collections")
        implementation("suse:commons-compress:1.21").fromPackage("apache-commons-compress")
        implementation("suse:commons-discovery:0.4").fromPackage("jakarta-commons-discovery")
        implementation("suse:commons-digester:1.7").fromPackage("jakarta-commons-digester")
            .matchingJar("jakarta-commons-digester-[0-9.]+\\.jar")
        implementation("suse:commons-el:1.0").fromPackage("apache-commons-el")
        implementation("suse:commons-fileupload:1.1.1").fromPackage("jakarta-commons-fileupload")
        implementation("suse:commons-io:2.6").fromPackage("apache-commons-io")
        implementation("suse:commons-jexl:2.1.1").fromPackage("apache-commons-jexl").matchingJar("commons-jexl\\.jar")
        implementation("suse:commons-lang3:3.8.1").fromPackage("apache-commons-lang3")
        implementation("suse:commons-logging:1.2").fromPackage("apache-commons-logging")
            .matchingJar("apache-commons-logging\\.jar")
        implementation("suse:commons-validator:1.3.1").fromPackage("apache-commons-validator")
        implementation("suse:dom4j:1.6.1")
        implementation("suse:geronimo-jta-1.1-api:1.2").fromPackage("geronimo-jta-1_1-api")
        implementation("suse:google-gson:2.8.5")
        implementation("suse:httpclient:4.5.6").fromPackage("httpcomponents-client")
        implementation("suse:httpcore:4.4.10").fromPackage("httpcomponents-core").matchingJar("httpcore.jar")
        implementation("suse:httpcore-nio:4.4.10").fromPackage("httpcomponents-core")
        implementation("suse:httpasyncclient:4.1.4").fromPackage("httpcomponents-asyncclient")
            .matchingJar("httpasyncclient-[0-9.]+")
        implementation("suse:jackson-annotations:2.10.2")
        implementation("suse:jackson-core:2.10.2")
        implementation("suse:jackson-databind:2.10.5.1")
        implementation("suse:jaf:1.1.1").fromPackage("gnu-jaf")
        implementation("suse:javassist:3.23.1")
        implementation("suse:jboss-logging:3.4.1")
        implementation("suse:jdom:1.1.3")
        implementation("suse:jetty-util:9.4.43")
        implementation("suse:joda-time:2.10.1")
        implementation("suse:jsch:0.1.55")
        implementation("suse:log4j-api:2.17.1").fromPackage("log4j")
        implementation("suse:log4j-core:2.17.1").fromPackage("log4j")
        implementation("suse:log4j-slf4j-impl:2.17.1").fromPackage("log4j-slf4j")
        implementation("suse:mchange-commons-java:0.2.20").fromPackage("mchange-commons")
        implementation("suse:oro:2.0.8")
        implementation("suse:postgresql-jdbc:42.2.25").fromPackage("postgresql-jdbc")
        implementation("suse:slf4j-api:1.7.30").fromPackage("slf4j").matchingJar("api")
        implementation("suse:snakeyaml:1.28")
        implementation("suse:xalan-j2:2.7.2").fromPackage("xalan-j2").matchingJar("xalan-j2-[0-9.]+")
        implementation("suse:xalan-j2-serializer:2.7.2").fromPackage("xalan-j2")
        implementation("suse:xerces-j2:2.12.0").fromPackage("xerces-j2")

        compileOnly("suse:jasper-el:9.0.36").fromPackage("tomcat-lib")
        compileOnly("suse:tomcat-el-3.0-api:9.0.36").fromPackage("tomcat-el-3_0-api")
        compileOnly("suse:tomcat-jsp-2.3-api:9.0.36").fromPackage("tomcat-jsp-2_3-api")
        compileOnly("suse:tomcat-juli:9.0.36").fromPackage("tomcat-lib")
        compileOnly("suse:tomcat-servlet-4.0-api:9.0.36").fromPackage("tomcat-servlet-4_0-api")
        compileOnly("suse:jasper:9.0.36").fromPackage("tomcat-lib").matchingJar("jasper\\.jar")

        // Test dependencies marked as implementation due to intellij problem with wrong source structure
        testImplementation("suse:hamcrest-core:1.3")
        testImplementation("suse:hamcrest-library:1.3").fromPackage("hamcrest").matchingJar("hamcrest/library")
        testImplementation("suse:junit:4.12")
    }

    fromObs("systemsmanagement:Uyuni:Master:Other", "openSUSE_Leap_15.4") {
        implementation("suse:byte-buddy:1.8.17")
        implementation("suse:classmate:1.3.4")
        implementation("suse:concurrent:1.3.4")
        implementation("suse:concurrentlinkedhashmap-lru:1.3.1")
        implementation("suse:dwr:3.0.2")
        implementation("suse:ehcache-core:2.10.1").fromPackage("ehcache")
        implementation("suse:hibernate-c3p0:5.3.25").fromPackage("hibernate5")
        implementation("suse:hibernate-commons-annotations:5.0.4")
        implementation("suse:hibernate-core:5.3.25").fromPackage("hibernate5")
        implementation("suse:hibernate-ehcache:5.3.25").fromPackage("hibernate5")
        implementation("suse:hibernate-types:2.12.1").fromPackage("hibernate-types")
        implementation("suse:ical4j:3.0.18")
        implementation("suse:jade4j:1.2.5")
        implementation("suse:jakarta-persistence-api:2.2.2").fromPackage("jpa-api")
        implementation("suse:java-saml:2.4.0").fromPackage("java-saml").matchingJar("java-saml-[0-9.]+")
        implementation("suse:java-saml-core:2.4.0").fromPackage("java-saml")
        implementation("suse:jcommon:1.0.16")
        implementation("suse:jose4j:0.5.1")
        implementation("suse:netty-buffer:4.1.44.Final").fromPackage("netty")
        implementation("suse:netty-codec:4.1.44.Final").fromPackage("netty")
        implementation("suse:netty-common:4.1.44.Final").fromPackage("netty")
        implementation("suse:netty-handler:4.1.44.Final").fromPackage("netty")
        implementation("suse:netty-resolver:4.1.44.Final").fromPackage("netty")
        implementation("suse:netty-transport:4.1.44.Final").fromPackage("netty")
        implementation("suse:netty-transport-native-unix-common:4.1.44.Final").fromPackage("netty")
        implementation("suse:pgjdbc-ng:0.8.7")
        implementation("suse:quartz:2.3.0")
        implementation("suse:redstone-xmlrpc:1.1_20071120").fromPackage("redstone-xmlrpc")
        implementation("suse:redstone-xmlrpc-client:1.1_20071120").fromPackage("redstone-xmlrpc")
        implementation("suse:simpleclient:0.3.0").fromPackage("prometheus-client-java")
            .matchingJar("simpleclient-[0-9.]+")
        implementation("suse:simpleclient_common:0.3.0").fromPackage("prometheus-client-java")
        implementation("suse:simpleclient_httpserver:0.3.0").fromPackage("prometheus-client-java")
        implementation("suse:simpleclient_servlet:0.3.0").fromPackage("prometheus-client-java")
        implementation("suse:simple-core:3.1.3").withArch("x86_64")
        implementation("suse:simple-xml:2.6.2")
        implementation("suse:sitemesh:2.1")
        implementation("suse:spark-core:2.7.2")
        implementation("suse:spark-template-jade:2.3")
        implementation("suse:spy:0.8.7").fromPackage("pgjdbc-ng")
        implementation("suse:statistics:1.0.2")
        implementation("suse:stax2-api:3.1.4").fromPackage("woodstox")
        implementation("suse:stringtree-json:2.0.9")
        implementation("suse:struts:1.2.9")
        implementation("suse:taglibs-standard-impl:1.2.5").fromPackage("tomcat-taglibs-standard-1_2_5")
        implementation("suse:taglibs-standard-jstlel:1.2.5").fromPackage("tomcat-taglibs-standard-1_2_5")
        implementation("suse:taglibs-standard-spec:1.2.5").fromPackage("tomcat-taglibs-standard-1_2_5")
        implementation("suse:woodstox-core-asl:4.4.2").fromPackage("woodstox")
        implementation("suse:xmlsec:2.0.7")
    }

    fromObs("systemsmanagement:Uyuni:Master", "openSUSE_Leap_15.4") {
        implementation("suse:salt-netapi-client:0.20.0")
    }

    implementation("javax.websocket:javax.websocket-api:1.1")

    // Test dependencies marked as implementation due to the wrong source structure
    testImplementation("javax.xml.bind:jaxb-api:2.3.0")
    testImplementation("com.sun.xml.bind:jaxb-core:2.3.0")
    testImplementation("com.sun.xml.bind:jaxb-impl:2.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
//    testImplementation("org.junit.platform:junit-platform-commons:1.8.2")
//    testImplementation("org.junit.platform:junit-platform-console-standalone:1.8.2")
    testImplementation("org.opentest4j:opentest4j:1.2.0")
    testImplementation("org.apiguardian:apiguardian-api:1.1.0")
    testImplementation("org.jmock:jmock:2.12.0")
    testImplementation("org.jmock:jmock-junit5:2.12.0")
    testImplementation("org.jmock:jmock-imposters:2.12.0")
    testImplementation("mockobjects:mockobjects-core:0.09")
    testImplementation("mockobjects:mockobjects-jdk1.4-j2ee1.3:0.09")
    testImplementation("org.objenesis:objenesis:1.0")
    testImplementation("strutstestcase:strutstestcase:2.1.3-1.2-2.4")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

//    fromGitHub {
//        testImplementation("uyuni-project:strutstestcase:0.0.1").tagged("v.0.0.1-alpha").named("strutstest-uyuni-0.0.1.jar")
//    }
}
