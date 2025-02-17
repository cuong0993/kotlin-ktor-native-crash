import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")

    // The following line allows to load io.gatling.gradle plugin and directly apply it
    id("io.gatling.gradle") version "3.9.3"
}

gatling {
    // WARNING: options below only work when logback config file isn't provided
    logLevel = "WARN" // logback root level
    logHttp =
        io.gatling.gradle.LogHttp.NONE // set to 'ALL' for all HTTP traffic in TRACE, 'FAILURES' for failed HTTP traffic in DEBUG

    enterprise.closureOf<Any> {
        // Enterprise Cloud (https://cloud.gatling.io/) configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-cloud
        // Enterprise Self-Hosted configuration reference: https://gatling.io/docs/gatling/reference/current/extensions/gradle_plugin/#working-with-gatling-enterprise-self-hosted
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
}

repositories {
    mavenCentral()
}
