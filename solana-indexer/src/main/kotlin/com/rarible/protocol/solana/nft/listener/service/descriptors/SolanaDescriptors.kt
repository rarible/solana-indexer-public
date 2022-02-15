package com.rarible.protocol.solana.nft.listener.service.descriptors

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.MetaplexCreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBaseLogRecord.TransferRecord

object SolanaProgramId {
    const val SPL_TOKEN_PROGRAM = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TOKEN_METADATA_PROGRAM = "metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s"
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

object CreateMetadataDescriptor : SolanaDescriptor(
    SolanaProgramId.TOKEN_METADATA_PROGRAM,
    SubscriberGroups.METAPLEX_META,
    entityType = MetaplexCreateMetadataRecord::class.java
)
