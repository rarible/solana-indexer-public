import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot")

    kotlin("jvm")
    kotlin("plugin.spring")
}

val springBootVersion: String by project
val solanaScannerVersion: String by project
val raribleCommonVersion: String by project

dependencies {
    api(kotlin("reflect"))
    api(kotlin("stdlib-jdk8"))
    api("com.rarible.blockchain.scanner:rarible-blockchain-scanner-solana:$solanaScannerVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    api("com.rarible.core:rarible-core-entity-reducer:$raribleCommonVersion")
}

tasks.withType<BootJar> {
    enabled = false
}
