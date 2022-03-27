package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Burn
import com.rarible.protocol.solana.borsh.BurnChecked
import com.rarible.protocol.solana.borsh.InitializeAccount
import com.rarible.protocol.solana.borsh.InitializeAccount2and3
import com.rarible.protocol.solana.borsh.MintTo
import com.rarible.protocol.solana.borsh.MintToChecked
import com.rarible.protocol.solana.borsh.Transfer
import com.rarible.protocol.solana.borsh.TransferChecked
import com.rarible.protocol.solana.borsh.parseTokenInstruction
import com.rarible.protocol.solana.common.util.toBigInteger
import com.rarible.protocol.solana.common.records.SolanaBalanceRecord
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class InitializeBalanceAccountSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "balance_initialize_account",
        groupId = SubscriberGroup.BALANCE.id,
        entityType = SolanaBalanceRecord.InitializeBalanceAccountRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord.InitializeBalanceAccountRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            InitializeAccount -> SolanaBalanceRecord.InitializeBalanceAccountRecord(
                balanceAccount = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                owner = log.instruction.accounts[2],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is InitializeAccount2and3 -> SolanaBalanceRecord.InitializeBalanceAccountRecord(
                balanceAccount = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                owner = instruction.owner,
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }
        return listOf(record)
    }
}

@Component
class MintToBalanceSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "balance_mint_to",
        groupId = SubscriberGroup.BALANCE.id,
        entityType = SolanaBalanceRecord.MintToRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is MintTo -> SolanaBalanceRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                account = log.instruction.accounts[1],
                mintAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is MintToChecked -> SolanaBalanceRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                account = log.instruction.accounts[1],
                mintAmount = instruction.amount.toBigInteger(),
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
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "balance_burn",
        groupId = SubscriberGroup.BALANCE.id,
        entityType = SolanaBalanceRecord.BurnRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Burn -> SolanaBalanceRecord.BurnRecord(
                account = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is BurnChecked -> SolanaBalanceRecord.BurnRecord(
                account = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toBigInteger(),
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
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        groupId = SubscriberGroup.BALANCE.id,
        id = "balance_transfer_income",
        entityType = SolanaBalanceRecord.TransferIncomeRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Transfer -> SolanaBalanceRecord.TransferIncomeRecord(
                from = log.instruction.accounts[0],
                mint = null,
                owner = log.instruction.accounts[1],
                incomeAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is TransferChecked -> SolanaBalanceRecord.TransferIncomeRecord(
                from = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                owner = log.instruction.accounts[2],
                incomeAmount = instruction.amount.toBigInteger(),
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
    override fun getDescriptor(): SolanaDescriptor = object : SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        groupId = SubscriberGroup.BALANCE.id,
        id = "balance_transfer_outcome",
        entityType = SolanaBalanceRecord.TransferOutcomeRecord::class.java,
        collection = SubscriberGroup.BALANCE.collectionName
    ) {}

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaBalanceRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Transfer -> SolanaBalanceRecord.TransferOutcomeRecord(
                to = log.instruction.accounts[1],
                owner = log.instruction.accounts[0],
                mint = null,
                outcomeAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is TransferChecked -> SolanaBalanceRecord.TransferOutcomeRecord(
                to = log.instruction.accounts[2],
                owner = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                outcomeAmount = instruction.amount.toBigInteger(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}
