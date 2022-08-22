package com.rarible.protocol.solana.nft.api.controller.advice

import com.rarible.protocol.solana.common.meta.MetaException
import com.rarible.protocol.solana.common.meta.MetaTimeoutException
import com.rarible.protocol.solana.common.meta.MetaUnparseableJsonException
import com.rarible.protocol.solana.common.meta.MetaUnparseableLinkException
import com.rarible.protocol.solana.dto.SolanaApiErrorBadRequestDto
import com.rarible.protocol.solana.dto.SolanaApiErrorServerErrorDto
import com.rarible.protocol.solana.dto.SolanaApiMetaErrorDto
import com.rarible.protocol.solana.nft.api.controller.TokenController
import com.rarible.protocol.solana.nft.api.exceptions.NftIndexerApiException
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebInputException

@RestControllerAdvice(basePackageClasses = [TokenController::class])
class ErrorsController {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(NftIndexerApiException::class)
    fun handleIndexerApiException(ex: NftIndexerApiException) = mono {
        ResponseEntity.status(ex.status).body(ex.data)
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleServerWebInputException(ex: ServerWebInputException) = mono {
        // For ServerWebInputException status is always 400
        val error = SolanaApiErrorBadRequestDto(
            code = SolanaApiErrorBadRequestDto.Code.BAD_REQUEST,
            message = ex.cause?.cause?.message ?: ex.cause?.message ?: ex.message ?: MISSING_MESSAGE
        )
        logger.warn("Web input error: {}", error.message)
        ResponseEntity.status(ex.status).body(error)
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerException(ex: Throwable) = mono {
        logUnexpectedError(ex)
    }

    @ExceptionHandler(MetaException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerMetaException(ex: MetaException) = mono {
        when (ex) {
            is MetaUnparseableJsonException -> SolanaApiMetaErrorDto(
                code = SolanaApiMetaErrorDto.Code.UNPARSEABLE_JSON,
                message = ex.message
            )

            is MetaTimeoutException -> SolanaApiMetaErrorDto(
                code = SolanaApiMetaErrorDto.Code.TIMEOUT,
                message = ex.message
            )

            is MetaUnparseableLinkException -> SolanaApiMetaErrorDto(
                code = SolanaApiMetaErrorDto.Code.UNPARSEABLE_LINK,
                message = ex.message
            )
        }
    }

    private fun logUnexpectedError(ex: Throwable): SolanaApiErrorServerErrorDto {
        logger.error("System error while handling request", ex)
        return SolanaApiErrorServerErrorDto(
            code = SolanaApiErrorServerErrorDto.Code.UNKNOWN,
            message = ex.message ?: "Something went wrong"
        )
    }

    companion object {
        const val MISSING_MESSAGE = "Missing message in error"
    }
}
