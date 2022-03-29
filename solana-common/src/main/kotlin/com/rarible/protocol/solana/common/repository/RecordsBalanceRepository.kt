package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.dto.ActivitySortDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RecordsBalanceRepository(
    private val mongo: ReactiveMongoOperations,
) {

    suspend fun save(record: SolanaBalanceRecord): SolanaBalanceRecord =
        mongo.save(record, COLLECTION).awaitSingle()

    fun findAll(
        type: List<ActivityTypeDto>,
        continuation: DateIdContinuation?,
        size: Int,
        sort: ActivitySortDto,
    ): Flow<SolanaBalanceRecord> {
        if (type.isEmpty()) return emptyFlow()

        val criteria = Criteria()
            .addType(type)
            .addContinuation(continuation, sort)

        val query = Query(criteria)
            .limit(size)
            .sortBy(sort)

        return mongo.find(query, SolanaBalanceRecord::class.java, COLLECTION).asFlow()
    }

    fun findByItem(
        type: Collection<ActivityTypeDto>,
        tokenAddress: String,
        continuation: DateIdContinuation?,
        size: Int,
        sort: ActivitySortDto,
    ): Flow<SolanaBalanceRecord> {
        if (type.isEmpty()) return emptyFlow()

        val criteria = Criteria.where("mint").isEqualTo(tokenAddress)
            .addType(type)
            .addContinuation(continuation, sort)

        val query = Query(criteria)
            .limit(size)
            .sortBy(sort)

        return mongo.find(query, SolanaBalanceRecord::class.java, COLLECTION).asFlow()
    }

    fun findByCollection(
        type: List<ActivityTypeDto>,
        collection: String,
        continuation: DateIdContinuation?,
        size: Int?,
        sort: ActivitySortDto,
    ): Flow<SolanaBalanceRecord> {
        return emptyFlow()
    }

    private fun Criteria.addType(type: Collection<ActivityTypeDto>) =
        and("_class").`in`(type.mapNotNull {
            // TODO ATM we support only these 3 types of activity
            when (it) {
                ActivityTypeDto.MINT -> MINT_TO_RECORD
                ActivityTypeDto.BURN -> BURN_RECORD
                ActivityTypeDto.TRANSFER -> TRANSFER_INCOME_RECORD
                else -> null
            }
        })

    private fun Criteria.addContinuation(continuation: DateIdContinuation?, sort: ActivitySortDto) =
        continuation?.let {
            if (sort == ActivitySortDto.LATEST_FIRST) {
                orOperator(
                    Criteria("timestamp").isEqualTo(continuation.date).and("_id").lt(continuation.id),
                    Criteria("timestamp").lt(continuation.date)
                )
            } else {
                orOperator(
                    Criteria("timestamp").isEqualTo(continuation.date).and("_id").gt(continuation.id),
                    Criteria("timestamp").gt(continuation.date)
                )
            }
        } ?: this

    private fun Query.sortBy(sort: ActivitySortDto) = this.with(
        if (sort == ActivitySortDto.LATEST_FIRST) {
            Sort.by("timestamp", "_id").descending()
        } else {
            Sort.by("timestamp", "_id").ascending()
        }
    )

    companion object {

        private const val COLLECTION = "records-balance"

        private const val MINT_TO_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$MintToRecord"

        private const val BURN_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$BurnRecord"

        private const val TRANSFER_INCOME_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$TransferIncomeRecord"

        private const val TRANSFER_OUTCOME_RECORD =
            "com.rarible.protocol.solana.common.records.SolanaBalanceRecord\$TransferOutcomeRecord"

        fun parseContinuation(continuation: String?) = continuation
            ?.split("_", limit = 2)
            ?.let { (t, k) -> Instant.ofEpochMilli(t.toLong()) to k }
    }
}
