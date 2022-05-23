package com.rarible.protocol.solana.common.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.core.common.optimisticLock
import com.rarible.protocol.solana.common.filter.token.dynamic.DynamicBlacklistedTokenEntry
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
class DynamicBlacklistedTokenRepository(
    private val mongo: ReactiveMongoOperations
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun save(mint: String, reason: String) {
        saveAll(listOf(mint), reason)
    }

    suspend fun saveAll(mints: Collection<String>, reason: String) {
        saveAll(mints.map { it to reason }.toMap())
    }

    suspend fun saveAll(mintAndReasons: Map<String, String>) {
        val entries = mintAndReasons.map { DynamicBlacklistedTokenEntry(it.key, it.value) }
        saveAll(entries)
    }

    @Suppress("DuplicatedCode")
    private suspend fun saveAll(entries: Collection<DynamicBlacklistedTokenEntry>) {
        if (entries.isEmpty()) return

        try {
            insertAll(entries)
        } catch (e: DuplicateKeyException) {
            logger.warn("Duplicate blacklisted tokens", e)
            try {
                optimisticLock {
                    val existingMints = findAll(entries.map { it.mint })
                    val toInsert = entries.filterNot { it.mint in existingMints }
                    insertAll(toInsert)
                }
            } catch (e: DuplicateKeyException) {
                logger.error("Failed to save blacklisted token in several attempts, falling back to single saving", e)
                val existingMints = findAll(entries.map { it.mint })
                val toInsert = entries.filterNot { it.mint in existingMints }
                for (accountToMintAssociation in toInsert) {
                    mongo.save(accountToMintAssociation)
                }
            }
        }
    }


    /**
     * Returns subset of mints that are blacklisted.
     */
    suspend fun findAll(mints: Collection<String>): Set<String> {
        if (mints.isEmpty()) return emptySet()

        val criteria = Criteria.where("_id").inValues(mints)
        val query = Query.query(criteria)
        query.fields().include("_id")
        return mongo.find(
            query,
            DynamicBlacklistedTokenEntry::class.java
        ).collectList().awaitFirst().map { it.mint }.toSet()
    }

    private suspend fun insertAll(associations: Collection<DynamicBlacklistedTokenEntry>) {
        mongo.insertAll(associations.toMono(), DynamicBlacklistedTokenEntry::class.java).asFlow().collect()
    }

}