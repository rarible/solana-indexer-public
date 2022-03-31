package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.converter.ActivityConverter
import com.rarible.protocol.solana.common.repository.RecordsBalanceRepository
import com.rarible.protocol.solana.common.repository.RecordsOrderRepository
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionDto
import com.rarible.protocol.solana.dto.ActivityFilterByCollectionTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByUserDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import com.rarible.protocol.solana.nft.api.converter.RecordsAuctionHouseOrderConverter
import com.rarible.protocol.solana.nft.api.converter.RecordsBalanceConverter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component

@Component
class ActivityApiService(
    private val recordsBalanceRepository: RecordsBalanceRepository,
    private val recordsOrderRepository: RecordsOrderRepository,
//    todo get rid of ActivityConverter's
//    private val activityConverter: ActivityConverter,
) {

    suspend fun getAllActivities(
        filter: ActivityFilterAllDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ): List<ActivityDto> {
        val types = filter.types.map { convert(it) }

        val balanceActivitiesDto = types.intersect(balanceTypes).let {
            if (it.isNotEmpty()) {
                val criteria = makeBalanceCriteria(it, continuation)
                val records = recordsBalanceRepository.findBy(criteria, size, sort)
                RecordsBalanceConverter.convert(records)
            } else {
                emptyFlow()
            }
        }

        val orderActivitiesDto = types.intersect(orderTypes).let {
            if (it.isNotEmpty()) {
                val criteria = makeOrderCriteria(it, continuation)
                val records = recordsOrderRepository.findBy(criteria, size, sort)
                RecordsAuctionHouseOrderConverter.convert(records)
            } else {
                emptyFlow()
            }
        }

        return coroutineScope {
            listOf(
                async { balanceActivitiesDto.take(size).toList() },
                async { orderActivitiesDto.take(size).toList() },
            )
        }.awaitAll().flatten()
    }

    suspend fun getActivitiesByItem(
        filter: ActivityFilterByItemDto,
        continuation: DateIdContinuation?,
        size: Int,
        sort: Boolean,
    ): List<ActivityDto> {
        val types = filter.types.map { convert(it) }

        val balanceActivitiesDto = types.intersect(balanceTypes).let {
            if (it.isNotEmpty()) {
                val criteria = makeBalanceCriteria(it, filter.itemId, continuation)
                val records = recordsBalanceRepository.findBy(criteria, size, sort)
                RecordsBalanceConverter.convert(records)
            } else {
                emptyFlow()
            }
        }

        val orderActivitiesDto = types.intersect(orderTypes).let {
            if (it.isNotEmpty()) {
                val criteria = makeOrderCriteria(it, filter.itemId, continuation)
                val records = recordsOrderRepository.findBy(criteria, size, sort)
                RecordsAuctionHouseOrderConverter.convert(records)
            } else {
                emptyFlow()
            }
        }

        return coroutineScope {
            listOf(
                async { balanceActivitiesDto.take(size).toList() },
                async { orderActivitiesDto.take(size).toList() },
            )
        }.awaitAll().flatten()
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
        Criteria("_class").`in`(types.mapNotNull(Companion.balanceTypeMapping::get))
            .addContinuation(continuation)

    private fun makeOrderCriteria(types: Collection<ActivityTypeDto>, continuation: DateIdContinuation?) =
        Criteria("_class").`in`(types.mapNotNull(Companion.orderTypeMapping::get))
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
        Criteria("_class").`in`(types.mapNotNull(Companion.orderTypeMapping::get))
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

    private fun convert(type: ActivityFilterByCollectionTypeDto): ActivityTypeDto {
        return when (type) {
            ActivityFilterByCollectionTypeDto.TRANSFER -> ActivityTypeDto.TRANSFER
            ActivityFilterByCollectionTypeDto.MINT -> ActivityTypeDto.MINT
            ActivityFilterByCollectionTypeDto.BURN -> ActivityTypeDto.BURN
            ActivityFilterByCollectionTypeDto.BID -> ActivityTypeDto.BID
            ActivityFilterByCollectionTypeDto.LIST -> ActivityTypeDto.LIST
            ActivityFilterByCollectionTypeDto.SELL -> ActivityTypeDto.SELL
            ActivityFilterByCollectionTypeDto.CANCEL_BID -> ActivityTypeDto.CANCEL_BID
            ActivityFilterByCollectionTypeDto.CANCEL_LIST -> ActivityTypeDto.CANCEL_LIST
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

        private const val TRANSFER_OUTCOME_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$TransferOutcomeRecord"


        const val BUY_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord\$BuyRecord"

        const val CANCEL_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord\$CancelRecord"

        const val EXECUTE_SALE_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaAuctionHouseOrderRecord\$ExecuteSaleRecord"

        const val SELL_RECORD =
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
