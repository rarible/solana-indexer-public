plugins {
    id("org.springframework.boot")

    kotlin("jvm")
    kotlin("plugin.spring")
}

val springBootVersion: String by project
val solanaScannerVersion: String by project
val raribleCommonVersion: String by project

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.rarible.blockchain.scanner:rarible-blockchain-scanner-solana:$solanaScannerVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("com.rarible.core:rarible-core-entity-reducer:$raribleCommonVersion")
}
