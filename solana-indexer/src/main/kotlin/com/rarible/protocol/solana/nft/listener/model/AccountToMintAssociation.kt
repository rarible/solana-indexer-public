package com.rarible.protocol.solana.nft.listener.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(AccountToMintAssociation.COLLECTION)
data class AccountToMintAssociation(
    @Id
    val account: String,
    val mint: String
) {
    companion object {
        const val COLLECTION = "account_to_mint_association"
    }
}
