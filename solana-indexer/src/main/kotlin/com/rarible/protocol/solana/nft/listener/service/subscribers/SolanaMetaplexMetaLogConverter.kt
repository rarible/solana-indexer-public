package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.MetaplexMetadata
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccount
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.trimEndNulls
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexCreateMetadataAccountRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexSignMetadataRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexUnVerifyCollectionRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexUpdateMetadataRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.MetaplexVerifyCollectionRecord
import com.rarible.protocol.solana.common.records.SolanaMetaRecord.SetAndVerifyMetadataRecord
import java.time.Instant

object SolanaMetaplexMetaLogConverter {
    fun convertCreateMetadataAccount(
        log: SolanaBlockchainLog,
        instruction: MetaplexCreateMetadataAccount,
        dateSeconds: Long
    ): MetaplexCreateMetadataAccountRecord = MetaplexCreateMetadataAccountRecord(
        metaAccount = log.instruction.accounts[0],
        mint = log.instruction.accounts[1],
        meta = instruction.createArgs.metadata.convertMetaplexMetaFields(),
        mutable = instruction.createArgs.mutable,
        log = log.log,
        timestamp = Instant.ofEpochSecond(dateSeconds)
    )

    fun convertUpdateMetadataAccount(
        log: SolanaBlockchainLog,
        instruction: MetaplexUpdateMetadataAccount,
        dateSeconds: Long
    ): MetaplexUpdateMetadataRecord = MetaplexUpdateMetadataRecord(
        metaAccount = log.instruction.accounts[0],
        mint = "", // Will be set in the SolanaRecordsLogEventFilter.
        updatedMeta = instruction.updateArgs.metadata?.convertMetaplexMetaFields(),
        updatedMutable = instruction.updateArgs.mutable,
        updateAuthority = instruction.updateArgs.updateAuthority,
        primarySaleHappened = instruction.updateArgs.primarySaleHappened,
        log = log.log,
        timestamp = Instant.ofEpochSecond(dateSeconds)
    )

    fun convertVerifyCollection(
        log: SolanaBlockchainLog,
        dateSeconds: Long
    ): MetaplexVerifyCollectionRecord = MetaplexVerifyCollectionRecord(
        mint = "", // Will be set in the SolanaRecordsLogEventFilter.
        metaAccount = log.instruction.accounts[0],
        collectionAccount = log.instruction.accounts[4],
        log = log.log,
        timestamp = Instant.ofEpochSecond(dateSeconds)
    )

    fun convertUnVerifyCollection(
        log: SolanaBlockchainLog,
        dateSeconds: Long
    ): MetaplexUnVerifyCollectionRecord = MetaplexUnVerifyCollectionRecord(
        metaAccount = log.instruction.accounts[0],
        mint = "", // Will be set in the SolanaRecordsLogEventFilter.
        unVerifyCollectionAccount = log.instruction.accounts[4],
        log = log.log,
        timestamp = Instant.ofEpochSecond(dateSeconds)
    )

    fun convertSignMetadata(
        log: SolanaBlockchainLog,
        dateSeconds: Long
    ): MetaplexSignMetadataRecord = MetaplexSignMetadataRecord(
        metaAccount = log.instruction.accounts[0],
        creatorAddress = log.instruction.accounts[1],
        mint = "", // Will be set in the SolanaRecordsLogEventFilter.
        log = log.log,
        timestamp = Instant.ofEpochSecond(dateSeconds)
    )

    fun convertSetAndVerifyMetadata(
        log: SolanaBlockchainLog,
        dateSeconds: Long
    ) : SetAndVerifyMetadataRecord = SetAndVerifyMetadataRecord(
        metaAccount = log.instruction.accounts[0],
        mint = "", // Will be set in the SolanaRecordsLogEventFilter.
        collectionMint = log.instruction.accounts[4],
        log = log.log,
        timestamp = Instant.ofEpochSecond(dateSeconds)
    )

    private fun MetaplexMetadata.Data.convertMetaplexMetaFields() = MetaplexMetaFields(
        name = name.trimEndNulls(),
        symbol = symbol.trimEndNulls(),
        uri = uri.trimEndNulls(),
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
}