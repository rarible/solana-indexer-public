package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor

class SolanaDescriptor(
    programId: String,
    id: String,
    groupId: String,
    entityType: Class<*>,
    collection: String
) : SolanaDescriptor(
    programId,
    id,
    groupId,
    entityType,
    collection
)
