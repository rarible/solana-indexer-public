import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://repo.rarible.org/repository/maven-public/")
    }
}

allprojects {
    group = "com.rarible.protocol"
    version = "1.0"

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_1_8}"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    repositories {
        mavenCentral()
        maven(url = "http://nexus-ext.rarible.int/repository/maven-public/") { isAllowInsecureProtocol = true }
        maven(url = "https://repo.rarible.org/repository/maven-public/")
    }
}

tasks.register<TestReport>("coverage") {
    // dummy
}