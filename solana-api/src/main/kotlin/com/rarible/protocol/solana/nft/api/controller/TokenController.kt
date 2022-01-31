package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.nft.api.converter.TokenConverter
import com.rarible.protocol.solana.nft.api.service.TokenService
import com.rarible.solana.protocol.api.controller.TokenControllerApi
import com.rarible.solana.protocol.dto.TokenDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class TokenController(
    private val tokenService: TokenService
) : TokenControllerApi {
    override suspend fun getTokenByAddress(tokenAddress: String): ResponseEntity<TokenDto> {
        val token = tokenService.getToken(tokenAddress)
        return ResponseEntity.ok(TokenConverter.convert(token))
    }
}
