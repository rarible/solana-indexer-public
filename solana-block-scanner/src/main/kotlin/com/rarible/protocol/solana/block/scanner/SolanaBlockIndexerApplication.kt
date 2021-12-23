package com.rarible.protocol.solana.block.scanner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SolanaBlockIndexerApplication

fun main(args: Array<String>) {
    runApplication<SolanaBlockIndexerApplication>(*args)
}