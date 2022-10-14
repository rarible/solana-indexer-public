package com.rarible.solana.block

import com.rarible.blockchain.scanner.solana.client.SolanaApi
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.blockchain.scanner.solana.client.dto.SolanaBlockDto
import com.rarible.blockchain.scanner.solana.client.dto.SolanaTransactionDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SolanaBlockCompressingApi(
    private val httpApi: SolanaHttpRpcApi,
    private val blockCompressor: BlockCompressor
) : SolanaApi {

    override suspend fun getBlock(
        slot: Long,
        details: GetBlockRequest.TransactionDetails
    ): ApiResponse<SolanaBlockDto> {
        return if (details == GetBlockRequest.TransactionDetails.None) {
            httpApi.getBlock(slot, details)
        } else {
            val result = httpApi.getBlock(slot, details)
            if (result.result != null) {
                logger.info("Solana API: compressed block #$slot")
                blockCompressor.compress(result)
            } else {
                result
            }
        }
    }

    override suspend fun getBlocks(
        slots: List<Long>,
        details: GetBlockRequest.TransactionDetails
    ): Map<Long, ApiResponse<SolanaBlockDto>> = coroutineScope {
        slots.map { async { it to getBlock(it, details) } }.awaitAll().toMap()
    }

    override suspend fun getFirstAvailableBlock(): ApiResponse<Long> =
        httpApi.getFirstAvailableBlock()

    override suspend fun getLatestSlot(): ApiResponse<Long> =
        httpApi.getLatestSlot()

    override suspend fun getTransaction(signature: String): ApiResponse<SolanaTransactionDto> =
        httpApi.getTransaction(signature)

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(SolanaBlockCompressingApi::class.java)
    }
}