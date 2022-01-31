package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.TokenControllerApi
import com.rarible.protocol.solana.dto.TokenDto
import com.rarible.protocol.solana.nft.api.converter.TokenConverter
import com.rarible.protocol.solana.nft.api.service.TokenService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class TokenController(
    private val tokenService: TokenService
) : TokenControllerApi {
    override suspend fun getTokenByAddress(tokenAddress: String): ResponseEntity<TokenDto> {
        val token = tokenService.getToken(tokenAddress)

        return ResponseEntity.ok(TokenConverter.convert(token))
    }
}
