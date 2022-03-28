package com.rarible.protocol.solana.common.model

sealed class AssetType {
    abstract val tokenAddress: String
}

data class TokenNftAssetType(
    override val tokenAddress: String,
) : AssetType()

object WrappedSolAssetType : AssetType() {
    override val tokenAddress: String = "So11111111111111111111111111111111111111112"
}

fun getAssetType(mint: String): AssetType = when (mint) {
    WrappedSolAssetType.tokenAddress -> WrappedSolAssetType
    else -> TokenNftAssetType(mint)
}