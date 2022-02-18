package com.rarible.protocol.solana.nft.listener.service.descriptors

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaTokenRecord


object InitializeMintDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    id = "initialize_mint",
    groupId = SubscriberGroup.TOKEN.id,
    entityType = SolanaTokenRecord.InitializeMintRecord::class.java,
    collection = SubscriberGroup.TOKEN.collectionName
)

object InitializeAccountDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    id = "initialize_account",
    groupId = SubscriberGroup.TOKEN.id,
    entityType = SolanaTokenRecord.InitializeTokenAccountRecord::class.java,
    collection = SubscriberGroup.TOKEN.collectionName
)

object MintToTokenDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    id = "mint_to_token",
    groupId = SubscriberGroup.TOKEN.id,
    entityType = SolanaTokenRecord.MintToRecord::class.java,
    collection = SubscriberGroup.TOKEN.collectionName
)

object BurnTokenDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    id = "burn_token",
    groupId = SubscriberGroup.TOKEN.id,
    entityType = SolanaTokenRecord.BurnRecord::class.java,
    collection = SubscriberGroup.TOKEN.collectionName
)
