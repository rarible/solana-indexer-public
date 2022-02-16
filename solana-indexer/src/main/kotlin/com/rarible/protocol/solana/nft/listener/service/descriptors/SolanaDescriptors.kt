package com.rarible.protocol.solana.nft.listener.service.descriptors

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord

object SolanaProgramId {
    const val SPL_TOKEN_PROGRAM = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TOKEN_METADATA_PROGRAM = "metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s"
}

object InitializeMintDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaTokenRecord.InitializeMintRecord::class.java
)

object InitializeAccountDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaTokenRecord.InitializeAccountRecord::class.java
)

object MintToTokenDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaTokenRecord.MintToRecord::class.java
)

object MintToBalanceDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaBalanceRecord.MintToRecord::class.java
)

object BurnTokenDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaTokenRecord.BurnRecord::class.java
)

object BurnBalanceDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaBalanceRecord.BurnRecord::class.java
)

object TransferIncomeBalanceDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaBalanceRecord.TransferIncomeRecord::class.java
)

object TransferOutcomeBalanceDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    SubscriberGroups.SPL_TOKEN,
    entityType = SolanaBalanceRecord.TransferOutcomeRecord::class.java
)

object CreateMetadataDescriptor : SolanaDescriptor(
    SolanaProgramId.TOKEN_METADATA_PROGRAM,
    SubscriberGroups.METAPLEX_META,
    entityType = SolanaMetaRecord.MetaplexCreateMetadataRecord::class.java
)

object UpdateMetadataDescriptor : SolanaDescriptor(
    SolanaProgramId.TOKEN_METADATA_PROGRAM,
    SubscriberGroups.METAPLEX_META,
    entityType = SolanaMetaRecord.MetaplexUpdateMetadataRecord::class.java
)

object VerifyCollectionDescriptor : SolanaDescriptor(
    SolanaProgramId.TOKEN_METADATA_PROGRAM,
    SubscriberGroups.METAPLEX_META,
    entityType = SolanaMetaRecord.MetaplexVerifyCollectionRecord::class.java
)