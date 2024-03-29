val solanaScannerVersion: String by project
val raribleCommonVersion: String by project
val solanaOpenapiVersion: String by project
val tinkVersion: String by project

dependencies {
    api(project(":util-borsh"))
    api("com.rarible.core:rarible-core-meta-resource:$raribleCommonVersion")
    api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    api("com.rarible.blockchain.scanner:rarible-blockchain-scanner-solana:$solanaScannerVersion")
    api("com.rarible.core:rarible-core-entity-reducer:$raribleCommonVersion")
    api("com.rarible.protocol.solana:solana-protocol-api:$solanaOpenapiVersion")

    api("com.google.crypto.tink:tink:$tinkVersion")

    implementation(enforcedPlatform("com.rarible.core:rarible-core-parent:$raribleCommonVersion"))
    testImplementation(project(":solana-test-common"))
}
