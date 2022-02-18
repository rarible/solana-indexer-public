package com.rarible.protocol.solana.nft.listener.service.subscribers

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.subscriber.SolanaLogEventSubscriber
import com.rarible.protocol.solana.borsh.Burn
import com.rarible.protocol.solana.borsh.BurnChecked
import com.rarible.protocol.solana.borsh.InitializeAccount
import com.rarible.protocol.solana.borsh.InitializeAccount2and3
import com.rarible.protocol.solana.borsh.InitializeMint1and2
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.MetaplexUpdateMetadataAccountArgs
import com.rarible.protocol.solana.borsh.MintTo
import com.rarible.protocol.solana.borsh.MintToChecked
import com.rarible.protocol.solana.borsh.Transfer
import com.rarible.protocol.solana.borsh.TransferChecked
import com.rarible.protocol.solana.borsh.VerifyCollection
import com.rarible.protocol.solana.borsh.parseMetaplexMetadataInstruction
import com.rarible.protocol.solana.borsh.parseTokenInstruction
import com.rarible.protocol.solana.nft.listener.service.descriptors.BurnBalanceDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.BurnTokenDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.CreateMetadataDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.InitializeAccountDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.InitializeMintDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.MintToBalanceDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.MintToTokenDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.TransferIncomeBalanceDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.TransferOutcomeBalanceDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.UpdateMetadataDescriptor
import com.rarible.protocol.solana.nft.listener.service.descriptors.VerifyCollectionDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord.InitializeTokenAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord.InitializeMintRecord
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class InitializeMintSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = InitializeMintDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<InitializeMintRecord> {
        val initializeMint1and2Instruction = log.instruction.data.parseTokenInstruction() as? InitializeMint1and2
            ?: return emptyList()
        val record = InitializeMintRecord(
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
class InitializeAccountSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = InitializeAccountDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<InitializeTokenAccountRecord> {
        val record = when (val instruction = log.instruction.data.parseTokenInstruction()) {
            InitializeAccount -> InitializeTokenAccountRecord(
                tokenAccount = log.instruction.accounts[0],
                mint = log.instruction.accounts[1],
                owner = log.instruction.accounts[2],
                log = log.log,
                timestamp = Instant.ofEpochSecond(block.timestamp)
            )
            is InitializeAccount2and3 -> InitializeTokenAccountRecord(
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

@Component
class MintToTokenSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = MintToTokenDescriptor

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
class MintToBalanceSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = MintToBalanceDescriptor

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
class BurnTokenSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = BurnTokenDescriptor

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
class BurnBalanceSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = BurnBalanceDescriptor

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
    override fun getDescriptor(): SolanaDescriptor = TransferIncomeBalanceDescriptor

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
    override fun getDescriptor(): SolanaDescriptor = TransferOutcomeBalanceDescriptor

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

@Component
class MetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = CreateMetadataDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val data = log.instruction.data
        val metaplexCreateMetadataAccount =
            data.parseMetaplexMetadataInstruction() as? MetaplexCreateMetadataAccount ?: return emptyList()
        val record = SolanaMetaRecord.MetaplexCreateMetadataRecord(
            metaAccount = log.instruction.accounts[0],
            mint = log.instruction.accounts[1],
            data = metaplexCreateMetadataAccount,
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )

        return listOf(record)
    }
}

@Component
class UpdateMetadataSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = UpdateMetadataDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val data = log.instruction.data
        val metaplexUpdateMetadataAccountArgs =
            data.parseMetaplexMetadataInstruction() as? MetaplexUpdateMetadataAccountArgs ?: return emptyList()
        val record = SolanaMetaRecord.MetaplexUpdateMetadataRecord(
            metaAccount = log.instruction.accounts[0],
            mint = log.instruction.accounts[1],
            newData = metaplexUpdateMetadataAccountArgs,
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )

        return listOf(record)
    }
}

@Component
class VerifyCollectionSubscriber : SolanaLogEventSubscriber {
    override fun getDescriptor(): SolanaDescriptor = VerifyCollectionDescriptor

    override suspend fun getEventRecords(
        block: SolanaBlockchainBlock,
        log: SolanaBlockchainLog
    ): List<SolanaMetaRecord> {
        val instruction = log.instruction

        if (instruction.data.parseMetaplexMetadataInstruction() !is VerifyCollection) return emptyList()

        val record = SolanaMetaRecord.MetaplexVerifyCollectionRecord(
            metaAccount = instruction.accounts[0],
            collectionAccount = instruction.accounts[4],
            log = log.log,
            timestamp = Instant.ofEpochSecond(block.timestamp)
        )
        return listOf(record)
    }
}
