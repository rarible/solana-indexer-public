package com.rarible.protocol.solana.nft.listener.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("account_to_mint_association")
data class AccountToMintAssociation(
    @Id
    val account: String,
    val mint: String
)
