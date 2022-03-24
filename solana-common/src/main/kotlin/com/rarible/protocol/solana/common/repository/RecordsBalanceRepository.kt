package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.solana.protocol.dto.ActivitySortDto
import com.rarible.solana.protocol.dto.ActivityTypeDto
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

@Component
class RecordsBalanceRepository(
    private val mongo: ReactiveMongoOperations,
) {

    suspend fun save(record: SolanaBalanceRecord): SolanaBalanceRecord =
        mongo.save(record, COLLECTION).awaitSingle()

    fun findAll() = mongo.findAll(SolanaBalanceRecord::class.java, COLLECTION).asFlow()

    fun findBy(query: Query): Flow<SolanaBalanceRecord> =
        mongo.find(query, SolanaBalanceRecord::class.java, COLLECTION).asFlow()

    fun findByItem(
        type: Collection<ActivityTypeDto>,
        tokenAddress: String,
        continuation: String? = null,
        size: Int? = null,
        sort: ActivitySortDto = ActivitySortDto.LATEST_FIRST,
    ): Flow<SolanaBalanceRecord> {
        val criteria = Criteria.where("mint").isEqualTo(tokenAddress)

        if (type.isNotEmpty()) {
            val types = type.joinToString("|") {
                when (it) {
                    ActivityTypeDto.MINT -> "MintToRecord"
                    ActivityTypeDto.BURN -> "BurnRecord"
                    ActivityTypeDto.TRANSFER -> "TransferIncomeRecord"
                }
            }
            criteria.and("_class").regex(".*($types)")
        } else {
            return emptyFlow()
        }

        if (continuation != null) {
            if (sort == ActivitySortDto.LATEST_FIRST) {
                criteria.and("_id").lt(continuation)
            } else {
                criteria.and("_id").gt(continuation)
            }
        }

        val query = Query(criteria)

        if (size != null) {
            query.limit(size)
        }

        if (sort == ActivitySortDto.LATEST_FIRST) {
            query.with(Sort.by("_id").descending())
        } else {
            query.with(Sort.by("_id").ascending())
        }

        return mongo.find(query, SolanaBalanceRecord::class.java, COLLECTION).asFlow()
    }

    companion object {
        private const val COLLECTION = "records-balance"
    }
}
