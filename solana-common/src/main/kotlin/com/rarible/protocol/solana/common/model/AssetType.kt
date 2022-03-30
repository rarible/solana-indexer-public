package com.rarible.protocol.solana.common.model

sealed class AssetType {

    abstract val tokenAddress: String
}

data class TokenNftAssetType(
    override val tokenAddress: String,
) : AssetType()

data class TokenFtAssetType(
    override val tokenAddress: String,
) : AssetType()

// WARNING token address should be a constant, it is made as data class only to be stored in mongo with this value
data class WrappedSolAssetType(
    override val tokenAddress: String = SOL
) : AssetType() {

    companion object {

        const val SOL = "So11111111111111111111111111111111111111112"
    }
}