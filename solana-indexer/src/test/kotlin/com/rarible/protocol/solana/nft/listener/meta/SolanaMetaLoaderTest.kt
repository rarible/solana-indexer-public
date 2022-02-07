package com.rarible.protocol.solana.nft.listener.meta

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.meta.ExternalHttpClient
import com.rarible.protocol.solana.common.meta.SolanaMeta
import com.rarible.protocol.solana.common.meta.SolanaMetaLoader
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

class SolanaMetaLoaderTest {

    private val externalHttpClient = ExternalHttpClient()
    private val solanaMetaLoader = SolanaMetaLoader(externalHttpClient)

    @Test
    fun `load meta`() = runBlocking<Unit> {
        val tokenAddress = randomString()
        val url = URL(
            "https://gist.githubusercontent.com/enslinmike/a18bd9fa8e922d641a8a8a64ce84dea6/raw/a8298b26e47f30279a1b107f19287be4f198e21d/meta.json"
        )
        val tokenMeta = solanaMetaLoader.loadMeta(tokenAddress, url)
        assertThat(tokenMeta).isEqualTo(
            SolanaMeta(
                name = "My NFT #1",
                description = "My description",
                url = url.toExternalForm()
            )
        )
    }
}
