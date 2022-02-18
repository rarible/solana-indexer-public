package com.rarible.protocol.solana.nft.listener.service.descriptors

import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.protocol.solana.nft.listener.service.records.SolanaBalanceRecord

object MintToBalanceDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    id = "mint_to_balance",
    groupId = SubscriberGroup.BALANCE.id,
    entityType = SolanaBalanceRecord.MintToRecord::class.java,
    collection = SubscriberGroup.BALANCE.collectionName
)

object BurnBalanceDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    id = "burn_balance",
    groupId = SubscriberGroup.BALANCE.id,
    entityType = SolanaBalanceRecord.BurnRecord::class.java,
    collection = SubscriberGroup.BALANCE.collectionName
)

object TransferIncomeBalanceDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    groupId = SubscriberGroup.BALANCE.id,
    id = "transfer_income_balance",
    entityType = SolanaBalanceRecord.TransferIncomeRecord::class.java,
    collection = SubscriberGroup.BALANCE.collectionName
)

object TransferOutcomeBalanceDescriptor : SolanaDescriptor(
    programId = SolanaProgramId.SPL_TOKEN_PROGRAM,
    groupId = SubscriberGroup.BALANCE.id,
    id = "transfer_outcome_balance",
    entityType = SolanaBalanceRecord.TransferOutcomeRecord::class.java,
    collection = SubscriberGroup.BALANCE.collectionName
)
