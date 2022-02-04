package com.rarible.protocol.solana.common.meta

import com.rarible.protocol.solana.common.model.TokenId
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("solana-meta-loading")

fun logMetaLoading(
    tokenAddress: TokenId,
    message: String,
    warn: Boolean = false
) {
    val logMessage = "Meta of $tokenAddress: $message"
    if (warn) {
        logger.warn(logMessage)
    } else {
        logger.info(logMessage)
    }
}
