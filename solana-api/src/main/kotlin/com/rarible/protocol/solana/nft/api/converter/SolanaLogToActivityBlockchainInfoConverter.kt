package com.rarible.protocol.solana.nft.api.converter

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.dto.ActivityBlockchainInfoDto

object SolanaLogToActivityBlockchainInfoConverter {
    fun convert(log: SolanaLog) = ActivityBlockchainInfoDto(
        blockNumber = log.blockNumber,
        blockHash = log.blockHash,
        transactionIndex = log.transactionIndex,
        transactionHash = log.transactionHash,
        instructionIndex = log.instructionIndex,
        innerInstructionIndex = log.innerInstructionIndex,
    )
}