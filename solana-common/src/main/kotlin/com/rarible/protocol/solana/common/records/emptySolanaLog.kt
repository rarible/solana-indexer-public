package com.rarible.protocol.solana.common.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog

/**
 * Used as a placeholder in places where the log is not necessary to compare.
 */
val EMPTY_SOLANA_LOG: SolanaLog = SolanaLog(
    blockNumber = 0,
    blockHash = "",
    transactionIndex = 0,
    transactionHash = "",
    instructionIndex = 0,
    innerInstructionIndex = null
)