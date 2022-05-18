package com.rarible.protocol.solana.nft.api.test

import com.rarible.core.common.nowMillis
import com.rarible.protocol.solana.api.client.BalanceControllerApi
import com.rarible.protocol.solana.api.client.CollectionControllerApi
import com.rarible.protocol.solana.api.client.FixedSolanaApiServiceUriProvider
import com.rarible.protocol.solana.api.client.NoopWebClientCustomizer
import com.rarible.protocol.solana.api.client.OrderControllerApi
import com.rarible.protocol.solana.api.client.SolanaNftIndexerApiClientFactory
import com.rarible.protocol.solana.api.client.TokenControllerApi
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetaLoader
import com.rarible.protocol.solana.common.meta.TokenMeta
import com.rarible.protocol.solana.common.meta.TokenMetaParser
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.model.TokenWithMeta
import com.rarible.protocol.solana.common.repository.AuctionHouseRepository
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.createRandomMetaplexOffChainMeta
import com.rarible.protocol.solana.test.createRandomToken
import com.rarible.protocol.solana.test.randomMint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.net.URI
import java.time.Instant
import javax.annotation.PostConstruct

@Import(ControllersTestConfiguration::class)
abstract class AbstractControllerTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var testMetaplexOffChainMetaLoader: MetaplexOffChainMetaLoader

    @Autowired
    lateinit var tokenRepository: TokenRepository

    @Autowired
    lateinit var metaplexMetaRepository: MetaplexMetaRepository

    @Autowired
    lateinit var metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository

    @Autowired
    lateinit var auctionHouseRepository: AuctionHouseRepository

    protected lateinit var tokenControllerApi: TokenControllerApi

    protected lateinit var collectionControllerApi: CollectionControllerApi

    protected lateinit var balanceControllerApi: BalanceControllerApi

    protected lateinit var orderControllerApi: OrderControllerApi

    @PostConstruct
    fun setup() {
        val urlProvider = FixedSolanaApiServiceUriProvider(URI.create("http://127.0.0.1:$port"))
        val clientFactory = SolanaNftIndexerApiClientFactory(urlProvider, NoopWebClientCustomizer())
        tokenControllerApi = clientFactory.createTokenControllerApiClient()
        collectionControllerApi = clientFactory.createCollectionControllerApiClient()
        balanceControllerApi = clientFactory.createBalanceControllerApiClient()
        orderControllerApi = clientFactory.createOrderControllerApiClient()
    }

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

    suspend fun saveTokenWithMeta(
        mint: String = randomMint(),
        updatedAt: Instant = nowMillis(),
        onCollection: MetaplexMetaFields.Collection? = null,
        offCollection: MetaplexOffChainMetaFields.Collection? = null
    ): TokenWithMeta {
        val token = tokenRepository.save(createRandomToken(mint).copy(updatedAt = updatedAt))
        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(
            tokenAddress = token.mint,
            metaplexMetaCustomizer = {
                onCollection?.let { this.copy(metaFields = this.metaFields.copy(collection = onCollection)) } ?: this
            },
            metaplexOffChainMetaCustomizer = {
                offCollection?.let { this.copy(metaFields = this.metaFields.copy(collection = offCollection)) } ?: this
            }
        )
        return TokenWithMeta(token, tokenMeta)
    }
}
