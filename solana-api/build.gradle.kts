val solanaOpenapiVersion: String by project

dependencies {
    implementation(project(":solana-common"))

    implementation("com.rarible.solana.protocol:solana-protocol-api:$solanaOpenapiVersion")
    testImplementation("com.rarible.solana.protocol:solana-protocol-client:$solanaOpenapiVersion")
}
