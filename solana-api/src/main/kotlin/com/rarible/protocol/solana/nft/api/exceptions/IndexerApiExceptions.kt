package com.rarible.protocol.solana.nft.api.exceptions

import com.rarible.solana.protocol.dto.SolanaApiErrorBadRequestDto
import com.rarible.solana.protocol.dto.SolanaApiErrorEntityNotFoundDto
import org.springframework.http.HttpStatus

sealed class NftIndexerApiException(
    message: String,
    val status: HttpStatus,
    val data: Any
) : Exception(message)

class EntityNotFoundApiException(type: String, id: Any) : NftIndexerApiException(
    message = getNotFoundMessage(type, id),
    status = HttpStatus.NOT_FOUND,
    data = SolanaApiErrorEntityNotFoundDto(
        message = getNotFoundMessage(type, id),
        code = SolanaApiErrorEntityNotFoundDto.Code.NOT_FOUND
    )
)

class ValidationApiException(message: String) : NftIndexerApiException(
    message = message,
    status = HttpStatus.BAD_REQUEST,
    data = SolanaApiErrorBadRequestDto(
        message = message,
        code = SolanaApiErrorBadRequestDto.Code.VALIDATION
    )
)

private fun getNotFoundMessage(type: String, id: Any): String =
    "$type with id $id not found"
