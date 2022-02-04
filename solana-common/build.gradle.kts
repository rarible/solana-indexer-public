val solanaScannerVersion: String by project
val raribleCommonVersion: String by project
val solanaOpenapiVersion: String by project

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    api("com.rarible.blockchain.scanner:rarible-blockchain-scanner-solana:$solanaScannerVersion")
    api("com.rarible.core:rarible-core-entity-reducer:$raribleCommonVersion")
    api("com.rarible.core:rarible-core-loader-cache:$raribleCommonVersion")
    api("com.rarible.solana.protocol:solana-protocol-api:$solanaOpenapiVersion")
}
