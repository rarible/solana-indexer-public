apply(plugin = "server")

val solanaOpenapiVersion: String by project
val raribleCommonVersion: String by project

dependencies {
    implementation(project(":solana-common"))
    implementation(project(":util-borsh"))
    implementation("com.rarible.core:rarible-core-apm-starter:$raribleCommonVersion")

    testImplementation("com.rarible.solana.protocol:solana-protocol-subscriber-starter:$solanaOpenapiVersion")
}
