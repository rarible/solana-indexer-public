package com.rarible.protocol.solana.common.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(TokenOffChainCollection.COLLECTION)
data class TokenOffChainCollection(
    @Id
    val id: String = ObjectId().toHexString(),
    val hash: String,
    val name: String,
    val family: String,
    val metadataUrl: String,
    val tokenAddress: String
) {
    companion object {
        const val COLLECTION = "token-off-chain-collection"
    }
}
