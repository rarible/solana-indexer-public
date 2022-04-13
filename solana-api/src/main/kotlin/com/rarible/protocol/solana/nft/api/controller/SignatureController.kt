package com.rarible.protocol.solana.nft.api.controller

import com.google.crypto.tink.subtle.Ed25519Verify
import com.google.crypto.tink.subtle.Hex
import com.rarible.protocol.solana.api.controller.SignatureControllerApi
import com.rarible.protocol.solana.dto.SolanaSignatureValidationFormDto
import com.rarible.protocol.solana.nft.api.exceptions.ValidationApiException
import org.bitcoinj.core.Base58
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class SignatureController : SignatureControllerApi {

    private val logger = LoggerFactory.getLogger(SignatureController::class.java)

    override suspend fun validate(
        solanaSignatureValidationFormDto: SolanaSignatureValidationFormDto
    ): ResponseEntity<Boolean> {
        val signature = solanaSignatureValidationFormDto.signature
        val signatureBytes = try {
            Hex.decode(signature)
        } catch (e: Exception) {
            throw ValidationApiException("Invalid signature, must be 128 hex string, but was $signature")
        }

        val signer = solanaSignatureValidationFormDto.signer
        if (signer.isEmpty()) {
            throw ValidationApiException("Signer is empty")
        }
        val signerBytes = try {
            Base58.decode(signer)
        } catch (e: Exception) {
            throw ValidationApiException("Signer must be in base58, but was $signer")
        }

        val message = solanaSignatureValidationFormDto.message
        try {
            Ed25519Verify(signerBytes).verify(signatureBytes, message.toByteArray())
        } catch (e: Exception) {
            logger.info("Wrong signature '$signature' by signer '$signer' for data '$message'")
            return ResponseEntity.ok(false)
        }

        return ResponseEntity.ok(true)
    }
}