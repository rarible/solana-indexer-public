package com.rarible.protocol.solana.common.filter.token

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStreamReader
import java.net.URL

/**
 * Reads currency tokens (coins) addresses from a hard-coded resource file `/tokens.json`
 * Those tokens are ignored from indexing because they are not NFTs.
 * Originally, the list is obtained from https://raw.githubusercontent.com/solana-labs/token-list/main/src/tokens/solana.tokenlist.json
 * There is `ManualCurrencyTokenListUpdater` utility test that updates the `/tokens.json` file, and which it must be committed.
 */
@Component
class CurrencyTokenReader {

    private val logger = LoggerFactory.getLogger(javaClass)

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

    fun downloadCurrencyTokensSet(): CurrencyTokenSet =
        objectMapper.readValue(URL(SOURCE_URL).readText())

    fun saveCurrencyTokensSet(outputFile: File, currencyTokenSet: CurrencyTokenSet) {
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValue(outputFile, currencyTokenSet)
        logger.info("Saved ${currencyTokenSet.tokens.size} currency tokens to ${outputFile.absolutePath}")
    }

    private companion object {
        const val SOURCE_URL =
            "https://raw.githubusercontent.com/solana-labs/token-list/main/src/tokens/solana.tokenlist.json"
    }
}

data class CurrencyTokenSet(
    val timestamp: String,
    val tokens: List<CurrencyToken>
)

data class CurrencyToken(
    val address: String,
    val symbol: String?
)
