package com.rarible.protocol.solana.nft.listener.service.records

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.protocol.solana.borsh.CreateMetadataAccountArgs
import com.rarible.protocol.solana.nft.listener.service.descriptors.SolanaProgramId

sealed class SolanaLogRecordImpl(log: SolanaLog) : SolanaLogRecord(log) {
    class InitializeMintRecord(
        val mint: String,
        val mintAuthority: String,
        val decimals: Int,
        log: SolanaLog
    ) : SolanaLogRecordImpl(log) {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    class InitializeAccountRecord(
        val account: String,
        val mint: String,
        val owner: String,
        log: SolanaLog
    ) : SolanaLogRecordImpl(log) {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    class MintToRecord(
        val account: String,
        val amount: ULong,
        val mint: String,
        log: SolanaLog
    ) : SolanaLogRecordImpl(log) {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    class BurnRecord(
        val account: String,
        val amount: ULong,
        val mint: String,
        log: SolanaLog
    ) : SolanaLogRecordImpl(log) {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    class TransferRecord(
        val from: String,
        val to: String,
        val amount: ULong,
        log: SolanaLog
    ) : SolanaLogRecordImpl(log) {
        override fun getKey(): String = SolanaProgramId.SPL_TOKEN_PROGRAM
    }

    class CreateMetadataRecord(
        val mint: String,
        val metadata: CreateMetadataAccountArgs,
        log: SolanaLog
    ) : SolanaLogRecordImpl(log) {
        override fun getKey(): String = SolanaProgramId.TOKEN_METADATA_PROGRAM
    }
}
