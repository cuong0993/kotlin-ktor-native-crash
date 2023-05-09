plugins {
    kotlin("multiplatform") version Versions.kotlin
}

group = "com.test"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val hostArch = System.getProperty("os.arch")
    val nativeTarget = when (hostOs) {
        "Mac OS X" -> if (hostArch == "aarch64") {
            macosArm64("native")
        } else {
            macosX64("native")
        }

        "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(deps.ktor.serverCore)
                implementation(deps.ktor.serverCio)
                implementation(deps.ktor.clientCore)
                implementation(deps.ktor.clientCurl)
            }
        }
        val nativeTest by getting
    }
}