apply(plugin = "server")

val solanaOpenapiVersion: String by project

dependencies {
    implementation(project(":solana-common"))
    implementation("com.google.crypto.tink:tink:1.6.1")
    implementation("org.bitcoinj:bitcoinj-core:0.16.1")

    testImplementation("com.rarible.protocol.solana:solana-protocol-api:$solanaOpenapiVersion")
    testImplementation(project(":solana-test-common"))
    testImplementation("com.rarible.protocol.solana:solana-protocol-client:$solanaOpenapiVersion")
}
