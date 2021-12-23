package com.rarible.protocol.solana.nft.listener

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NftListenerApplication

fun main(args: Array<String>) {
    runApplication<NftListenerApplication>(*args)
}