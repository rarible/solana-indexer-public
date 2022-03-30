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

object WrappedSolAssetType : AssetType() {

    override val tokenAddress: String = "So11111111111111111111111111111111111111112"

    override fun equals(other: Any?): Boolean {
        return other != null && other is WrappedSolAssetType
    }

    override fun hashCode(): Int {
        return tokenAddress.hashCode()
    }

    override fun toString(): String {
        return "WrappedSolAssetType(tokenAddress=$tokenAddress)"
    }
}