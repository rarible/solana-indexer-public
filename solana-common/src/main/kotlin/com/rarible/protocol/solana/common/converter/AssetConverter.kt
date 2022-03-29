package com.rarible.protocol.solana.common.converter

import com.rarible.protocol.solana.common.model.Asset
import com.rarible.protocol.solana.common.model.AssetType
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.model.WrappedSolAssetType
import com.rarible.protocol.solana.common.service.PriceNormalizer
import com.rarible.protocol.solana.dto.AssetDto
import com.rarible.protocol.solana.dto.AssetTypeDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.dto.SolanaSolAssetTypeDto
import org.springframework.stereotype.Component

@Component
class AssetConverter(
    private val priceNormalizer: PriceNormalizer
) {

    suspend fun convert(asset: Asset) = AssetDto(
        type = convert(asset.type),
        value = priceNormalizer.normalize(asset.type, asset.amount)
    )

    private fun convert(assetType: AssetType): AssetTypeDto = when (assetType) {
        is TokenNftAssetType -> SolanaNftAssetTypeDto(mint = assetType.tokenAddress)
        is WrappedSolAssetType -> SolanaSolAssetTypeDto()
    }

}