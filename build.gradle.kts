/*
 * Copyright 2024 Marcel Korzonek and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.time.Instant

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.google.cloud.tools.jib") version "3.4.2"
    java
    application
}

val versionObj = Version(major = "5", minor = "2", revision = "0", classifier = null)

val isCi = System.getenv("GITHUB_ACTIONS") == "true"
val eventName = System.getenv("GITHUB_EVENT_NAME")
val isRelease = eventName == "release"
val commitHash by lazy {
    val commit = System.getenv("GITHUB_SHA")?.take(7)

    if (isCi && commit != null)
        commit
    else
        "DEV"
}

group = "at.xirado"
version = versionObj.toString() + if (isRelease) "" else "_$commitHash"

println("Version: $version")

repositories {
    mavenCentral()
    maven("https://maven.xirado.dev/jitpack")
}

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:5.1.0")
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("club.minnced:discord-webhooks:0.7.5")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.8")

    // Database
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("org.scilab.forge:jlatexmath:1.0.7")
    implementation("io.github.classgraph:classgraph:4.8.146")
    implementation("net.jodah:expiringmap:0.5.10")

    val prometheusVersion = "0.14.1"

    implementation("io.prometheus:simpleclient:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_httpserver:$prometheusVersion")

    implementation("com.facebook:ktfmt:0.36")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.4.4")
    implementation("com.sksamuel.aedile:aedile-core:1.3.1")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:2.0.20")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("com.akuleshov7:ktoml-core:0.5.1")

    val ktorVersion = "2.3.9"
    val exposedVersion = "0.48.0"

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
}

val dockerRegistry = System.getenv("docker_registry") ?: null
val dockerUser = System.getenv("docker_user") ?: null
val dockerPassword = System.getenv("docker_password") ?: null

jib {
    to {
        image = if (dockerRegistry != null)
            "$dockerRegistry/xirado/bean"
        else
            "xirado/bean"
        val tags = buildList {
            add(project.version.toString())
            if (isRelease)
                add("latest")
        }

        setTags(tags)
        if (dockerUser != null && dockerPassword != null) {
            auth {
                username = dockerUser
                password = dockerPassword
            }
        }
    }
    from {
        image = "openjdk:21"
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

tasks.compileJava {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.encoding = "UTF-8"
}

tasks.processResources {
    filesMatching("**/app.properties") {
        expand("version" to project.version, "buildTime" to Instant.now().toEpochMilli().toString())
    }
}

tasks.build {
    dependsOn(tasks.processResources)
}

data class Version(
    val major: String,
    val minor: String,
    val revision: String,
    val classifier: String? = null
) {
    companion object {
        fun parse(string: String): Version {
            val (major, minor, revision) = string.substringBefore("-").split(".")
            val classifier = string.substringAfter("-").takeIf { "-" in string }
            return Version(major, minor, revision, classifier)
        }
    }

    override fun toString(): String {
        return "$major.$minor.$revision" + if (classifier != null) "-$classifier" else ""
    }
}