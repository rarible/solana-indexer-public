import org.springframework.boot.gradle.tasks.bundling.BootJar

val springBootVersion: String by project
val mongockVersion: String by project
val testcontainersVersion: String by project
val raribleCommonVersion: String by project

plugins {
    id("org.springframework.boot")

    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":solana-common"))
    implementation(project(":util-borsh"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("com.rarible.core:rarible-core-test-common:$raribleCommonVersion")
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
}

tasks.withType<BootJar> {
    destinationDirectory.set(file("./target/boot"))
}
