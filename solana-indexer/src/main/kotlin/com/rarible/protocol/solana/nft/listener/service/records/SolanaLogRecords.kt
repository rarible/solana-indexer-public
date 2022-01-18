package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.protocol.solana.borsh.CreateMetadataAccountArgs
import com.rarible.protocol.solana.nft.listener.service.descriptors.SolanaProgramId

sealed class SolanaLogRecordImpl : SolanaLogRecord() {
    data class InitializeMintRecord(
        val mint: String,
        val mintAuthority: String,
        val decimals: Int,
        override val log: SolanaLog
    ) : SolanaLogRecordImpl() {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    data class InitializeAccountRecord(
        val account: String,
        val mint: String,
        val owner: String,
        override val log: SolanaLog
    ) : SolanaLogRecordImpl() {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    data class MintToRecord(
        val account: String,
        val amount: Long,
        val mint: String,
        override val log: SolanaLog
    ) : SolanaLogRecordImpl() {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    data class BurnRecord(
        val account: String,
        val amount: Long,
        val mint: String,
        override val log: SolanaLog
    ) : SolanaLogRecordImpl() {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    data class TransferRecord(
        val mint: String,
        val from: String,
        val to: String,
        val amount: Long,
        override val log: SolanaLog
    ) : SolanaLogRecordImpl() {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    data class CreateMetadataRecord(
        val mint: String,
        val metadata: CreateMetadataAccountArgs,
        override val log: SolanaLog
    ) : SolanaLogRecordImpl() {
        override fun getKey(): String = SolanaProgramId.TOKEN_METADATA_PROGRAM
    }
}
