package com.rarible.protocol.solana.nft.migration.mongock

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.rarible.protocol.solana.common.records.SubscriberGroup
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update


@ChangeLog(order = "00002")
class ChangeLog00002ReworkBalanceRecords {

    @ChangeSet(
        id = "ChangeLog00002ReworkBalanceRecords.remapBalanceAccountAndOwner",
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
                val clazz = it["_class"].asText()

                val update = when {
                    "InitializeBalanceAccountRecord" in clazz -> Update().rename("balanceAccount", "account")
                    "TransferIncomeRecord" in clazz -> Update().rename("owner", "account")
                    "TransferOutcomeRecord" in clazz -> Update().rename("owner", "account")
                    else -> return@collect
                }

                val criteria = Criteria.where("_id").`is`(it["_id"].textValue())

                mongoOperations.updateFirst(
                    Query(criteria),
                    update,
                    SubscriberGroup.BALANCE.collectionName
                ).awaitFirst()
            }
    }
}