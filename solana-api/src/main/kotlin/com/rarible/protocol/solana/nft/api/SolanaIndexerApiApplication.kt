package com.rarible.protocol.solana.nft.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SolanaIndexerApiApplication

fun main(args: Array<String>) {
    runApplication<SolanaIndexerApiApplication>(*args)
}
