package com.rarible.protocol.solana.nft.listener.service

import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.model.ItemId
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ItemRepository(
    private val mongo: ReactiveMongoOperations
) {
    fun save(item: Item): Mono<Item> {
        return mongo.save(item)
    }

    fun findById(id: ItemId): Mono<Item> {
        return mongo.findById(id)
    }

    suspend fun search(query: Query): List<Item> {
        return mongo.query<Item>().matching(query)
            .all()
            .collectList()
            .awaitFirst()
    }
}
