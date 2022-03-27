package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.SignatureControllerApi
import com.rarible.protocol.solana.dto.SolanaSignatureValidationFormDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class SignatureController : SignatureControllerApi {

    override suspend fun validate(
        solanaSignatureValidationFormDto: SolanaSignatureValidationFormDto
    ): ResponseEntity<Boolean> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(false)
    }
}