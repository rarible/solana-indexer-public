package com.rarible.protocol.solana.nft.listener.block.cache

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rarible.blockchain.scanner.solana.client.SolanaApi
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest

class BlockCacheClient(
    private val solanaApi: SolanaApi,
) {

    private val mapper = jacksonObjectMapper()

    suspend fun getBlockBytesToCache(
        slot: Long,
        details: GetBlockRequest.TransactionDetails
    ): ByteArray {
        val blockResponse = solanaApi.getBlock(slot, details)
        return mapper.writeValueAsBytes(blockResponse)
    }
}