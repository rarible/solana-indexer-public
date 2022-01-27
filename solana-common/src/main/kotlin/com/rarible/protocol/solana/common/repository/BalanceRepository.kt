package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.findAllAndRemove
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class BalanceRepository(
    private val mongo: ReactiveMongoOperations
) {

    fun save(balance: Balance): Mono<Balance> {
        return mongo.save(balance)
    }

    suspend fun saveAll(balances: Collection<Balance>): List<Balance> {
        return mongo.insertAll(balances).collectList().awaitFirst()
    }

    suspend fun removeAll(ids: Collection<BalanceId>): List<Balance> {
        val criteria = Criteria.where("_id").inValues(ids)
        return mongo.findAllAndRemove<Balance>(Query.query(criteria)).collectList().awaitFirst()
    }

    fun findById(id: BalanceId): Mono<Balance> {
        return mongo.findById(id)
    }

    suspend fun findAll(ids: Collection<BalanceId>): List<Balance> {
        val criteria = Criteria.where("_id").inValues(ids)
        return mongo.find<Balance>(Query.query(criteria)).collectList().awaitFirst()
    }

    suspend fun search(criteria: Criteria?, size: Int, sort: Sort?): List<Balance> {
        return query(criteria, size, sort)
    }

    suspend fun search(query: Query?): List<Balance> {
        return mongo.query<Balance>()
            .matching(query ?: Query())
            .all()
            .collectList()
            .awaitFirst()
    }

    fun deleteById(id: BalanceId): Mono<Balance> {
        return mongo.findAndRemove(Query(Criteria("_id").isEqualTo(id)), Balance::class.java)
    }

    private suspend fun query(criteria: Criteria?, limit: Int, sort: Sort?): List<Balance> {
        val query = Query.query(criteria ?: Criteria()).with(
            sort ?: Sort.by(
                Sort.Order.desc("date"),
                Sort.Order.desc("_id")
            )
        ).limit(limit)

        return mongo.query<Balance>()
            .matching(query)
            .all()
            .collectList()
            .awaitFirst()
    }
}
