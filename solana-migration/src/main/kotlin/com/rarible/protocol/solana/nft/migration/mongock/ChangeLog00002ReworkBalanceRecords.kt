package com.rarible.protocol.solana.nft.migration.mongock

import com.fasterxml.jackson.databind.ObjectMapper
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

@ChangeLog(order = "00002")
class ChangeLog00002CreateIndices {

    @ChangeSet(
        id = "ChangeLog00002CreateIndices.reworkBalanceRecords",
        order = "00002",
        author = "protocol",
        runAlways = true
    )
    fun reworkBalanceRecords(
        mongoOperations: ReactiveMongoOperations
    ) = runBlocking {
        val mapper = ObjectMapper()

        mongoOperations.find(Query(), ObjectNode::class.java, SubscriberGroup.BALANCE.collectionName)
            .asFlow()
            .collect {
                val clazz = it["_class"].asText()

                when {
                    "InitializeBalanceAccountRecord" in clazz -> it.replaceTextNode("balanceAccount", "account")
                    "TransferIncomeRecord" in clazz -> it.replaceTextNode("owner", "account")
                    "TransferOutcomeRecord" in clazz -> it.replaceTextNode("owner", "account")
                    else -> return@collect
                }

                val criteria = Criteria.where("_id").`is`(it["_id"].textValue())

                mongoOperations.remove(
                    Query(criteria),
                    SubscriberGroup.BALANCE.collectionName
                ).awaitFirst()

                mongoOperations.insert(mapper.writeValueAsString(it), SubscriberGroup.BALANCE.collectionName).awaitFirst()
            }
    }

    private fun ObjectNode.replaceTextNode(src: String, dst: String) {
        val old = remove(src).textValue()

        check(old.isNotBlank())

        put(dst, old)
    }
}