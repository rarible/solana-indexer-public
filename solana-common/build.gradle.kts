val solanaScannerVersion: String by project
val raribleCommonVersion: String by project
val solanaOpenapiVersion: String by project
val tinkVersion: String by project
val bitcoinJVersion: String by project

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    api("com.rarible.blockchain.scanner:rarible-blockchain-scanner-solana:$solanaScannerVersion")
    api("com.rarible.core:rarible-core-entity-reducer:$raribleCommonVersion")
    api("com.rarible.protocol.solana:solana-protocol-api:$solanaOpenapiVersion")

    api("com.google.crypto.tink:tink:$tinkVersion")
    api("org.bitcoinj:bitcoinj-core:$bitcoinJVersion")

    testImplementation(project(":solana-test-common"))
}
