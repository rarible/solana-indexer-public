package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.converter.SolanaAuctionHouseOrderActivityConverter
import com.rarible.protocol.solana.common.converter.SolanaBalanceActivityConverter
import com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.records.SolanaBaseLogRecord
import com.rarible.protocol.solana.common.repository.SolanaAuctionHouseOrderRecordsRepository
import com.rarible.protocol.solana.common.repository.SolanaBalanceRecordsRepository
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByUserDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class ActivityApiService(
    private val balanceRecordsRepository: SolanaBalanceRecordsRepository,
    private val orderRecordsRepository: SolanaAuctionHouseOrderRecordsRepository,
    private val balanceActivityConverter: SolanaBalanceActivityConverter,
    private val orderActivityConverter: SolanaAuctionHouseOrderActivityConverter
) {

    suspend fun getAllActivities(
        filter: ActivityFilterAllDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ): List<ActivityDto> {
        val activityTypes = filter.types.map { convert(it) }

        val balanceCriteria = makeBalanceCriteria(types = activityTypes, mint = null, continuation = continuation)
        val balanceActivitiesDto = if (balanceCriteria !== null) {
            balanceRecordsRepository
                .findBy(balanceCriteria, size, sort)
                .mapNotNull { balanceActivityConverter.convert(it, false) }
        } else {
            emptyFlow()
        }

        val orderCriteria = makeOrderCriteria(types = activityTypes, mint = null, continuation = continuation)
        val orderActivitiesDto = if (orderCriteria !== null) {
            val records = orderRecordsRepository.findBy(orderCriteria, size, sort)
            orderActivityConverter.convert(records, false)
        } else {
            emptyFlow()
        }

        return balanceActivitiesDto.toList() + orderActivitiesDto.toList()
    }

    suspend fun getActivitiesByItem(
        filter: ActivityFilterByItemDto,
        continuation: DateIdContinuation?,
        size: Int,
        sortAscending: Boolean,
    ): List<ActivityDto> {
        val activityTypes = filter.types.map { convert(it) }

        val balanceCriteria = makeBalanceCriteria(activityTypes, filter.itemId, continuation)
        val balanceActivitiesDto = if (balanceCriteria !== null) {
            val records = balanceRecordsRepository.findBy(balanceCriteria, size, sortAscending)
            records.mapNotNull { balanceActivityConverter.convert(it, false) }
        } else {
            emptyFlow()
        }

        val orderCriteria = makeOrderCriteria(activityTypes, filter.itemId, continuation)
        val orderActivitiesDto = if (orderCriteria !== null) {
            val records = orderRecordsRepository.findBy(orderCriteria, size, sortAscending)
            orderActivityConverter.convert(records, false)
        } else {
            emptyFlow()
        }

        return balanceActivitiesDto.toList() + orderActivitiesDto.toList()
    }

    suspend fun getActivitiesByCollection(
        filter: ActivityFilterByCollectionDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ) = emptyList<ActivityDto>() // TODO: not implemented yet.

    suspend fun getActivitiesByUser(
        filter: ActivityFilterByUserDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ) = emptyList<ActivityDto>() // TODO: not implemented yet.

    private fun makeBalanceCriteria(
        types: Collection<ActivityTypeDto>,
        mint: String?,
        continuation: DateIdContinuation?,
    ): Criteria? {
        val balanceRecordClasses = types.mapNotNull { getBalanceRecordClassByActivityType(it) }
        if (balanceRecordClasses.isEmpty()) {
            return null
        }
        return Criteria("_class").`in`(balanceRecordClasses)
            .let { if (mint != null) it.and("mint").isEqualTo(mint) else it }
            .addContinuation(continuation)
    }

    private fun makeOrderCriteria(
        types: Collection<ActivityTypeDto>,
        mint: String?,
        continuation: DateIdContinuation?,
    ): Criteria? {
        val orderRecordClasses = types.mapNotNull { getOrderRecordClassByActivityType(it) }
        if (orderRecordClasses.isEmpty()) {
            return null
        }
        return Criteria("_class").`in`(orderRecordClasses)
            .let { if (mint != null) it.and("mint").isEqualTo(mint) else it }
            .addContinuation(continuation)
    }

    private fun Criteria.addContinuation(continuation: DateIdContinuation?) = this.apply {
        continuation ?: return@apply
        if (continuation.asc) {
            orOperator(
                Criteria("timestamp").isEqualTo(continuation.date).and("_id").gt(continuation.id),
                Criteria("timestamp").gt(continuation.date)
            )
        } else {
            orOperator(
                Criteria("timestamp").isEqualTo(continuation.date).and("_id").lt(continuation.id),
                Criteria("timestamp").lt(continuation.date)
            )
        }
    }

    private fun convert(type: ActivityFilterByItemTypeDto): ActivityTypeDto {
        return when (type) {
            ActivityFilterByItemTypeDto.TRANSFER -> ActivityTypeDto.TRANSFER
            ActivityFilterByItemTypeDto.MINT -> ActivityTypeDto.MINT
            ActivityFilterByItemTypeDto.BURN -> ActivityTypeDto.BURN
            ActivityFilterByItemTypeDto.BID -> ActivityTypeDto.BID
            ActivityFilterByItemTypeDto.LIST -> ActivityTypeDto.LIST
            ActivityFilterByItemTypeDto.SELL -> ActivityTypeDto.SELL
            ActivityFilterByItemTypeDto.CANCEL_BID -> ActivityTypeDto.CANCEL_BID
            ActivityFilterByItemTypeDto.CANCEL_LIST -> ActivityTypeDto.CANCEL_LIST
        }
    }

    private fun convert(type: ActivityFilterAllTypeDto): ActivityTypeDto {
        return when (type) {
            ActivityFilterAllTypeDto.TRANSFER -> ActivityTypeDto.TRANSFER
            ActivityFilterAllTypeDto.MINT -> ActivityTypeDto.MINT
            ActivityFilterAllTypeDto.BURN -> ActivityTypeDto.BURN
            ActivityFilterAllTypeDto.BID -> ActivityTypeDto.BID
            ActivityFilterAllTypeDto.LIST -> ActivityTypeDto.LIST
            ActivityFilterAllTypeDto.SELL -> ActivityTypeDto.SELL
            ActivityFilterAllTypeDto.CANCEL_BID -> ActivityTypeDto.CANCEL_BID
            ActivityFilterAllTypeDto.CANCEL_LIST -> ActivityTypeDto.CANCEL_LIST
        }
    }

    companion object {

        private fun <T: SolanaBaseLogRecord> jvmClassName(kClass: KClass<T>): String {
            val fullClassName = kClass.qualifiedName!!
            return fullClassName.substringBeforeLast(".") + "\$" + fullClassName.substringAfterLast(".")
        }

        private fun getBalanceRecordClassByActivityType(activityTypeDto: ActivityTypeDto): String? =
            when (activityTypeDto) {
                ActivityTypeDto.MINT -> jvmClassName(SolanaBalanceRecord.MintToRecord::class)
                ActivityTypeDto.BURN -> jvmClassName(SolanaBalanceRecord.BurnRecord::class)
                ActivityTypeDto.TRANSFER -> jvmClassName(SolanaBalanceRecord.TransferIncomeRecord::class)
                // Ignored. Do not use 'else' here.
                ActivityTypeDto.BID -> null
                ActivityTypeDto.LIST -> null
                ActivityTypeDto.SELL -> null
                ActivityTypeDto.CANCEL_LIST -> null
                ActivityTypeDto.CANCEL_BID -> null
                ActivityTypeDto.AUCTION_BID -> null
                ActivityTypeDto.AUCTION_CREATED -> null
                ActivityTypeDto.AUCTION_CANCEL -> null
                ActivityTypeDto.AUCTION_FINISHED -> null
                ActivityTypeDto.AUCTION_STARTED -> null
                ActivityTypeDto.AUCTION_ENDED -> null
            }

        private fun getOrderRecordClassByActivityType(activityTypeDto: ActivityTypeDto): String? =
            when (activityTypeDto) {
                ActivityTypeDto.SELL -> jvmClassName(SolanaAuctionHouseOrderRecord.ExecuteSaleRecord::class)
                ActivityTypeDto.CANCEL_LIST -> jvmClassName(SolanaAuctionHouseOrderRecord.CancelRecord::class)
                ActivityTypeDto.CANCEL_BID -> jvmClassName(SolanaAuctionHouseOrderRecord.CancelRecord::class)
                ActivityTypeDto.LIST -> jvmClassName(SolanaAuctionHouseOrderRecord.SellRecord::class)
                ActivityTypeDto.BID -> jvmClassName(SolanaAuctionHouseOrderRecord.BuyRecord::class)
                // Ignored. Do not use 'else' here.
                ActivityTypeDto.TRANSFER -> null
                ActivityTypeDto.MINT -> null
                ActivityTypeDto.BURN -> null
                ActivityTypeDto.AUCTION_BID -> null
                ActivityTypeDto.AUCTION_CREATED -> null
                ActivityTypeDto.AUCTION_CANCEL -> null
                ActivityTypeDto.AUCTION_FINISHED -> null
                ActivityTypeDto.AUCTION_STARTED -> null
                ActivityTypeDto.AUCTION_ENDED -> null
            }
    }
}
