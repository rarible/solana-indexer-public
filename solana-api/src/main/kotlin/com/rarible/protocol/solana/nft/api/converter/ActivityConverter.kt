package com.rarible.protocol.solana.nft.api.converter

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.dto.ActivityBlockchainInfoDto
import com.rarible.protocol.solana.dto.ActivityDto
import kotlinx.coroutines.flow.Flow

interface ActivityConverter<T> {
    fun convert(flow: Flow<T>): Flow<ActivityDto>

    fun blockchainInfo(log: SolanaLog) = ActivityBlockchainInfoDto(
        blockNumber = log.blockNumber,
        blockHash = log.blockHash,
        transactionIndex = log.transactionIndex,
        transactionHash = log.transactionHash,
        instructionIndex = log.instructionIndex,
        innerInstructionIndex = log.innerInstructionIndex,
    )
}
