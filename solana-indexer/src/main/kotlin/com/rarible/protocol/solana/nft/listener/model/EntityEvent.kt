package com.rarible.protocol.solana.nft.listener.model

import com.rarible.blockchain.scanner.solana.model.SolanaLog

interface EntityEvent {
    val log: SolanaLog
    val reversed: Boolean
}