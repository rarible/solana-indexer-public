package com.rarible.protocol.solana.nft.listener.service.descriptors

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

object SolanaProgramId {
    const val SPL_TOKEN_PROGRAM = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
}

object InitializeMintDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = InitializeMintRecord::class.java
)

object InitializeAccountDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = InitializeAccountRecord::class.java
)

object MintToDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = MintToRecord::class.java
)

object BurnDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = BurnRecord::class.java
)

object TransferDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = TransferRecord::class.java
)

@Component
class CreateMetadataDescriptor(properties: SolanaIndexerProperties) : SolanaDescriptor(
    properties.metadataProgramId,
    SubscriberGroups.SPL_TOKEN,
    entityType = CreateMetadataRecord::class.java
)
