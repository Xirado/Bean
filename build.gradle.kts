import org.apache.commons.io.FileUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant

application {
    mainClass.set("at.xirado.bean.Bean")
    tasks.run.get().workingDir = File(rootProject.projectDir, "build/libs")
}

plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    application
}

group = "at.xirado"
version = "5.0.0-musicOnly"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io/")
    maven("https://m2.chew.pro/releases")
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("net.dv8tion:JDA:5.0.0-beta.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.7.3")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha5")
    implementation("com.zaxxer:HikariCP:5.0.0")
    implementation("com.google.guava:guava:31.0.1-jre")
    implementation("io.github.classgraph:classgraph:4.8.146")
    implementation("com.github.minndevelopment:jda-ktx:17eb77a")
    implementation("net.jodah:expiringmap:0.5.10")
    implementation("com.github.chew:jda-chewtils:5e1a9f93f9")
    implementation("com.github.walkyst:lavaplayer-fork:e833a69")
    implementation("com.github.TopiSenpai.LavaSrc:lavasrc:7bd20fd")
    implementation("club.minnced:udpqueue-native-linux-x86-64:0.2.5") // adds linux 64bit
    implementation("club.minnced:udpqueue-native-win-x86-64:0.2.5") // adds windows 64bit
    implementation("com.sedmelluq:jda-nas:1.1.0") {
        exclude(module="udp-queue")
    }
}

tasks {
    named<ProcessResources>("processResources") {
        outputs.upToDateWhen { false }

        filesMatching("**/app.properties") {
            filter {
                it.replace("@app-version@", version as String)
            }
            filter {
                it.replace("@build-time@", Instant.now().toEpochMilli().toString())
            }
        }
    }
}

tasks.create("clearResources") {
    FileUtils.deleteDirectory(File(project.rootDir, "build/resources/main"))
}

val clean by tasks
val build by tasks
val processResources by tasks
val clearResources by tasks
val compileKotlin by tasks
val shadowJar by tasks

tasks.build {
    dependsOn(processResources)
    dependsOn(clearResources)
    dependsOn(shadowJar)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}