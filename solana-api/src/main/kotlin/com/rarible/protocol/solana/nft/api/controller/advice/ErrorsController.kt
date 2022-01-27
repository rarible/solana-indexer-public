package com.rarible.protocol.solana.nft.api.controller.advice

import com.rarible.protocol.solana.dto.SolanaApiErrorBadRequestDto
import com.rarible.protocol.solana.dto.SolanaApiErrorServerErrorDto
import com.rarible.protocol.solana.nft.api.controller.TokenController
import com.rarible.protocol.solana.nft.api.exceptions.NftIndexerApiException
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.core.convert.ConversionFailedException
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

    @ExceptionHandler(ConversionFailedException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handlerConversionFailedException(ex: ConversionFailedException) = mono {
        when (ex.cause) {
            else -> logUnexpectedError(ex)
        }
    }

    @ExceptionHandler(Throwable::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handlerException(ex: Throwable) = mono {
        logUnexpectedError(ex)
    }

    private fun logUnexpectedError(ex: Throwable) {
        logger.error("System error while handling request", ex)
        SolanaApiErrorServerErrorDto(
            code = SolanaApiErrorServerErrorDto.Code.UNKNOWN,
            message = ex.message ?: "Something went wrong"
        )
    }

    companion object {
        const val MISSING_MESSAGE = "Missing message in error"
    }
}
