package com.rarible.protocol.solana.common.converter

import com.rarible.blockchain.scanner.solana.util.toFixedLengthString
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.AssetDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.dto.SolanaSolAssetTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object RecordsAuctionHouseOrderConverter {

    fun convert(flow: Flow<SolanaAuctionHouseOrderRecord>): Flow<ActivityDto> = flow {
        val current: MutableList<SolanaAuctionHouseOrderRecord> = mutableListOf()
        var hash: String? = null

        flow.collect { record ->
            if (hash == record.hash()) {
                current.add(record)
            } else {
                process(current).forEach { emit(it) }
                current.clear()
                current.add(record)
                hash = record.hash()
            }
        }
        process(current).forEach { emit(it) }
    }

    private const val DIGITS = 9

    private fun SolanaAuctionHouseOrderRecord.hash() =
        log.blockNumber.toFixedLengthString(12) + ":" + log.blockHash + ":" +
                log.transactionIndex.toLong().toFixedLengthString(8) + ":" + log.transactionIndex

    private fun process(records: List<SolanaAuctionHouseOrderRecord>) = when {
        records.isEmpty() -> emptyList()
        records.size == 1 -> listOf(convert(records.single()))
        records.any { it is SolanaAuctionHouseOrderRecord.BuyRecord } -> {
            records.find { it is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord && it.direction == OrderDirection.SELL }
                ?.let { listOf(makeMatchActivity(it as SolanaAuctionHouseOrderRecord.ExecuteSaleRecord)) }
                ?: records.map { convert(it) }
        }
        records.any { it is SolanaAuctionHouseOrderRecord.SellRecord } -> {
            records.find { it is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord && it.direction == OrderDirection.BUY }
                ?.let { listOf(makeMatchActivity(it as SolanaAuctionHouseOrderRecord.ExecuteSaleRecord)) }
                ?: records.map { convert(it) }
        }
        else -> {
            records.map { convert(it) }
        }
    }

    private fun convert(record: SolanaAuctionHouseOrderRecord): ActivityDto {
        return when (record) {
            is SolanaAuctionHouseOrderRecord.SellRecord -> makeListActivity(record)
            is SolanaAuctionHouseOrderRecord.BuyRecord -> makeBidActivity(record)
            is SolanaAuctionHouseOrderRecord.CancelRecord -> makeCancelActivity(record)
            is SolanaAuctionHouseOrderRecord.ExecuteSaleRecord -> makeMatchActivity(record)
        }
    }

    private fun makeMatchActivity(record: SolanaAuctionHouseOrderRecord.ExecuteSaleRecord) =
        OrderMatchActivityDto(
            id = record.id,
            date = record.timestamp,
            type = if (record.direction == OrderDirection.SELL) OrderMatchActivityDto.Type.SELL else OrderMatchActivityDto.Type.ACCEPT_BID,
            nft = AssetDto(SolanaNftAssetTypeDto(record.mint), record.amount.toBigDecimal(DIGITS)),
            payment = AssetDto(SolanaSolAssetTypeDto(), record.price.toBigDecimal(DIGITS)),
            buyer = record.buyer,
            seller = record.seller,
            price = record.price.toBigDecimal(DIGITS),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false,
        )

    private fun makeListActivity(record: SolanaAuctionHouseOrderRecord.SellRecord) =
        OrderListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = AssetDto(SolanaNftAssetTypeDto(record.mint), record.amount.toBigDecimal(DIGITS)),
            take = AssetDto(SolanaSolAssetTypeDto(), record.sellPrice.toBigDecimal(DIGITS)),
            price = record.sellPrice.toBigDecimal(),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false,
        )

    private fun makeBidActivity(record: SolanaAuctionHouseOrderRecord.BuyRecord) =
        OrderBidActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = AssetDto(SolanaSolAssetTypeDto(), record.buyPrice.toBigDecimal(DIGITS)),
            take = AssetDto(SolanaNftAssetTypeDto(record.mint), record.amount.toBigDecimal(DIGITS)),
            price = record.buyPrice.toBigDecimal(DIGITS),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false,
        )


    private fun makeCancelActivity(record: SolanaAuctionHouseOrderRecord.CancelRecord) = when (record.direction) {
        OrderDirection.SELL -> OrderCancelListActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = SolanaNftAssetTypeDto(record.mint),
            take = SolanaSolAssetTypeDto(),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false,
        )
        OrderDirection.BUY -> OrderCancelBidActivityDto(
            id = record.id,
            date = record.timestamp,
            hash = record.orderId,
            maker = record.maker,
            make = SolanaSolAssetTypeDto(),
            take = SolanaNftAssetTypeDto(record.mint),
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = false,
        )
    }
}
