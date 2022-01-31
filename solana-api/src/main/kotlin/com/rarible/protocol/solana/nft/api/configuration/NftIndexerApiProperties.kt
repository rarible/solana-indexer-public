package com.rarible.protocol.solana.nft.api.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("api")
class NftIndexerApiProperties
