package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.meta.TokenMetaParser
import com.rarible.protocol.solana.nft.listener.EventAwareBlockScannerTest
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomToken
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TokenUpdateServiceIt : EventAwareBlockScannerTest() {

    @Autowired
    private lateinit var tokenUpdateService: TokenUpdateService

    @Test
    fun `new token inserted`() = runBlocking<Unit> {
        val token = createRandomToken()
        tokenUpdateService.update(token)
        assertThat(tokenRepository.findByMint(token.mint)).isEqualTo(token)
        assertTokenMetaUpdatedEvent(token.mint, null, null)
    }

    @Test
    fun `collection NFT updated`() = runBlocking<Unit> {
        val collectionToken = createRandomToken()
        val collectionMint = collectionToken.mint
        collectionService.saveCollectionV2(collectionMint)
        val metaplexMeta = createRandomMetaplexMeta(mint = collectionMint)
        metaplexMetaRepository.save(metaplexMeta)
        tokenUpdateService.update(collectionToken)
        Wait.waitAssert {
            assertUpdateCollectionEvent(
                collectionMint = collectionMint,
                collectionTokenMeta = TokenMetaParser.mergeOnChainAndOffChainMeta(metaplexMeta.metaFields, null)
            )
        }
    }

}