package com.rarible.protocol.solana.nft.listener.service.currency

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.time.Instant

@Component
class CurrencyTokenReader {

    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO: replace with https://raw.githubusercontent.com/solana-labs/token-list/main/src/tokens/solana.tokenlist.json
    private val fileName = "/tokens.json"

    private val objectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun readCurrencyTokens(): CurrencyTokenSet {
        val inputStream = this::class.java.getResourceAsStream(fileName)
        val result = objectMapper.readValue(InputStreamReader(inputStream), CurrencyTokenSet::class.java)

        logger.info("Found {} Solana token definitions dated by {}", result.tokens.size, result.timestamp)

        return result
    }
}

data class CurrencyTokenSet(
    val timestamp: Instant,
    val tokens: List<CurrencyToken>
)

data class CurrencyToken(
    val address: String,
    val symbol: String?
)
