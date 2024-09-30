import java.time.Instant

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("com.google.cloud.tools.jib") version "3.4.2"
    id("com.google.devtools.ksp") version "2.0.20-1.0.25"
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
    mavenLocal()
    maven("https://maven.xirado.dev/releases")
    maven("https://maven.xirado.dev/jitpack")
}

dependencies {
    // JDA
    implementation("io.github.JDA-Fork:JDA:ef41a9445d") // User app support
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("club.minnced:discord-webhooks:0.7.5")
    implementation("at.xirado:JDUI:0.4.5")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Database
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.flywaydb:flyway-core:10.14.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.14.0")

    implementation("io.insert-koin:koin-core:4.0.0-RC1")
    implementation("io.insert-koin:koin-annotations:1.4.0-RC4")
    ksp("io.insert-koin:koin-ksp-compiler:1.3.1")

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("org.tomlj:tomlj:1.1.1")

    val ktorVersion = "2.3.9"
    val exposedVersion = "0.54.0"

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

tasks.processResources {
    filesMatching("**/app.properties") {
        expand("version" to project.version, "buildTime" to Instant.now().toEpochMilli().toString())
    }
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
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