package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Burn
import com.rarible.protocol.solana.borsh.BurnChecked
import com.rarible.protocol.solana.borsh.MintTo
import com.rarible.protocol.solana.borsh.MintToChecked
import com.rarible.protocol.solana.borsh.Transfer
import com.rarible.protocol.solana.borsh.TransferChecked
import com.rarible.protocol.solana.borsh.parseTokenInstruction
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MintToBalanceSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "mint_to_balance",
        groupId = SubscriberGroup.BALANCE.id,
        entityType = SolanaBalanceRecord.MintToRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is MintTo -> SolanaBalanceRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                account = log.instruction.accounts[1],
                mintAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is MintToChecked -> SolanaBalanceRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                account = log.instruction.accounts[1],
                mintAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class BurnBalanceSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "burn_balance",
        groupId = SubscriberGroup.BALANCE.id,
        entityType = SolanaBalanceRecord.BurnRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Burn -> SolanaBalanceRecord.BurnRecord(
                account = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is BurnChecked -> SolanaBalanceRecord.BurnRecord(
                account = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class TransferIncomeSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        groupId = SubscriberGroup.BALANCE.id,
        id = "transfer_income_balance",
        entityType = SolanaBalanceRecord.TransferIncomeRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Transfer -> SolanaBalanceRecord.TransferIncomeRecord(
                from = log.instruction.accounts[0],
                to = log.instruction.accounts[2],
                incomeAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is TransferChecked -> SolanaBalanceRecord.TransferIncomeRecord(
                from = log.instruction.accounts[0],
                to = log.instruction.accounts[2],
                incomeAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class TransferOutcomeSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        groupId = SubscriberGroup.BALANCE.id,
        id = "transfer_outcome_balance",
        entityType = SolanaBalanceRecord.TransferOutcomeRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Transfer -> SolanaBalanceRecord.TransferOutcomeRecord(
                from = log.instruction.accounts[0],
                to = log.instruction.accounts[2],
                outcomeAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is TransferChecked -> SolanaBalanceRecord.TransferOutcomeRecord(
                from = log.instruction.accounts[0],
                to = log.instruction.accounts[2],
                outcomeAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}
