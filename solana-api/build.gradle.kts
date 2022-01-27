val solanaOpenapiVersion: String by project

plugins {
    id("org.springframework.boot")

    kotlin("jvm")
    kotlin("plugin.spring")
}

dependencies {
    implementation("com.rarible.protocol.solana:solana-protocol-api:$solanaOpenapiVersion")
    implementation(project(":solana-common"))
}
