package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccountArgs
import com.rarible.protocol.solana.borsh.VerifyCollection
import com.rarible.protocol.solana.borsh.parseMetaplexMetadataInstruction
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CreateMetaplexMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_create",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = SolanaMetaRecord.MetaplexCreateMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val data = log.instruction.data
        val metaplexCreateMetadataAccount =
            data.parseMetaplexMetadataInstruction() as? MetaplexCreateMetadataAccount ?: return emptyList()
        val record = SolanaMetaRecord.MetaplexCreateMetadataRecord(
            metaAccount = log.instruction.accounts[0],
            mint = log.instruction.accounts[1],
            data = metaplexCreateMetadataAccount,
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )

        return listOf(record)
    }
}

@Component
class UpdateMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_update",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = SolanaMetaRecord.MetaplexUpdateMetadataRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val data = log.instruction.data
        val metaplexUpdateMetadataAccountArgs =
            data.parseMetaplexMetadataInstruction() as? MetaplexUpdateMetadataAccountArgs ?: return emptyList()
        val record = SolanaMetaRecord.MetaplexUpdateMetadataRecord(
            metaAccount = log.instruction.accounts[0],
            mint = log.instruction.accounts[1],
            newData = metaplexUpdateMetadataAccountArgs,
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )

        return listOf(record)
    }
}

@Component
class VerifyCollectionSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.TOKEN_METADATA_PROGRAM,
        id = "metadata_verify_collection",
        groupId = SubscriberGroup.METAPLEX_META.id,
        entityType = SolanaMetaRecord.MetaplexVerifyCollectionRecord::class.java,
        collection = SubscriberGroup.METAPLEX_META.collectionName
    )

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