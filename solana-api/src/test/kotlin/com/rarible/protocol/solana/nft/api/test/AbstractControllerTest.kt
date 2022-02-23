package com.rarible.protocol.solana.nft.api.test

import com.rarible.protocol.solana.common.meta.TokenMetaService
import com.rarible.protocol.solana.nft.api.service.BalanceApiService
import com.rarible.protocol.solana.nft.api.service.TokenApiService
import com.rarible.solana.protocol.api.client.BalanceControllerApi
import com.rarible.solana.protocol.api.client.FixedSolanaApiServiceUriProvider
import com.rarible.solana.protocol.api.client.NoopWebClientCustomizer
import com.rarible.solana.protocol.api.client.SolanaNftIndexerApiClientFactory
import com.rarible.solana.protocol.api.client.TokenControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import java.net.URI
import javax.annotation.PostConstruct

@Import(ControllersTestConfiguration::class)
abstract class AbstractControllerTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var testTokenApiService: TokenApiService

    @Autowired
    lateinit var testTokenMetaService: TokenMetaService

    @Autowired
    lateinit var testBalanceApiService: BalanceApiService

    protected lateinit var tokenControllerApi: TokenControllerApi

    protected lateinit var balanceControllerApi: BalanceControllerApi

    @PostConstruct
    fun setup() {
        val urlProvider = FixedSolanaApiServiceUriProvider(URI.create("http://127.0.0.1:$port"))
        val clientFactory = SolanaNftIndexerApiClientFactory(urlProvider, NoopWebClientCustomizer())
        tokenControllerApi = clientFactory.createTokenControllerApiClient()
        balanceControllerApi = clientFactory.createBalanceControllerApiClient()
    }
}
