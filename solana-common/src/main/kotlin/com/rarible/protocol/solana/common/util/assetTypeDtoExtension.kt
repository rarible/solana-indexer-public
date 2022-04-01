package com.rarible.protocol.solana.common.util

import com.rarible.protocol.solana.dto.AssetTypeDto
import com.rarible.protocol.solana.dto.SolanaFtAssetTypeDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.dto.SolanaSolAssetTypeDto

fun AssetTypeDto.getNftMint(): String? {
    return when (this) {
        is SolanaSolAssetTypeDto -> null
        is SolanaNftAssetTypeDto -> this.mint
        is SolanaFtAssetTypeDto -> null
    }
}