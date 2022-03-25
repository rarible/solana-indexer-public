package com.rarible.protocol.solana.nft.listener.repository

import com.rarible.core.apm.CaptureSpan
import com.rarible.core.apm.SpanType
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SubscriberGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component

@Component
@CaptureSpan(type = SpanType.DB)
class BalanceLogRepository(
    private val mongo: ReactiveMongoOperations
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val collection = SubscriberGroup.BALANCE.collectionName

    suspend fun createIndices() {
        Indices.ALL.forEach { index ->
            logger.info("Ensure index '{}' for collection '{}'", index, collection)
            mongo.indexOps(collection).ensureIndex(index).awaitFirst()
        }
    }

    suspend fun findBalanceInitializationRecord(
        balanceAccount: String
    ): SolanaBalanceRecord.InitializeBalanceAccountRecord? {
        return mongo.findOne(
            Query.query(SolanaBalanceRecord.InitializeBalanceAccountRecord::balanceAccount isEqualTo balanceAccount),
            SolanaBalanceRecord.InitializeBalanceAccountRecord::class.java,
            collection
        ).awaitFirstOrNull()
    }

    suspend fun findBalanceInitializationRecords(
        balanceAccounts: List<String>
    ): Flow<SolanaBalanceRecord.InitializeBalanceAccountRecord> =
        mongo.find(
            Query.query(SolanaBalanceRecord.InitializeBalanceAccountRecord::balanceAccount inValues balanceAccounts),
            SolanaBalanceRecord.InitializeBalanceAccountRecord::class.java,
            collection
        ).asFlow()

    private object Indices {

        private val BALANCE_ACCOUNT: Index = Index()
            .on(SolanaBalanceRecord.InitializeBalanceAccountRecord::balanceAccount.name, Sort.Direction.ASC)
            .sparse()
            .background()

        private val BALANCE_ACTIVITY_BY_ITEM: Index = Index()
            .on("mint", Sort.Direction.DESC)
            .on("_class", Sort.Direction.DESC)
            .on("timestamp", Sort.Direction.DESC)
            .on("_id", Sort.Direction.DESC)
            .background()

        private val BALANCE_ACTIVITY_ALL: Index = Index()
            .on("_class", Sort.Direction.DESC)
            .on("timestamp", Sort.Direction.DESC)
            .on("_id", Sort.Direction.DESC)
            .background()

        val ALL = listOf(
            BALANCE_ACCOUNT,
            BALANCE_ACTIVITY_BY_ITEM,
            BALANCE_ACTIVITY_ALL,
        )
    }
}
