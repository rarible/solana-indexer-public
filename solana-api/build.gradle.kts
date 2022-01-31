val solanaOpenapiVersion: String by project

dependencies {
    implementation(project(":solana-common"))

    implementation("com.rarible.protocol.solana:solana-protocol-api:$solanaOpenapiVersion")
}
