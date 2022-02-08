package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram
import com.rarible.protocol.solana.common.event.BurnEvent
import com.rarible.protocol.solana.common.event.InitializeMintEvent
import com.rarible.protocol.solana.common.event.MetaplexCreateMetadataEvent
import com.rarible.protocol.solana.common.event.MintEvent
import com.rarible.protocol.solana.common.event.TokenEvent
import com.rarible.protocol.solana.common.event.TransferEvent
import com.rarible.protocol.solana.common.model.MetaplexTokenMeta
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.*
import org.springframework.stereotype.Component

@Component
class TokenEventConverter {
    suspend fun convert(source: SolanaLogRecordEvent): List<TokenEvent> {
        val timestamp = source.record.timestamp
        return when (val record = source.record) {
            is MintToRecord -> listOf(
                MintEvent(
                    token = record.mint,
                    reversed = source.reversed,
                    amount = record.mintAmount,
                    log = source.record.log,
                    timestamp = timestamp
                )
            )
            is BurnRecord -> listOf(
                BurnEvent(
                    reversed = source.reversed,
                    token = record.mint,
                    amount = record.burnAmount,
                    log = source.record.log,
                    timestamp = timestamp
                )
            )
            is TransferRecord -> listOf(
                TransferEvent(
                    reversed = source.reversed,
                    token = record.mint,
                    from = record.from,
                    to = record.to,
                    amount = record.amount,
                    log = source.record.log,
                    timestamp = timestamp
                )
            )
            is InitializeMintRecord -> listOf(
                InitializeMintEvent(
                    log = source.record.log,
                    reversed = source.reversed,
                    token = record.mint,
                    timestamp = timestamp
                )
            )
            is MetaplexCreateMetadataRecord -> {
                val rawData = record.data
                listOf(
                    MetaplexCreateMetadataEvent(
                        log = source.record.log,
                        reversed = source.reversed,
                        token = record.mint,
                        timestamp = timestamp,
                        metadata = rawData.convert()
                    )
                )
            }
            is InitializeAccountRecord -> emptyList()
        }
    }

    private fun MetaplexMetadataProgram.Data.convert() = MetaplexTokenMeta(
        name = name,
        symbol = symbol,
        uri = uri,
        sellerFeeBasisPoints = sellerFeeBasisPoints.toInt(),
        creators = creators.orEmpty().map {
            MetaplexTokenCreator(
                address = it.address,
                share = it.share.toInt()
            )
        },
        collection = collection?.let {
            MetaplexTokenMeta.Collection(
                address = it.key,
                verified = it.verified
            )
        },
        mutable = mutable
    )

}
