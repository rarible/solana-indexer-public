package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.TokenControllerApi
import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.converter.TokenWithMetaConverter
import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.dto.RoyaltiesDto
import com.rarible.protocol.solana.dto.TokenDto
import com.rarible.protocol.solana.dto.TokenMetaDto
import com.rarible.protocol.solana.dto.TokensDto
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.protocol.solana.nft.api.service.TokenApiService
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

    override suspend fun getAllTokens(
        showDeleted: Boolean?,
        lastUpdatedFrom: Long?,
        lastUpdatedTo: Long?,
        continuation: String?,
        size: Int?
    ): ResponseEntity<TokensDto> {
        // TODO implement
        return ResponseEntity.ok(TokensDto())
    }

    override suspend fun getTokenByAddress(tokenAddress: String): ResponseEntity<TokenDto> {
        val tokenWithMeta = tokenApiService.getTokenWithMeta(tokenAddress)
        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokenWithMeta))
    }

    override suspend fun getTokenMetaByAddress(tokenAddress: String): ResponseEntity<TokenMetaDto> {
        val tokenMeta = tokenMetaService.loadTokenMeta(tokenAddress)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TokenMetaConverter.convert(tokenMeta))
    }

    override suspend fun resetTokenMeta(tokenAddress: String): ResponseEntity<Unit> {
        // TODO implement
        return ResponseEntity.ok().build()
    }

    override suspend fun getTokenRoyaltiesByAddress(tokenAddress: String): ResponseEntity<RoyaltiesDto> {
        // TODO implement
        return ResponseEntity.ok(RoyaltiesDto(emptyList()))
    }

    override suspend fun getTokensByAddresses(tokenAddresses: List<String>): ResponseEntity<TokensDto> {
        val tokensWithMeta = tokenApiService.getTokensWithMeta(tokenAddresses).toList()

        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokensWithMeta))
    }

    override suspend fun getTokensByCollection(
        collection: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<TokensDto> {
        // TODO support continuation
        val tokensWithMeta = tokenApiService.getTokensWithMetaByCollection(collection).toList()

        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokensWithMeta))
    }

    override suspend fun getTokensByOwner(
        owner: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<TokensDto> {
        // TODO support continuation
        val balancesWithMeta = balanceApiService.getBalanceWithMetaByOwner(owner)
        val tokensWithMeta = balancesWithMeta.map {
            tokenApiService.getTokenWithMeta(it.balance.mint)
        }.toList()

        return ResponseEntity.ok(TokenWithMetaConverter.convert(tokensWithMeta))
    }

    override suspend fun getTokensByCreator(
        creator: String, continuation: String?, size: Int?
    ): ResponseEntity<TokensDto> {
        // TODO implement
        return ResponseEntity.ok(TokensDto())
    }
}
