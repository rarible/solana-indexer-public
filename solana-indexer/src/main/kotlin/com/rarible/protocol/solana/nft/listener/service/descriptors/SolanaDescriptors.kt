package com.rarible.protocol.solana.nft.listener.service.descriptors

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaLogRecordImpl.BurnRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaLogRecordImpl.CreateMetadataRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaLogRecordImpl.InitializeAccountRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaLogRecordImpl.InitializeMintRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaLogRecordImpl.MintToRecord
import com.rarible.protocol.solana.nft.listener.service.records.SolanaLogRecordImpl.TransferRecord

object SolanaProgramId {
    const val SPL_TOKEN_PROGRAM = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TOKEN_METADATA_PROGRAM = "metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s"
}

object InitializeMintDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    "spl-token",
    entityType = InitializeMintRecord::class.java
)

object InitializeAccountDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    "spl-token",
    entityType = InitializeAccountRecord::class.java
)

object MintToDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    "spl-token",
    entityType = MintToRecord::class.java
)

object BurnDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    "spl-token",
    entityType = BurnRecord::class.java
)

object TransferDescriptor : SolanaDescriptor(
    SolanaProgramId.SPL_TOKEN_PROGRAM,
    "spl-token",
    entityType = TransferRecord::class.java
)

object CreateMetadataDescriptor : SolanaDescriptor(
    SolanaProgramId.TOKEN_METADATA_PROGRAM,
    "token-metadata",
    entityType = CreateMetadataRecord::class.java
)