package com.rarible.protocol.solana.common.repository

import com.rarible.protocol.solana.common.model.Escrow
import com.rarible.protocol.solana.common.model.EscrowId
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.stereotype.Component

@Component
class EscrowRepository(
    private val mongo: ReactiveMongoOperations
) {
    suspend fun save(escrow: Escrow): Escrow =
        mongo.save(escrow).awaitFirst()

    suspend fun findByAccount(account: EscrowId): Escrow? =
        mongo.findById<Escrow>(account).awaitFirstOrNull()
}