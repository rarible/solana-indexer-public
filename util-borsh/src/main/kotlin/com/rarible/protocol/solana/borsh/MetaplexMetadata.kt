package com.rarible.protocol.solana.borsh

object MetaplexMetadata {
    enum class DataVersion {
        V1, V2
    }

    data class Creator(
        val address: Pubkey,
        val verified: Boolean,
        // In percentages, NOT basis points ;) Watch out!
        val share: Byte,
    )

    data class Collection(
        val key: Pubkey,
        val verified: Boolean
    )

    data class Data(
        val name: String,
        /// The symbol for the asset
        val symbol: String,
        /// URI pointing to JSON representing the asset
        val uri: String,
        /// Royalty basis points that goes to creators in secondary sales (0-10000)
        val sellerFeeBasisPoints: Short,
        /// Array of creators, optional
        val creators: List<Creator>?,
        val collection: Collection?
    )

    data class CreateAccountArgs(
        val metadata: Data,
        val mutable: Boolean
    )

    data class UpdateAccountArgs(
        val metadata: Data?,
        val updateAuthority: Pubkey?,
        val primarySaleHappened: Boolean?,
        val mutable: Boolean?
    )
}
