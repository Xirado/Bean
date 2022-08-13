import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.commons.io.FileUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant

application {
    mainClass.set("at.xirado.bean.Main")

    tasks.run.get().workingDir = File(rootProject.projectDir, "build/libs")
}

group = "at.xirado"
version = "1.0.0"

plugins {
    kotlin("jvm") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    application
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
    maven("https://m2.chew.pro/releases")
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("net.dv8tion:JDA:5.0.0-alpha.17")
    implementation("com.github.minndevelopment:jda-ktx:9f01b74")
    implementation("com.github.Topis-Lavalink-Plugins:Topis-Source-Managers:2.0.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("io.github.classgraph:classgraph:4.8.146")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha14")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.postgresql:postgresql:42.3.6")
    implementation("net.jodah:expiringmap:0.5.10")
    implementation("com.github.Xirado:SimpleJSON:7488469")
    implementation("com.github.walkyst:lavaplayer-fork:1.3.97.1")
    implementation(kotlin("reflect"))
    implementation("com.facebook:ktfmt:0.36")

    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.6.21")
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
val compileKotlin: KotlinCompile by tasks
val shadowJar: ShadowJar by tasks

tasks.build {
    dependsOn(processResources)
    dependsOn(clearResources)
    dependsOn(shadowJar)
}

compileKotlin.kotlinOptions.apply {
    jvmTarget = "16"
}

