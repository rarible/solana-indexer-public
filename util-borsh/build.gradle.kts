val bitcoinjVersion: String by project
val springBootVersion: String by project

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation("org.bitcoinj:bitcoinj-core:$bitcoinjVersion")

    testImplementation("org.junit.jupiter:junit-jupiter")
}