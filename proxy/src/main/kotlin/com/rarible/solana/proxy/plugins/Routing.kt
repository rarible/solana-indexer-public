package com.rarible.solana.proxy.plugins

import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.solana.block.SolanaBlockCompressingApi
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    solanaBlockCompressingApi: SolanaBlockCompressingApi
) {

    routing {
        post("/") {
            val request = call.receive<Map<String, Any?>>()

            when (request["method"] as String) {
                "getBlock" -> {
                    val params = request["params"] as List<*>
                    val slot = params[0] as Int
                    val response = solanaBlockCompressingApi.getBlock(
                        slot = slot.toLong(),
                        details = GetBlockRequest.TransactionDetails.Full
                    )

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
                        call.respond(response)
                    }
                }
                "getSlot" -> {
                    val response = solanaBlockCompressingApi.getLatestSlot()

                    call.respond(response)
                }
                "getFirstAvailableBlock" -> {
                    val response = solanaBlockCompressingApi.getFirstAvailableBlock()

                    call.respond(response)
                }
                else -> call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
