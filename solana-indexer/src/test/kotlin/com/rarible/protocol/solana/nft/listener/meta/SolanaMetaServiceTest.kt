package com.rarible.protocol.solana.nft.listener.meta

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.meta.SolanaMetaLoader
import com.rarible.protocol.solana.common.meta.SolanaMetaService
import com.rarible.protocol.solana.nft.listener.AbstractBlockScannerTest
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.createRandomTokenMeta
import com.rarible.protocol.solana.test.randomUrl
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.net.URL

@Disabled
class SolanaMetaServiceTest : AbstractBlockScannerTest() {

    @Autowired
    private lateinit var solanaMetaService: SolanaMetaService

    @Autowired
    @Qualifier("test.solana.meta.loader")
    private lateinit var solanaMetaLoader: SolanaMetaLoader

    @Test
    fun `schedule loading and resolve meta`() = runBlocking<Unit> {
        val metadataUrl = randomUrl().toUrl()
        val token = createRandomToken().copy(metadataUrl = metadataUrl.toExternalForm())
        tokenRepository.save(token)
        val tokenMeta = createRandomTokenMeta()
        coEvery { solanaMetaLoader.loadMeta(token.id, metadataUrl) } returns tokenMeta
        assertThat(solanaMetaService.getAvailable(token.id)).isNull()
        solanaMetaService.scheduleLoading(token.id, metadataUrl)
        Wait.waitAssert {
            assertThat(solanaMetaService.getAvailable(token.id)).isEqualTo(tokenMeta)
        }
    }

    private fun String.toUrl() = URL(this)
}
