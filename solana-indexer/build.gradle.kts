apply(plugin = "server")

val solanaOpenapiVersion: String by project

dependencies {
    implementation(project(":solana-common"))
    implementation(project(":util-borsh"))
    testImplementation("com.rarible.solana.protocol:solana-protocol-subscriber-starter:$solanaOpenapiVersion")
}
