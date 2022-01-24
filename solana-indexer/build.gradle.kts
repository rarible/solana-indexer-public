import org.springframework.boot.gradle.tasks.bundling.BootJar

val springBootVersion: String by project
val solanaScannerVersion: String by project
val mongockVersion: String by project
val testcontainersVersion: String by project
val raribleCommonVersion: String by project

plugins {
    id("org.springframework.boot")

    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":util-borsh"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.rarible.blockchain.scanner:rarible-blockchain-scanner-solana:$solanaScannerVersion")
    implementation("com.github.cloudyrock.mongock:mongock-spring-v5:$mongockVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.rarible.core:rarible-core-entity-reducer:$raribleCommonVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("com.rarible.core:rarible-core-test-common:$raribleCommonVersion")
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
}

tasks.withType<BootJar> {
    destinationDirectory.set(file("./target/boot"))
}