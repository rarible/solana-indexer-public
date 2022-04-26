package com.rarible.protocol.solana.nft.listener.util

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.protocol.solana.borsh.MetaplexCreateMetadataAccount
import com.rarible.protocol.solana.borsh.parseMetaplexMetadataInstruction
import com.rarible.protocol.solana.common.pubkey.SolanaProgramId
import com.rarible.protocol.solana.common.records.SolanaMetaRecord
import com.rarible.protocol.solana.nft.listener.service.subscribers.SolanaMetaplexMetaLogConverter

fun SolanaBlockchainBlock.transactionLogs(
    transactionHash: String,
    programId: String
) = logs.filter { it.log.transactionHash == transactionHash && it.instruction.programId == programId }

fun SolanaBlockchainBlock.hasCreateMetaplexMeta(
    transactionHash: String,
    matcher: (SolanaMetaRecord.MetaplexCreateMetadataAccountRecord) -> Boolean
): Boolean {
    return transactionLogs(transactionHash, SolanaProgramId.TOKEN_METADATA_PROGRAM).any { log ->
        val createMetadataAccount = log.instruction.data.parseMetaplexMetadataInstruction()
                as? MetaplexCreateMetadataAccount ?: return false

        val record = SolanaMetaplexMetaLogConverter.convertCreateMetadataAccount(
            log,
            createMetadataAccount,
            timestamp
        )

        matcher(record)
    }
}