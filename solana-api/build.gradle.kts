val solanaOpenapiVersion: String by project

dependencies {
    implementation(project(":solana-common"))
    testImplementation("com.rarible.solana.protocol:solana-protocol-client:$solanaOpenapiVersion")
}
