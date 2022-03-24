val ktorVersion: String by project
val logbackVersion: String by project
val solanaScannerVersion: String by project

plugins {
    application
}

application {
    mainClass.set("com.rarible.solana.proxy.ApplicationKt")
}

dependencies {
    implementation("com.rarible.blockchain.scanner:rarible-blockchain-scanner-solana:$solanaScannerVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")

    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
}