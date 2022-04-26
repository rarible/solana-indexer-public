package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccount
import com.rarible.protocol.solana.borsh.SetAndVerifyCollection
import com.rarible.protocol.solana.borsh.SignMetadata
import com.rarible.protocol.solana.borsh.UnVerifyCollection
import com.rarible.protocol.solana.borsh.VerifyCollection
import com.rarible.protocol.solana.borsh.parseMetaplexMetadataInstruction
import com.rarible.protocol.solana.common.pubkey.SolanaProgramId
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexCreateMetadataAccountRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexSignMetadataRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexUnVerifyCollectionRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexUpdateMetadataRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexVerifyCollectionRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.SetAndVerifyMetadataRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import org.springframework.stereotype.Component

@Component
class CreateMetaplexMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_create",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = MetaplexCreateMetadataAccountRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<MetaplexCreateMetadataAccountRecord> {
        val data = log.instruction.data
        val metaplexCreateMetadataAccount =
            data.parseMetaplexMetadataInstruction() as? MetaplexCreateMetadataAccount ?: return emptyList()
        val record = SolanaMetaplexMetaLogConverter.convertCreateMetadataAccount(
            log,
            metaplexCreateMetadataAccount,
            block.timestamp
        )

        return listOf(record)
    }
}

@Component
class UpdateMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_update",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = MetaplexUpdateMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<MetaplexUpdateMetadataRecord> {
        val data = log.instruction.data
        val metaplexUpdateMetadataAccount =
            data.parseMetaplexMetadataInstruction() as? MetaplexUpdateMetadataAccount ?: return emptyList()
        val record = SolanaMetaplexMetaLogConverter.convertUpdateMetadataAccount(
            log,
            metaplexUpdateMetadataAccount,
            block.timestamp
        )

        return listOf(record)
    }
}

@Component
class VerifyCollectionSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_verify_collection",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = MetaplexVerifyCollectionRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<MetaplexVerifyCollectionRecord> {
        if (log.instruction.data.parseMetaplexMetadataInstruction() !is VerifyCollection) return emptyList()

        val record = SolanaMetaplexMetaLogConverter.convertVerifyCollection(
            log,
            block.timestamp
        )
        return listOf(record)
    }
}

@Component
class UnVerifyCollectionSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_un_verify_collection",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = MetaplexUnVerifyCollectionRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<MetaplexUnVerifyCollectionRecord> {
        if (log.instruction.data.parseMetaplexMetadataInstruction() !is UnVerifyCollection) return emptyList()

        val record = SolanaMetaplexMetaLogConverter.convertUnVerifyCollection(
            log,
            block.timestamp
        )
        return listOf(record)
    }
}

@Component
class SignMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_sign_collection",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = MetaplexSignMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<MetaplexSignMetadataRecord> {
        if (log.instruction.data.parseMetaplexMetadataInstruction() !is SignMetadata) return emptyList()

        val record = SolanaMetaplexMetaLogConverter.convertSignMetadata(
            log,
            block.timestamp
        )
        return listOf(record)
    }
}


@Component
class SetAndVerifyMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_set_and_verify_collection",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = MetaplexSignMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SetAndVerifyMetadataRecord> {
        if (log.instruction.data.parseMetaplexMetadataInstruction() !is SetAndVerifyCollection) return emptyList()

        val record = SolanaMetaplexMetaLogConverter.convertSetAndVerifyMetadata(
            log,
            block.timestamp
        )

        return listOf(record)
    }
}