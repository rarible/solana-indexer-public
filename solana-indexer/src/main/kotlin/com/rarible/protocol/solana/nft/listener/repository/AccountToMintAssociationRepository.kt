package com.rarible.protocol.solana.nft.listener.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.nft.listener.model.AccountToMintAssociation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toMono

@Component
@CaptureSpan(type = SpanType.DB)
class AccountToMintAssociationRepository(
    private val mongo: ReactiveMongoOperations
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun findAll(balanceAccounts: Collection<String>): List<AccountToMintAssociation> {
        if (balanceAccounts.isEmpty()) return emptyList()

        val criteria = Criteria.where("_id").inValues(balanceAccounts)
        return mongo.find(Query.query(criteria), AccountToMintAssociation::class.java)
            .collectList().awaitFirst()
    }

    /**
     * Insert all associations to DB. Ideally, you should be sure such associations does NOT exist,
     * in such case operation will take minimal time.
     */
    suspend fun saveAll(associations: Collection<AccountToMintAssociation>) {
        if (associations.isEmpty()) return

        try {
            insertAll(associations)
        } catch (e: DuplicateKeyException) {
            logger.warn("Duplicate account-to-mint association", e)
            val exist = findAll(associations.map { it.balanceAccount }).map { it.balanceAccount }.toSet()
            val toInsert = associations.filter { !exist.contains(it.balanceAccount) }
            insertAll(toInsert)
        }
    }

    private suspend fun insertAll(associations: Collection<AccountToMintAssociation>) {
        mongo.insertAll(associations.toMono(), AccountToMintAssociation::class.java).asFlow().collect()
    }

}
