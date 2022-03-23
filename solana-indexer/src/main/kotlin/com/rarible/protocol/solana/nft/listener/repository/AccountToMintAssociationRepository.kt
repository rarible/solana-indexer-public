package com.rarible.protocol.solana.nft.listener.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.core.common.optimisticLock
import com.rarible.protocol.solana.nft.listener.model.AccountToMintAssociation
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    suspend fun findAll(accounts: Collection<String>, batchCount: Int): List<AccountToMintAssociation> {
        if (batchCount == 1) return findAll(accounts)

        val batchSize = (accounts.size / batchCount) + 1
        return coroutineScope {
            accounts.chunked(batchSize).map {
                async { findAll(it) }
            }.awaitAll().flatten()
        }
    }

    private suspend fun findAll(accounts: Collection<String>): List<AccountToMintAssociation> {
        if (accounts.isEmpty()) return emptyList()

        val criteria = Criteria.where("_id").inValues(accounts)
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
            try {
                optimisticLock {
                    val toInsert = associations - findAll(associations.map { it.account })
                    insertAll(toInsert)
                }
            } catch (e: DuplicateKeyException) {
                logger.error("Failed to save account-to-mint association in several attempts, falling back to single saving", e)
                val toInsert = associations - findAll(associations.map { it.account })
                for (accountToMintAssociation in toInsert) {
                    mongo.save(accountToMintAssociation)
                }
            }
        }
    }

    private suspend fun insertAll(associations: Collection<AccountToMintAssociation>) {
        mongo.insertAll(associations.toMono(), AccountToMintAssociation::class.java).asFlow().collect()
    }

}
