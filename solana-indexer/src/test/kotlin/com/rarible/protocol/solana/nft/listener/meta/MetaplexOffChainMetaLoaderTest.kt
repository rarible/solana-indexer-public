package com.rarible.protocol.solana.nft.listener.meta

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.meta.ExternalHttpClient
import com.rarible.protocol.solana.common.meta.MetaplexOffChainCollectionHash
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetaLoader
import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant

class MetaplexOffChainMetaLoaderTest {

    private val externalHttpClient = ExternalHttpClient()
    private val metaplexOffChainMetaLoader = MetaplexOffChainMetaLoader(
        metaplexOffChainMetaRepository = mockk {
            coEvery { save(any()) } answers { firstArg() }
        },
        externalHttpClient = externalHttpClient,
        metaMetrics = mockk(),
        clock = mockk {
            every { instant() } returns Instant.EPOCH
        }
    )

    @Test
    fun `load meta`() = runBlocking<Unit> {
        val url = URL(
            "https://gist.githubusercontent.com/serejke/6e5c9e1cad75956f17d6059e3b1eaf98/raw/c85ac6ab1431e2fde06bc25954c79cc91445a3f8/meta-with-collection.json"
        )
        val tokenAddress = randomString()
        val metaplexOffChainMeta = metaplexOffChainMetaLoader.loadMetaplexOffChainMeta(tokenAddress, url)
        assertThat(metaplexOffChainMeta).isEqualTo(
            MetaplexOffChainMeta(
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
                                share = 100
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
        )
    }
}
