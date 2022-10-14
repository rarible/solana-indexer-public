package com.rarible.solana.proxy

import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.solana.block.BlockCompressor
import com.rarible.solana.block.SolanaBlockCompressingApi
import com.rarible.solana.proxy.plugins.configureHTTP
import com.rarible.solana.proxy.plugins.configureRouting
import com.rarible.solana.proxy.plugins.configureSerialization
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    val blockCompressorProgramIds = System.getProperty("solana.block.proxy.compressor.program.ids", "")
        .takeIf { it.isNotEmpty() }?.split(",")?.map { it.trim() }
        ?.toSet()
        ?: BlockCompressor.DEFAULT_COMPRESSOR_PROGRAM_IDS
    val blockCompressor = BlockCompressor(blockCompressorProgramIds)
    val urls = System.getProperty("solana.block.proxy.urls", "")
        .split(",").map { it.trim() }
        .also { require(it.isNotEmpty()) { "No 'solana.block.proxy.urls' specified" } }
    val solanaBlockCompressingApi = SolanaBlockCompressingApi(
        httpApi = SolanaHttpRpcApi(urls = urls, timeoutMillis = 20_000),
        blockCompressor = blockCompressor
    )
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureRouting(solanaBlockCompressingApi)
        configureSerialization()
        configureHTTP()
    }.start(wait = true)
}