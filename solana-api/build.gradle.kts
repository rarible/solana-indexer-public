import org.springframework.boot.gradle.tasks.bundling.BootJar

val solanaOpenapiVersion: String by project

plugins {
    id("org.springframework.boot")

    kotlin("jvm")
    kotlin("plugin.spring")
}

tasks.withType<BootJar> {
    enabled = false
}

dependencies {
    implementation("com.rarible.protocol.solana:solana-protocol-api:$solanaOpenapiVersion")
    implementation(project(":solana-common"))
}
