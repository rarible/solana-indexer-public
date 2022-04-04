package com.rarible.protocol.solana.nft.migration.mongock

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.rarible.protocol.solana.common.records.SubscriberGroup
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query

@ChangeLog(order = "00002")
class ChangeLog00002CreateIndices {

    @ChangeSet(
        id = "ChangeLog00002CreateIndices.reworkBalanceRecords",
        order = "00002",
        author = "protocol",
        runAlways = false
    )
    fun reworkBalanceRecords(
        mongoOperations: ReactiveMongoOperations
    ) = runBlocking {
        mongoOperations.find(Query(), ObjectNode::class.java, SubscriberGroup.BALANCE.collectionName)
            .asFlow()
            .collect {
                val balanceAccount = it["balanceAccount"].asText()

                if (balanceAccount != null) {
                    mongoOperations.remove(it)
                    it.remove("balanceAccount")
                    it.put("account", balanceAccount)
                    mongoOperations.insert(it, SubscriberGroup.BALANCE.collectionName)
                } else {
                    val owner = it["owner"].asText()

                    if (owner != null) {
                        mongoOperations.remove(it)
                        it.remove("owner")
                        it.put("account", owner)
                        mongoOperations.insert(it, SubscriberGroup.BALANCE.collectionName)
                    }
                }
            }
    }
}