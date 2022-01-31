package com.rarible.protocol.solana.nft.listener

import com.rarible.protocol.solana.common.configuration.CommonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(CommonConfiguration::class)
class NftListenerApplication

fun main(args: Array<String>) {
    runApplication<NftListenerApplication>(*args)
}
