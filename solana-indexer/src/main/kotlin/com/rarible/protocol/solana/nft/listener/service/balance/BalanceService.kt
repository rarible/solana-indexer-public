package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.nft.listener.model.Balance
import com.rarible.protocol.solana.nft.listener.model.BalanceId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class BalanceService(
    private val balanceRepository: BalanceRepository
) {
    fun get(id: BalanceId): Mono<Balance> = balanceRepository.findById(id)

    suspend fun getAll(ids: Collection<BalanceId>): List<Balance> = balanceRepository.findAll(ids)

    suspend fun saveAll(balances: Collection<Balance>): List<Balance> = balanceRepository.saveAll(balances)

    suspend fun removeAll(ids: Collection<BalanceId>): List<Balance> = balanceRepository.removeAll(ids)

    fun delete(marker: Marker, balance: Balance): Mono<Balance> = balanceRepository.deleteById(balance.id)

    private fun save(marker: Marker, balance: Balance): Mono<Balance> {
        logger.info(marker, "Saving Balance ${balance.id}")

        return balanceRepository.save(balance)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(BalanceService::class.java)
    }
}
