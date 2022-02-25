package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.converter.TokenWithMetaConverter
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.protocol.solana.nft.api.service.TokenApiService
import com.rarible.solana.protocol.api.controller.TokenControllerApi
import com.rarible.solana.protocol.dto.TokenDto
import com.rarible.solana.protocol.dto.TokenMetaDto
import com.rarible.solana.protocol.dto.TokensDto
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenController(
    private val tokenApiService: TokenApiService,
    private val tokenMetaService: TokenMetaService,
    private val balanceApiService: BalanceApiService
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

    override suspend fun getTokensByAddresses(tokenAddresses: List<String>): ResponseEntity<TokensDto> {
        val tokensWithMeta = tokenApiService.getTokensWithMeta(tokenAddresses).toList()

        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokensWithMeta))
    }

    override suspend fun getTokensByCollection(collection: String): ResponseEntity<TokensDto> {
        val tokensWithMeta = tokenApiService.getTokensWithMetaByCollection(collection).toList()

        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokensWithMeta))
    }

    override suspend fun getTokensByOwner(owner: String): ResponseEntity<TokensDto> {
        val balancesWithMeta = balanceApiService.getBalanceWithMetaByOwner(owner)
        val tokensWithMeta = balancesWithMeta.map {
            tokenApiService.getTokenWithMeta(it.balance.mint)
        }.toList()

        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokensWithMeta))
    }
}
