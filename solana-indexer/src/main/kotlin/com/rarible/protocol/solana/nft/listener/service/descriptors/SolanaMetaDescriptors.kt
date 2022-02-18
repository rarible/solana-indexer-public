package com.rarible.protocol.solana.nft.listener.service.descriptors

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord

object CreateMetadataDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
    id = "create_metadata",
    groupId = SubscriberGroup.METAPLEX_META.id,
    entityType = SolanaMetaRecord.MetaplexCreateMetadataRecord::class.java,
    collection = SubscriberGroup.METAPLEX_META.collectionName
)

object UpdateMetadataDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
    id = "update_metadata",
    groupId = SubscriberGroup.METAPLEX_META.id,
    entityType = SolanaMetaRecord.MetaplexUpdateMetadataRecord::class.java,
    collection = SubscriberGroup.METAPLEX_META.collectionName
)

object VerifyCollectionDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
    id = "update_metadata",
    groupId = SubscriberGroup.METAPLEX_META.id,
    entityType = SolanaMetaRecord.MetaplexVerifyCollectionRecord::class.java,
    collection = SubscriberGroup.METAPLEX_META.collectionName
)
