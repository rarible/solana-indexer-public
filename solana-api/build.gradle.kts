apply(plugin = "server")

val solanaOpenapiVersion: String by project

dependencies {
    implementation(project(":solana-common"))

    testImplementation("com.rarible.protocol.solana:solana-protocol-api:$solanaOpenapiVersion")
    testImplementation(project(":solana-test-common"))
    testImplementation("com.rarible.protocol.solana:solana-protocol-client:$solanaOpenapiVersion")
}
