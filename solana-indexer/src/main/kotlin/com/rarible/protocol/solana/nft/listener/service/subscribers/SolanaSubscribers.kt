package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Burn
import com.rarible.protocol.solana.borsh.BurnChecked
import com.rarible.protocol.solana.borsh.CreateMetadataAccountArgs
import com.rarible.protocol.solana.borsh.InitializeAccount
import com.rarible.protocol.solana.borsh.InitializeMint
import com.rarible.protocol.solana.borsh.MintTo
import com.rarible.protocol.solana.borsh.MintToChecked
import com.rarible.protocol.solana.borsh.Transfer
import com.rarible.protocol.solana.borsh.TransferChecked
import com.rarible.protocol.solana.borsh.parseMetadataInstruction
import com.rarible.protocol.solana.borsh.parseTokenInstruction
import com.rarible.protocol.solana.nft.listener.service.descriptors.BurnDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.CreateMetadataDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.InitializeAccountDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.InitializeMintDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.MintToDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.TransferDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaItemLogRecord.TransferRecord
import org.springframework.stereotype.Component

@Component
class InitializeMintSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = InitializeMintDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<InitializeMintRecord> {
        val initializeMintInstruction =
            log.instruction.data.parseTokenInstruction() as? InitializeMint ?: return emptyList()
        val record = InitializeMintRecord(
            mint = log.instruction.accounts[0],
            mintAuthority = initializeMintInstruction.mintAuthority,
            decimals = initializeMintInstruction.decimal.toInt(),
            log.log
        )

        return listOf(record)
    }
}

@Component
class InitializeAccountSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = InitializeAccountDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<InitializeAccountRecord> {
        if (log.instruction.data.parseTokenInstruction() !is InitializeAccount) return emptyList()

        val record = InitializeAccountRecord(
            account = log.instruction.accounts[0],
            mint = log.instruction.accounts[1],
            owner = log.instruction.accounts[2],
            log.log
        )

        return listOf(record)
    }
}

@Component
class MintToSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = MintToDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<MintToRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is MintTo -> MintToRecord(
                mint = log.instruction.accounts[0],
                account = log.instruction.accounts[1],
                mintAmount = instruction.amount.toLong(),
                log = log.log
            )
            is MintToChecked -> MintToRecord(
                mint = log.instruction.accounts[0],
                account = log.instruction.accounts[1],
                mintAmount = instruction.amount.toLong(),
                log = log.log
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class BurnSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = BurnDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<BurnRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            is Burn -> BurnRecord(
                account = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toLong(),
                log = log.log
            )
            is BurnChecked -> BurnRecord(
                account = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                burnAmount = instruction.amount.toLong(),
                log = log.log
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class TransferSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = TransferDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<TransferRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            // TODO we have no mint here, rewrite later
            is Transfer -> TransferRecord(
                from = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                to = log.instruction.accounts[2],
                amount = instruction.amount.toLong(),
                log = log.log
            )
            is TransferChecked -> TransferRecord(
                from = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                to = log.instruction.accounts[2],
                amount = instruction.amount.toLong(),
                log = log.log
            )
            else -> return emptyList()
        }

        return listOf(record)
    }
}

@Component
class MetadataSubscriber(private val descriptor: CreateMetadataDescriptor) : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = descriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaLogRecord> {
        val data = log.instruction.data
        val createMetadataAccountArgs =
            data.parseMetadataInstruction() as? CreateMetadataAccountArgs ?: return emptyList()
        val record = CreateMetadataRecord(
            mint = log.instruction.accounts[1],
            metadata = createMetadataAccountArgs,
            log = log.log
        )

        return listOf(record)
    }
}