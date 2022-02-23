package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.meta.TokenMeta
import com.rarible.protocol.solana.common.meta.TokenMetaParser
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractMetaAwareIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    protected lateinit var metaplexMetaRepository: MetaplexMetaRepository

    @Autowired
    protected lateinit var metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository

    suspend fun saveRandomMetaplexOnChainAndOffChainMeta(
        tokenAddress: TokenId,
        metaplexMetaCustomizer: MetaplexMeta.() -> MetaplexMeta = { this },
        metaplexOffChainMetaCustomizer: MetaplexOffChainMeta.() -> MetaplexOffChainMeta = { this }
    ): TokenMeta {
        val metaplexMeta = createRandomMetaplexMeta().copy(
            tokenAddress = tokenAddress
        ).metaplexMetaCustomizer()
        val metaplexOffChainMeta = createRandomMetaplexOffChainMeta().copy(
            tokenAddress = tokenAddress
        ).metaplexOffChainMetaCustomizer()

        metaplexMetaRepository.save(metaplexMeta)
        metaplexOffChainMetaRepository.save(metaplexOffChainMeta)

        return TokenMetaParser.mergeOnChainAndOffChainMeta(
            metaplexMeta.metaFields,
            metaplexOffChainMeta.metaFields
        )
    }
}
