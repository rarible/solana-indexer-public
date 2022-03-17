package com.rarible.protocol.solana.nft.listener.migration

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.rarible.protocol.solana.nft.listener.repository.BalanceLogRepository
import io.changock.migration.api.annotations.NonLockGuarded
import kotlinx.coroutines.runBlocking

@ChangeLog(order = "00001")
class ChangeLog00001CreateIndices {

    @ChangeSet(
        id = "ChangeLog00001CreateIndices.createIndicesForAllCollections",
        order = "00001",
        author = "protocol",
        runAlways = true
    )
    fun createIndicesForAllCollections(
        @NonLockGuarded balanceLogRepository: BalanceLogRepository
    ) = runBlocking {
        balanceLogRepository.createIndices()
    }
}