import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.commons.io.FileUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant

application {
    mainClass.set("at.xirado.bean.Main")
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
    implementation("com.github.Xirado:JDA:bc50c87")
    implementation("com.github.minndevelopment:jda-ktx:9f01b74")
    implementation("com.github.Topis-Lavalink-Plugins:Topis-Source-Managers:2.0.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("club.minnced:discord-webhooks:0.7.5")
    implementation("org.fusesource.jansi:jansi:2.4.0")

}

tasks.withType<ProcessResources> {
    filesMatching("**/app.properties") {
        expand("app-version" to version, "build-time" to Instant.now().toString())
    }
}

tasks.create("clearResources") {
    FileUtils.deleteDirectory(File(project.rootDir, "build/resources/main"))
}

val clean by tasks
val build by tasks
val clearResources by tasks
val compileKotlin: KotlinCompile by tasks
val shadowJar: ShadowJar by tasks

compileKotlin.kotlinOptions.apply {
    jvmTarget = "16"
}

tasks.build {
    dependsOn(clearResources)
    dependsOn(shadowJar)
}

