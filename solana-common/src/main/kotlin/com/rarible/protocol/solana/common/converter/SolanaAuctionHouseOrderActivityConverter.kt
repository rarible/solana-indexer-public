package com.rarible.protocol.solana.common.converter

import com.rarible.blockchain.scanner.solana.util.toFixedLengthString
import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.service.PriceNormalizer
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.dto.SolanaSolAssetTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.stereotype.Component

@Component
class SolanaAuctionHouseOrderActivityConverter(
    private val assetConverter: AssetConverter,
    private val priceNormalizer: PriceNormalizer
) {

    fun convert(flow: Flow<SolanaAuctionHouseOrderRecord>, reverted: Boolean): Flow<ActivityDto> = flow {
        val current: MutableList<SolanaAuctionHouseOrderRecord> = mutableListOf()
        var hash: String? = null

        flow.collect { record ->
            if (hash == record.hash()) {
                current.add(record)
            } else {
                process(current, reverted).forEach { emit(it) }
                current.clear()
                current.add(record)
                hash = record.hash()
            }
        }
        process(current, reverted).forEach { emit(it) }
    }

    private fun SolanaAuctionHouseOrderRecord.hash() =
        log.blockNumber.toFixedLengthString(12) + ":" + log.blockHash + ":" +
                log.transactionIndex.toLong().toFixedLengthString(8) + ":" + log.transactionIndex

    private suspend fun process(records: List<SolanaAuctionHouseOrderRecord>, reverted: Boolean) = when {
        records.isEmpty() -> emptyList()
        records.size == 1 -> listOfNotNull(convert(records.single(), reverted))
        records.any { it is SolanaAuctionHouseOrderRecord.BuyRecord } -> {
            records.find { it is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord && it.direction == OrderDirection.SELL }
                ?.let { listOf(makeMatchActivity(it as SolanaAuctionHouseOrderRecord.ExecuteSaleRecord, reverted)) }
                ?: records.mapNotNull { convert(it, reverted) }
        }
        records.any { it is SolanaAuctionHouseOrderRecord.SellRecord } -> {
            records.find { it is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord && it.direction == OrderDirection.BUY }
                ?.let { listOf(makeMatchActivity(it as SolanaAuctionHouseOrderRecord.ExecuteSaleRecord, reverted)) }
                ?: records.mapNotNull { convert(it, reverted) }
        }
        else -> records.mapNotNull { convert(it, reverted) }
    }

    private suspend fun convert(record: SolanaAuctionHouseOrderRecord, reverted: Boolean): ActivityDto? {
        return when (record) {
            is SolanaAuctionHouseOrderRecord.SellRecord -> makeListActivity(record, reverted)
            is SolanaAuctionHouseOrderRecord.BuyRecord -> makeBidActivity(record, reverted)
            is SolanaAuctionHouseOrderRecord.CancelRecord -> makeCancelActivity(record, reverted)
            is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> makeMatchActivity(record, reverted)
            // Should be never found in DB
            is SolanaAuctionHouseOrderRecord.InternalOrderUpdateRecord -> null
        }
    }

    private suspend fun makeMatchActivity(record: SolanaAuctionHouseOrderRecord.ExecuteSaleRecord, reverted: Boolean) =
        OrderMatchActivityDto(
            id = record.id,
            date = record.timestamp,
            type = when (record.direction) {
                OrderDirection.SELL -> OrderMatchActivityDto.Type.SELL
                OrderDirection.BUY -> OrderMatchActivityDto.Type.ACCEPT_BID
            },
            nft = assetConverter.convert(Asset(TokenNftAssetType(record.mint), record.amount)),
            payment = assetConverter.convert(Asset(WrappedSolAssetType(), record.price)),
            buyer = record.buyer,
            seller = record.seller,
            price = priceNormalizer.normalize(Asset(WrappedSolAssetType(), record.price)),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )

    private suspend fun makeListActivity(record: SolanaAuctionHouseOrderRecord.SellRecord, reverted: Boolean) =
        OrderListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = assetConverter.convert(Asset(TokenNftAssetType(record.mint), record.amount)),
            take = assetConverter.convert(Asset(WrappedSolAssetType(), record.sellPrice)),
            price = priceNormalizer.normalize(WrappedSolAssetType(), record.sellPrice),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )

    private suspend fun makeBidActivity(record: SolanaAuctionHouseOrderRecord.BuyRecord, reverted: Boolean) =
        OrderBidActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = assetConverter.convert(Asset(WrappedSolAssetType(), record.buyPrice)),
            take = assetConverter.convert(Asset(TokenNftAssetType(record.mint), record.amount)),
            price = priceNormalizer.normalize(WrappedSolAssetType(), record.buyPrice),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )

    private fun makeCancelActivity(
        record: SolanaAuctionHouseOrderRecord.CancelRecord,
        reverted: Boolean
    ) = when (record.direction) {
        OrderDirection.SELL -> OrderCancelListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = SolanaNftAssetTypeDto(record.mint),
            take = SolanaSolAssetTypeDto(),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )
        OrderDirection.BUY -> OrderCancelBidActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = SolanaSolAssetTypeDto(),
            take = SolanaNftAssetTypeDto(record.mint),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
        )
    }
}
