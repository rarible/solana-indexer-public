package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.TokenControllerApi
import com.rarible.protocol.solana.common.continuation.ContinuationFactory
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.common.continuation.TokenContinuation
import com.rarible.protocol.solana.common.converter.TokenMetaConverter
import com.rarible.protocol.solana.common.converter.TokenWithMetaConverter
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetaLoadService
import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.dto.RoyaltiesDto
import com.rarible.protocol.solana.dto.TokenDto
import com.rarible.protocol.solana.dto.TokenMetaDto
import com.rarible.protocol.solana.dto.TokensDto
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.protocol.solana.nft.api.service.TokenApiService
import com.rarible.protocol.union.dto.continuation.page.PageSize
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class TokenController(
    private val tokenApiService: TokenApiService,
    private val metaplexOffChainMetaLoadService: MetaplexOffChainMetaLoadService,
    private val balanceApiService: BalanceApiService
) : TokenControllerApi {

    override suspend fun getAllTokens(
        showDeleted: Boolean?,
        lastUpdatedFrom: Long?,
        lastUpdatedTo: Long?,
        continuation: String?,
        size: Int?
    ): ResponseEntity<TokensDto> {
        val safeSize = PageSize.TOKEN.limit(size)

        val tokens = tokenApiService.findAll(
            lastUpdatedFrom?.let { Instant.ofEpochMilli(it) },
            lastUpdatedTo?.let { Instant.ofEpochMilli(it) },
            DateIdContinuation.parse(continuation),
            safeSize
        ).toList()

        val dto = toSlice(tokens, TokenContinuation.ByLastUpdatedAndId, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getTokenByAddress(tokenAddress: String): ResponseEntity<TokenDto> {
        val token = tokenApiService.getToken(tokenAddress)
        return ResponseEntity.ok(TokenWithMetaConverter.convert(token))
    }

    override suspend fun getTokenMetaByAddress(tokenAddress: String): ResponseEntity<TokenMetaDto> {
        val tokenMeta = metaplexOffChainMetaLoadService.loadOffChainTokenMeta(tokenAddress)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TokenMetaConverter.convert(tokenMeta))
    }

    override suspend fun getTokenRoyaltiesByAddress(tokenAddress: String): ResponseEntity<RoyaltiesDto> {
        val royalties = tokenApiService.getTokenRoyalties(tokenAddress)
        return ResponseEntity.ok(RoyaltiesDto(royalties))
    }

    override suspend fun getTokensByAddresses(tokenAddresses: List<String>): ResponseEntity<TokensDto> {
        val tokens = tokenApiService.getTokens(tokenAddresses).toList()
        val dto = tokens.map { TokenWithMetaConverter.convert(it) }

        // Originally, here should be another DTO without continuation
        return ResponseEntity.ok(TokensDto(dto, null))
    }

    override suspend fun getTokensByCollection(
        collection: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<TokensDto> {
        val safeSize = PageSize.TOKEN.limit(size)

        val tokens = tokenApiService.getTokensByCollection(
            collection = collection,
            // Here we use continuation by ID since we can't support date sort
            // Specifically for this request union passes continuation 'as is'
            continuation = continuation,
            limit = safeSize
        ).toList()

        val dto = toSlice(tokens, TokenContinuation.ById, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getTokensByOwner(
        owner: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<TokensDto> {
        val safeSize = PageSize.TOKEN.limit(size)

        val balances = balanceApiService.getBalanceByOwner(
            owner,
            DateIdContinuation.parse(continuation),
            safeSize
        )

        val tokens = balances.map { balance ->
            val token = tokenApiService.getToken(balance.balance.mint)
            // There is no way to provide correct sorting except of using updatedAt of the balance
            token.copy(token = token.token.copy(updatedAt = balance.balance.updatedAt))
        }.toList()

        val dto = toSlice(tokens, TokenContinuation.ByLastUpdatedAndId, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getTokensByCreator(
        creator: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<TokensDto> {
        // TODO implement
        return ResponseEntity.ok(TokensDto())
    }

    private fun toSlice(
        tokens: List<TokenWithMeta>,
        continuationFactory: ContinuationFactory<TokenDto, *>,
        size: Int,
    ): TokensDto {
        val dto = tokens.map { TokenWithMetaConverter.convert(it) }

        val slice = Paging(continuationFactory, dto).getSlice(size)
        return TokensDto(slice.entities, slice.continuation)
    }
}
