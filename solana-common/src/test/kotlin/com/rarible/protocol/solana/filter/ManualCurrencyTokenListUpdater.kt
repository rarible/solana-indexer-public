package com.rarible.protocol.solana.filter

import com.rarible.protocol.solana.common.filter.token.CurrencyTokenReader
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Run this test to update the `/tokens.json` resource (see [CurrencyTokenReader]).
 */
@Disabled
class ManualCurrencyTokenListUpdater {
    @Test
    fun `download and save the currency token set`() {
        val reader = CurrencyTokenReader()
        val tokensSet = reader.downloadCurrencyTokensSet()
        val tokensFile = File(this::class.java.getResource("/").toExternalForm().substringAfter("file:").substringBefore("/build") + "/src/main/resources/tokens.json")
        reader.saveCurrencyTokensSet(tokensFile, tokensSet)
    }
}