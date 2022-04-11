package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.MetaplexMetadata
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccount
import com.rarible.protocol.solana.borsh.SetAndVerifyCollection
import com.rarible.protocol.solana.borsh.SignMetadata
import com.rarible.protocol.solana.borsh.UnVerifyCollection
import com.rarible.protocol.solana.borsh.VerifyCollection
import com.rarible.protocol.solana.borsh.parseMetaplexMetadataInstruction
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.records.SolanaMetaRecord
import com.rarible.protocol.solana.common.records.SubscriberGroup
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CreateMetaplexMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_create",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = SolanaMetaRecord.MetaplexCreateMetadataAccountRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val data = log.instruction.data
        val metaplexCreateMetadataAccount =
            data.parseMetaplexMetadataInstruction() as? MetaplexCreateMetadataAccount ?: return emptyList()
        val createArgs = metaplexCreateMetadataAccount.createArgs
        val record = SolanaMetaRecord.MetaplexCreateMetadataAccountRecord(
            metaAccount = log.instruction.accounts[0],
            mint = log.instruction.accounts[1],
            meta = createArgs.metadata.convertExecuteSale(),
            mutable = createArgs.mutable,
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
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
        entityType = SolanaMetaRecord.MetaplexUpdateMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val data = log.instruction.data
        val metaplexUpdateMetadataAccount =
            data.parseMetaplexMetadataInstruction() as? MetaplexUpdateMetadataAccount ?: return emptyList()
        val updateArgs = metaplexUpdateMetadataAccount.updateArgs
        val record = SolanaMetaRecord.MetaplexUpdateMetadataRecord(
            metaAccount = log.instruction.accounts[0],
            mint = log.instruction.accounts[1],
            updatedMeta = updateArgs.metadata?.convertExecuteSale(),
            updatedMutable = updateArgs.mutable,
            updateAuthority = updateArgs.updateAuthority,
            primarySaleHappened = updateArgs.primarySaleHappened,
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
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
        entityType = SolanaMetaRecord.MetaplexVerifyCollectionRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val instruction = log.instruction

        if (instruction.data.parseMetaplexMetadataInstruction() !is VerifyCollection) return emptyList()

        val record = SolanaMetaRecord.MetaplexVerifyCollectionRecord(
            metaAccount = instruction.accounts[0],
            collectionAccount = instruction.accounts[4],
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
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
        entityType = SolanaMetaRecord.MetaplexUnVerifyCollectionRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val instruction = log.instruction

        if (instruction.data.parseMetaplexMetadataInstruction() !is UnVerifyCollection) return emptyList()

        val record = SolanaMetaRecord.MetaplexUnVerifyCollectionRecord(
            metaAccount = instruction.accounts[0],
            unVerifyCollectionAccount = instruction.accounts[4],
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
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
        entityType = SolanaMetaRecord.MetaplexSignMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val instruction = log.instruction

        if (instruction.data.parseMetaplexMetadataInstruction() !is SignMetadata) return emptyList()

        val record = SolanaMetaRecord.MetaplexSignMetadataRecord(
            metaAccount = instruction.accounts[0],
            creatorAddress = instruction.accounts[1],
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
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
        entityType = SolanaMetaRecord.MetaplexSignMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val instruction = log.instruction

        if (instruction.data.parseMetaplexMetadataInstruction() !is SetAndVerifyCollection) return emptyList()

        val record = SolanaMetaRecord.SetAndVerifyMetadataRecord(
            metaAccount = instruction.accounts[0],
            mint = instruction.accounts[4],
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )

        return listOf(record)
    }
}

private fun MetaplexMetadata.Data.convertExecuteSale() = MetaplexMetaFields(
    name = name,
    symbol = symbol,
    uri = uri,
    sellerFeeBasisPoints = sellerFeeBasisPoints.toInt(),
    creators = creators.orEmpty().map {
        MetaplexTokenCreator(
            address = it.address,
            share = it.share.toInt(),
            verified = it.verified
        )
    },
    collection = collection?.let {
        MetaplexMetaFields.Collection(
            address = it.key,
            verified = it.verified
        )
    }
)
