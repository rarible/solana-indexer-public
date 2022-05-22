package com.rarible.solana.proxy.plugins

import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.solana.proxy.converter.BlockConverter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
            val request = call.receive<Map<String, Any?>>()

            when (request["method"] as String) {
                "getBlock" -> {
                    val params = request["params"] as List<*>
                    val slot = params[0] as Int
                    val response = solanaClient.getBlock(slot.toLong(), GetBlockRequest.TransactionDetails.Full)

                    if (response.error != null) {
                        call.respond(
                            ApiResponse(
                                id = response.id,
                                result = null,
                                error = ApiResponse.Error(
                                    "",
                                    response.error!!.code
                                ),
                                jsonrpc = response.jsonrpc
                            )
                        )
                    } else {
                        call.respond(BlockConverter.convert(response))
                    }
                }
                "getSlot" -> {
                    val response = solanaClient.getLatestSlot()

                    call.respond(response)
                }
                "getFirstAvailableBlock" -> {
                    val response = solanaClient.getFirstAvailableBlock()

                    call.respond(response)
                }
                else -> call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
