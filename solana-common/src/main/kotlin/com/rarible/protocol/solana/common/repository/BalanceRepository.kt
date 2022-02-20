package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceId
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Component

@Component
class BalanceRepository(
    private val mongo: ReactiveMongoOperations
) {

    suspend fun save(balance: Balance): Balance =
        mongo.save(balance).awaitFirst()

    suspend fun findByAccount(account: BalanceId): Balance? =
        mongo.findById<Balance>(account).awaitFirstOrNull()
}
