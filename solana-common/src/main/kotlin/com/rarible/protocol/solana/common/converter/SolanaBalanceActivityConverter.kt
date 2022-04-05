package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.BurnActivityDto
import com.rarible.protocol.solana.dto.MintActivityDto
import com.rarible.protocol.solana.dto.TransferActivityDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SolanaBalanceActivityConverter(
    private val balanceRepository: BalanceRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun convert(source: SolanaBalanceRecord, reverted: Boolean): ActivityDto? {
        return when (source) {
            is SolanaBalanceRecord.MintToRecord -> createMintActivity(source, reverted)
            is SolanaBalanceRecord.BurnRecord -> createBurnActivity(source, reverted)
            is SolanaBalanceRecord.TransferIncomeRecord -> createTransferIncomeActivity(source, reverted)
            is SolanaBalanceRecord.TransferOutcomeRecord -> createTransferOutcomeActivity(source, reverted)
            is SolanaBalanceRecord.InitializeBalanceAccountRecord -> null
        }
    }

    private suspend fun createMintActivity(
        record: SolanaBalanceRecord.MintToRecord,
        reverted: Boolean
    ): ActivityDto? {
        val owner = findOwner(record.account) ?: return null
        return MintActivityDto(
            id = record.id,
            date = record.timestamp,
            owner = owner,
            tokenAddress = record.mint,
            value = record.mintAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted
        )
    }

    private suspend fun createBurnActivity(
        record: SolanaBalanceRecord.BurnRecord,
        reverted: Boolean
    ): ActivityDto? {
        val owner = findOwner(record.account) ?: return null
        return BurnActivityDto(
            id = record.id,
            date = record.timestamp,
            owner = owner,
            tokenAddress = record.mint,
            value = record.burnAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted
        )
    }

    private suspend fun createTransferIncomeActivity(
        record: SolanaBalanceRecord.TransferIncomeRecord,
        reverted: Boolean
    ): TransferActivityDto? {
        @Suppress("DuplicatedCode")
        val fromOwner = findOwner(record.from) ?: return null
        val toOwner = findOwner(record.account) ?: return null
        return TransferActivityDto(
            id = record.id,
            date = record.timestamp,
            from = fromOwner,
            owner = toOwner,
            tokenAddress = record.mint,
            value = record.incomeAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
            purchase = false // TODO should be evaluated
            // Purchase = true/false should be evaluated in next way (at least, it is done in such way at ETH):
            // If item transferred to owner from contract, it means item has been purchased, if "to" is not a
            // contract but regular balance, this transfer is not a purchase.

            // To determine is transfer related to a contract we did next things at ETH:
            // 1. We started to write field "to" (from - prev owner, owner - end owner, to - exchange contract)
            // 2. We introduced configuration field "exchage contract addresses" contains most popular exchange contracts
            // 3. If 'to' contains value from this contract set - it means this transfer is 'purchase'
        )
    }

    private suspend fun createTransferOutcomeActivity(
        record: SolanaBalanceRecord.TransferOutcomeRecord,
        reverted: Boolean
    ): TransferActivityDto? {
        val toOwner = findOwner(record.to) ?: return null
        val fromOwner = findOwner(record.account) ?: return null
        return TransferActivityDto(
            id = record.id,
            date = record.timestamp,
            from = fromOwner,
            owner = toOwner,
            tokenAddress = record.mint,
            value = record.outcomeAmount,
            blockchainInfo = SolanaLogToActivityBlockchainInfoConverter.convert(record.log),
            reverted = reverted,
            purchase = false // TODO should be evaluated
        )
    }

    private suspend fun findOwner(account: String): String? {
        val owner = balanceRepository.findByAccount(account)?.owner
        if (owner == null) {
            logger.warn("Unable to find balance by account: {}", account)
            return null
        }
        return owner
    }

}