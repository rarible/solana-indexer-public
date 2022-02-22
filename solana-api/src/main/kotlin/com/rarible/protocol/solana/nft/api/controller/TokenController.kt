package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.converter.TokenWithMetaConverter
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.nft.api.service.TokenApiService
import com.rarible.solana.protocol.api.controller.TokenControllerApi
import com.rarible.solana.protocol.dto.TokenDto
import com.rarible.solana.protocol.dto.TokenMetaDto
import com.rarible.solana.protocol.dto.TokensDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenController(
    private val tokenApiService: TokenApiService,
    private val tokenMetaService: TokenMetaService
) : TokenControllerApi {
    override suspend fun getTokenByAddress(tokenAddress: String): ResponseEntity<TokenDto> {
        val tokenWithMeta = tokenApiService.getTokenWithMeta(tokenAddress)
        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokenWithMeta))
    }

    override suspend fun getTokenMetaByAddress(tokenAddress: String): ResponseEntity<TokenMetaDto> {
        val tokenMeta = tokenMetaService.loadTokenMeta(tokenAddress)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TokenMetaConverter.convert(tokenMeta))
    }

    override suspend fun getTokensByCollection(collection: String): ResponseEntity<TokensDto> {
        val tokensWithMeta = tokenApiService.getTokensWithMetaByCollection(collection)
        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokensWithMeta))
    }
}
