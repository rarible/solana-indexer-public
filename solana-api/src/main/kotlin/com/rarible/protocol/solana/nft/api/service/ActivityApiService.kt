package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.converter.RecordsAuctionHouseOrderConverter
import com.rarible.protocol.solana.common.converter.SolanaBalanceActivityConverter
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
import com.rarible.protocol.solana.dto.BurnActivityDto
import com.rarible.protocol.solana.dto.MintActivityDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.dto.TransferActivityDto
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component

@Component
class ActivityApiService(
    private val balanceRecordsRepository: SolanaBalanceRecordsRepository,
    private val orderRecordsRepository: SolanaAuctionHouseOrderRecordsRepository,
    private val balanceActivityConverter: SolanaBalanceActivityConverter
) {

    suspend fun getAllActivities(
        filter: ActivityFilterAllDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ): List<ActivityDto> {
        val allTypes = filter.types.map { convert(it) }
        val balanceTypes = allTypes.intersect(balanceTypes)
        val orderTypes = allTypes.intersect(orderTypes)

        val balanceActivitiesDto = if (balanceTypes.isNotEmpty()) {
            val criteria = makeBalanceCriteria(balanceTypes, continuation)
            val records = balanceRecordsRepository.findBy(criteria, size, sort)
            records.mapNotNull { balanceActivityConverter.convert(it, false) }
        } else {
            emptyFlow()
        }

        val orderActivitiesDto = if (orderTypes.isNotEmpty()) {
            val criteria = makeOrderCriteria(orderTypes, continuation)
            val records = orderRecordsRepository.findBy(criteria, size, sort)
            RecordsAuctionHouseOrderConverter.convert(records)
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
        val allTypes = filter.types.map { convert(it) }
        val balanceTypes = allTypes.intersect(balanceTypes)
        val orderTypes = allTypes.intersect(orderTypes)

        val balanceActivitiesDto = if (balanceTypes.isNotEmpty()) {
            val criteria = makeBalanceCriteria(balanceTypes, filter.itemId, continuation)
            val records = balanceRecordsRepository.findBy(criteria, size, sortAscending)
            records.mapNotNull { balanceActivityConverter.convert(it, false) }
        } else {
            emptyFlow()
        }

        val orderActivitiesDto = if (orderTypes.isNotEmpty()) {
            val criteria = makeOrderCriteria(orderTypes, filter.itemId, continuation)
            val records = orderRecordsRepository.findBy(criteria, size, sortAscending)
            RecordsAuctionHouseOrderConverter.convert(records).filter { activityType(it) in orderTypes }
        } else {
            emptyFlow()
        }

        return balanceActivitiesDto.toList() + orderActivitiesDto.toList()
    }

    private fun activityType(activity: ActivityDto) = when (activity) {
        is MintActivityDto -> ActivityTypeDto.MINT
        is BurnActivityDto -> ActivityTypeDto.BURN
        is TransferActivityDto -> ActivityTypeDto.TRANSFER
        is OrderMatchActivityDto -> ActivityTypeDto.SELL
        is OrderListActivityDto -> ActivityTypeDto.LIST
        is OrderCancelListActivityDto -> ActivityTypeDto.CANCEL_LIST
        is OrderBidActivityDto -> ActivityTypeDto.BID
        is OrderCancelBidActivityDto -> ActivityTypeDto.CANCEL_BID
    }


    suspend fun getActivitiesByCollection(
        filter: ActivityFilterByCollectionDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ) = emptyList<ActivityDto>()

    suspend fun getActivitiesByUser(
        filter: ActivityFilterByUserDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ) = emptyList<ActivityDto>()


    private fun makeBalanceCriteria(types: Collection<ActivityTypeDto>, continuation: DateIdContinuation?) =
        Criteria("_class").`in`(types.mapNotNull(balanceTypeMapping::get))
            .addContinuation(continuation)

    private fun makeOrderCriteria(types: Collection<ActivityTypeDto>, continuation: DateIdContinuation?) =
        Criteria("_class").`in`(types.mapNotNull(orderTypeMapping::get))
            .addContinuation(continuation)

    private fun makeBalanceCriteria(
        types: Collection<ActivityTypeDto>,
        itemId: String,
        continuation: DateIdContinuation?,
    ) =
        Criteria("_class").`in`(types.mapNotNull(balanceTypeMapping::get))
            .and("mint").isEqualTo(itemId)
            .addContinuation(continuation)

    private fun makeOrderCriteria(
        types: Collection<ActivityTypeDto>,
        itemId: String,
        continuation: DateIdContinuation?,
    ) =
        Criteria("_class").`in`(types.mapNotNull(orderTypeMapping::get) + EXECUTE_SALE_RECORD)
            .and("mint").isEqualTo(itemId)
            .addContinuation(continuation)

    private fun Criteria.addContinuation(continuation: DateIdContinuation?) = this.apply {
        if (continuation != null) {
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

        private const val MINT_TO_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$MintToRecord"

        private const val BURN_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$BurnRecord"

        private const val TRANSFER_INCOME_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$TransferIncomeRecord"

        private const val BUY_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord\$BuyRecord"

        private const val CANCEL_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord\$CancelRecord"

        private const val EXECUTE_SALE_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord\$ExecuteSaleRecord"

        private const val SELL_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord\$SellRecord"


        private val balanceTypes = setOf(
            ActivityTypeDto.MINT,
            ActivityTypeDto.BURN,
            ActivityTypeDto.TRANSFER,
        )

        private val orderTypes = setOf(
            ActivityTypeDto.LIST,
            ActivityTypeDto.CANCEL_LIST,
            ActivityTypeDto.BID,
            ActivityTypeDto.CANCEL_BID,
            ActivityTypeDto.SELL,
        )

        private val balanceTypeMapping = mapOf(
            ActivityTypeDto.MINT to MINT_TO_RECORD,
            ActivityTypeDto.BURN to BURN_RECORD,
            ActivityTypeDto.TRANSFER to TRANSFER_INCOME_RECORD,
        )

        private val orderTypeMapping = mapOf(
            ActivityTypeDto.SELL to SELL_RECORD,
            ActivityTypeDto.LIST to BUY_RECORD,
            ActivityTypeDto.CANCEL_LIST to CANCEL_RECORD,
        )
    }
}
