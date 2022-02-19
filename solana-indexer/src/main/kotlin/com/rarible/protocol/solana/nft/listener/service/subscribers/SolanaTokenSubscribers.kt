package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Burn
import com.rarible.protocol.solana.borsh.BurnChecked
import com.rarible.protocol.solana.borsh.InitializeAccount
import com.rarible.protocol.solana.borsh.InitializeAccount2and3
import com.rarible.protocol.solana.borsh.InitializeMint1and2
import com.rarible.protocol.solana.borsh.MintTo
import com.rarible.protocol.solana.borsh.MintToChecked
import com.rarible.protocol.solana.borsh.parseTokenInstruction
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class InitializeMintSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "initialize_mint",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.InitializeMintRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaTokenRecord.InitializeMintRecord> {
        val initializeMint1and2Instruction = log.instruction.data.parseTokenInstruction() as? InitializeMint1and2
            ?: return emptyList()
        val record = SolanaTokenRecord.InitializeMintRecord(
            mint = log.instruction.accounts[0],
            mintAuthority = initializeMint1and2Instruction.mintAuthority,
            decimals = initializeMint1and2Instruction.decimal.toInt(),
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )
        return listOf(record)
    }
}

@Component
class MintToTokenSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "mint_to_token",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.MintToRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaTokenRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is MintTo -> SolanaTokenRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                tokenAccount = log.instruction.accounts[1],
                mintAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is MintToChecked -> SolanaTokenRecord.MintToRecord(
                mint = log.instruction.accounts[0],
                tokenAccount = log.instruction.accounts[1],
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
class BurnTokenSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "burn_token",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.BurnRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaTokenRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Burn -> SolanaTokenRecord.BurnRecord(
                tokenAccount = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toLong(),
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is BurnChecked -> SolanaTokenRecord.BurnRecord(
                tokenAccount = log.instruction.accounts[0],
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
class InitializeTokenAccountSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = SolanaDescriptor(
        programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
        id = "initialize_token_account",
        groupId = SubscriberGroup.TOKEN.id,
        entityType = SolanaTokenRecord.InitializeTokenAccountRecord::class.java,
        collection = SubscriberGroup.TOKEN.collectionName
    )

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaTokenRecord.InitializeTokenAccountRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            InitializeAccount -> SolanaTokenRecord.InitializeTokenAccountRecord(
                tokenAccount = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                owner = log.instruction.accounts[2],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is InitializeAccount2and3 -> SolanaTokenRecord.InitializeTokenAccountRecord(
                tokenAccount = log.instruction.accounts[0],
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
