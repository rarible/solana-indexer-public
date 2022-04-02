package com.rarible.protocol.solana.common.converter

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.dto.ActivityBlockchainInfoDto
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
            is SolanaBalanceRecord.MintToRecord -> makeMint(source, reverted)
            is SolanaBalanceRecord.BurnRecord -> makeBurn(source, reverted)
            is SolanaBalanceRecord.TransferIncomeRecord -> makeTransferIn(source, reverted)
            is SolanaBalanceRecord.TransferOutcomeRecord -> makeTransferOut(source, reverted)
            is SolanaBalanceRecord.InitializeBalanceAccountRecord -> null
        }
    }

    private suspend fun makeMint(record: SolanaBalanceRecord.MintToRecord, reverted: Boolean): ActivityDto? {
        val owner = balanceRepository.findById(record.account)?.owner
        if (owner == null) {
            logger.warn("Unable to find balance: {}", record.account)
            return null
        }
        return MintActivityDto(
            id = record.id,
            date = record.timestamp,
            owner = owner,
            tokenAddress = record.mint,
            value = record.mintAmount,
            blockchainInfo = blockchainInfo(record.log),
            reverted = reverted
        )
    }

    private suspend fun makeBurn(record: SolanaBalanceRecord.BurnRecord, reverted: Boolean): ActivityDto? {
        val owner = balanceRepository.findById(record.account)?.owner
        if (owner == null) {
            logger.warn("Unable to find balance: {}", record.account)
            return null
        }
        return BurnActivityDto(
            id = record.id,
            date = record.timestamp,
            owner = record.account,
            tokenAddress = record.mint,
            value = record.burnAmount,
            blockchainInfo = blockchainInfo(record.log),
            reverted = reverted
        )
    }

    private fun makeTransferIn(
        record: SolanaBalanceRecord.TransferIncomeRecord, reverted: Boolean
    ) = TransferActivityDto(
        id = record.id,
        date = record.timestamp,
        from = record.from,
        owner = record.owner,
        tokenAddress = record.mint,
        value = record.incomeAmount,
        blockchainInfo = blockchainInfo(record.log),
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

    private fun makeTransferOut(
        record: SolanaBalanceRecord.TransferOutcomeRecord, reverted: Boolean
    ) = TransferActivityDto(
        id = record.id,
        date = record.timestamp,
        from = record.owner,
        owner = record.to,
        tokenAddress = record.mint,
        value = record.outcomeAmount,
        blockchainInfo = blockchainInfo(record.log),
        reverted = reverted,
        purchase = false // TODO should be evaluated
    )

    private fun blockchainInfo(log: SolanaLog) = ActivityBlockchainInfoDto(
        blockNumber = log.blockNumber,
        blockHash = log.blockHash,
        transactionIndex = log.transactionIndex,
        transactionHash = log.transactionHash,
        instructionIndex = log.instructionIndex,
        innerInstructionIndex = log.innerInstructionIndex,
    )
}