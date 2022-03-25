package com.rarible.protocol.solana.nft.listener.block.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.blockchain.scanner.solana.client.SolanaApi
import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import com.rarible.blockchain.scanner.solana.client.dto.SolanaBlockDto
import com.rarible.blockchain.scanner.solana.client.dto.SolanaTransactionDto

class SolanaCacheApi(
    val repository: BlockCacheRepository,
    val httpApi: SolanaHttpRpcApi,
    val mapper: ObjectMapper
) : SolanaApi {
    override suspend fun getBlock(
        slot: Long,
        details: GetBlockRequest.TransactionDetails
    ): ApiResponse<SolanaBlockDto> {
        val bytes = repository.find(slot) ?: return httpApi.getBlock(slot, details)
        val apiResponse = mapper.readValue<ApiResponse<SolanaBlockDto>>(bytes)

        return when (details) {
            GetBlockRequest.TransactionDetails.Full -> apiResponse
            GetBlockRequest.TransactionDetails.None -> apiResponse.copy(
                error = apiResponse.error,
                result = apiResponse.result?.copy(transactions = emptyList())
            )
        }
    }

    override suspend fun getFirstAvailableBlock(): ApiResponse<Long> = httpApi.getFirstAvailableBlock()

    override suspend fun getLatestSlot(): ApiResponse<Long> = httpApi.getLatestSlot()

    override suspend fun getTransaction(signature: String): ApiResponse<SolanaTransactionDto> = httpApi.getTransaction(signature)
}