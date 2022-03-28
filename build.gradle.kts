import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.0"
    id("org.jetbrains.compose") version "1.1.1"
    id("com.github.johnrengelman.shadow") version "7.1.0"

}

group = "xyz.mastriel"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation(kotlin("reflect"))
    implementation("net.dv8tion:JDA:5.0.0-alpha.9")
    implementation("com.github.minndevelopment:jda-ktx:9f01b74")

    implementation(compose.desktop.currentOs)
}

tasks.withType<ShadowJar> {
    configurations = listOf(project.configurations.shadow.get())
    archiveFileName.set("HostileTakeover Client v${archiveVersion.get()}.jar")
}

compose.desktop {
    application {
        mainClass = "xyz.mastriel.hostiletakeover.HostileTakeoverKt"
        args += "-opt-in=kotlin.RequiresOptIn"
        nativeDistributions {
            targetFormats(TargetFormat.AppImage, TargetFormat.Exe)
        }
    }
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}


