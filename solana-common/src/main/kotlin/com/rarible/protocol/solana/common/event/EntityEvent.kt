package com.rarible.protocol.solana.common.event

import com.rarible.blockchain.scanner.solana.model.SolanaLog

interface EntityEvent {
    val log: SolanaLog
    val reversed: Boolean
}
