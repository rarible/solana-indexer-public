package com.rarible.protocol.solana.nft.listener.meta

import com.rarible.core.meta.resource.parser.UrlParser
import com.rarible.core.meta.resource.resolver.ConstantGatewayProvider
import com.rarible.core.meta.resource.resolver.IpfsGatewayResolver
import com.rarible.core.meta.resource.resolver.LegacyIpfsGatewaySubstitutor
import com.rarible.core.meta.resource.resolver.RandomGatewayProvider
import com.rarible.core.meta.resource.resolver.UrlResolver
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.meta.ExternalHttpClient
import com.rarible.protocol.solana.common.meta.MetaUnparseableJsonException
import com.rarible.protocol.solana.common.meta.MetaUnparseableLinkException
import com.rarible.protocol.solana.common.meta.MetaplexOffChainCollectionHash
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetaLoader
import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.service.CollectionService
import com.rarible.protocol.solana.common.service.UrlService
import com.rarible.protocol.solana.test.createRandomMetaplexMeta
import com.rarible.protocol.solana.test.randomMint
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URL
import java.time.Instant

class MetaplexOffChainMetaLoaderTest {

    private val externalHttpClient = ExternalHttpClient()
    private val collectionService: CollectionService = mockk()
    private val metaplexOffChainMetaLoader = MetaplexOffChainMetaLoader(
        metaplexOffChainMetaRepository = mockk {
            coEvery { save(any()) } answers { firstArg() }
        },
        externalHttpClient = externalHttpClient,
        solanaIndexerProperties = mockk<SolanaIndexerProperties>().apply { every { this@apply.metaplexOffChainMetaLoadingTimeout } returns 20000 },
        metaMetrics = mockk {
            every { onMetaLoadingError() } returns Unit
            every { onMetaParsingError() } returns Unit
        },
        clock = mockk {
            every { instant() } returns Instant.EPOCH
        },
        urlService = UrlService(
            UrlParser(),
            UrlResolver(
                IpfsGatewayResolver(
                    ConstantGatewayProvider("https://ipfs.io"),
                    RandomGatewayProvider(listOf("https://ipfs.io")),
                    customGatewaysResolver = LegacyIpfsGatewaySubstitutor(emptyList())
                )
            )
        )
    )

    @Test
    fun `load meta`() = runBlocking<Unit> {
        val url = URL(
            "https://gist.githubusercontent.com/serejke/6e5c9e1cad75956f17d6059e3b1eaf98/raw/c85ac6ab1431e2fde06bc25954c79cc91445a3f8/meta-with-collection.json"
        )

        val metaplexMeta = createRandomMetaplexMeta().let {
            it.copy(
                metaFields = it.metaFields.copy(
                    uri = url.toExternalForm(),
                    creators = listOf(
                        MetaplexTokenCreator(
                            address = "6G7AqEUxwbyHJtKA3aHL7SbiijGqznuvNRDb2hG7uwA4",
                            share = 100,
                            verified = true
                        )
                    )
                )
            )
        }

        val tokenAddress = randomMint()
        val expected = MetaplexOffChainMeta(
            tokenAddress = tokenAddress,
            metaFields = MetaplexOffChainMetaFields(
                name = "My NFT #1",
                symbol = "MY_SYMBOL",
                sellerFeeBasisPoints = 420,
                externalUrl = "",
                edition = "Class of 2021",
                description = "My description",
                backgroundColor = "000000",
                collection = MetaplexOffChainMetaFields.Collection(
                    name = "Collection name",
                    family = "Collection family",
                    hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                        name = "Collection name",
                        family = "Collection family",
                        creators = listOf("6G7AqEUxwbyHJtKA3aHL7SbiijGqznuvNRDb2hG7uwA4")
                    )
                ),
                attributes = listOf(
                    MetaplexOffChainMetaFields.Attribute(
                        traitType = "Background",
                        value = "Blue"
                    )
                ),
                properties = MetaplexOffChainMetaFields.Properties(
                    category = "image",
                    creators = listOf(
                        MetaplexTokenCreator(
                            address = "6G7AqEUxwbyHJtKA3aHL7SbiijGqznuvNRDb2hG7uwA4",
                            share = 100,
                            verified = false
                        )
                    ),
                    files = listOf(
                        MetaplexOffChainMetaFields.Properties.File(
                            uri = "https://arweave.net/fRfJ7WvuCxmSub9uD41hU8_WOAaJRzLUpepmp7KUirk?ext=png",
                            type = "image/png"
                        )
                    ),
                ),
                image = "https://arweave.net/fRfJ7WvuCxmSub9uD41hU8_WOAaJRzLUpepmp7KUirk",
                animationUrl = null
            ),
            loadedAt = Instant.EPOCH
        )

        coEvery { collectionService.saveCollectionV1(expected) } returns null

        val metaplexOffChainMeta = metaplexOffChainMetaLoader.loadMetaplexOffChainMeta(
            tokenAddress = tokenAddress,
            metaplexMetaFields = metaplexMeta.metaFields
        )

        assertThat(metaplexOffChainMeta).isEqualTo(expected)
    }

    @Test
    fun `load meta with non standard properties-collection`() = runBlocking<Unit> {
        val url = URL(
            "https://arweave.net/EHWKTkuer3stMM57HQqW8jzYpdn549M4cAvQ4KsTwPA"
        )

        val metaplexMeta = createRandomMetaplexMeta().let {
            it.copy(
                metaFields = it.metaFields.copy(
                    uri = url.toExternalForm(),
                    creators = listOf(
                        MetaplexTokenCreator(
                            address = "6G7AqEUxwbyHJtKA3aHL7SbiijGqznuvNRDb2hG7uwA4",
                            share = 100,
                            verified = true
                        )
                    )
                )
            )
        }
        val metaplexOffChainMeta = metaplexOffChainMetaLoader.loadMetaplexOffChainMeta(
            tokenAddress = randomMint(),
            metaplexMetaFields = metaplexMeta.metaFields
        )
        val collection = metaplexOffChainMeta?.metaFields?.collection
        assertThat(collection).isEqualTo(
            MetaplexOffChainMetaFields.Collection(
                name = "Degeniverse Eggs",
                family = null,
                hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                    name = "Degeniverse Eggs",
                    family = null,
                    creators = listOf("6G7AqEUxwbyHJtKA3aHL7SbiijGqznuvNRDb2hG7uwA4")
                )
            )
        )
    }

    @Test
    fun `not found error`() = runBlocking {
        val url = URL("https://arweave.net/notfound")
        val metaplexMeta = createRandomMetaplexMeta().let {
            it.copy(
                metaFields = it.metaFields.copy(
                    uri = url.toExternalForm()
                )
            )
        }

        val metaplexOffChainMeta = metaplexOffChainMetaLoader.loadMetaplexOffChainMeta(
            tokenAddress = randomMint(),
            metaplexMetaFields = metaplexMeta.metaFields
        )

        assertNull(metaplexOffChainMeta)
    }

    @Test
    fun `unparseable link`() = runBlocking<Unit> {
        val metaplexMeta = createRandomMetaplexMeta().let {
            it.copy(
                metaFields = it.metaFields.copy(
                    uri = "notvalidschema://meta"
                )
            )
        }

        assertThrows<MetaUnparseableLinkException> {
            metaplexOffChainMetaLoader.loadMetaplexOffChainMeta(
                tokenAddress = randomMint(),
                metaplexMetaFields = metaplexMeta.metaFields
            )
        }
    }

    @Test
    fun `unparseable json`() = runBlocking<Unit> {
        val metaplexMeta = createRandomMetaplexMeta().let {
            it.copy(
                metaFields = it.metaFields.copy(
                    uri = "https://gist.github.com/enslinmike/332b0805282271ee80e1947742072e87"
                )
            )
        }

        assertThrows<MetaUnparseableJsonException> {
            metaplexOffChainMetaLoader.loadMetaplexOffChainMeta(
                tokenAddress = randomMint(),
                metaplexMetaFields = metaplexMeta.metaFields
            )
        }
    }
}
