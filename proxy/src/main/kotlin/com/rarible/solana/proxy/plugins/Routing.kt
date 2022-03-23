package com.rarible.solana.proxy.plugins

import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.solana.proxy.converter.BlockConverter
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {
    val solanaClient = SolanaHttpRpcApi(
        listOf(
            "https://white-damp-rain.solana-mainnet.quiknode.pro/728e275a5bf349a7384fcc8e72d463df65b24a8c/",
            "https://holy-proud-wave.solana-mainnet.quiknode.pro/790699a8dbe2e4f3b6b5593a366664d78646cf95/"
        ),
        timeoutMillis = 20_000
    )

    routing {
        post("/") {
            val getBlockRequest = call.receive<GetBlockRequest>()

            val slot = getBlockRequest.params!![0] as Int
            val block = solanaClient.getBlock(slot.toLong(), GetBlockRequest.TransactionDetails.Full)

            call.respond(BlockConverter.convert(block))
        }
    }
}
