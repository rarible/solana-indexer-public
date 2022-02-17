package com.rarible.protocol.solana.nft.listener.meta

import com.rarible.protocol.solana.common.meta.ExternalHttpClient
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetadataJsonSchema
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetadataLoader
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

class MetaplexOffChainMetadataJsonSchemaLoaderTest {

    private val externalHttpClient = ExternalHttpClient()
    private val metaplexOffChainMetadataLoader = MetaplexOffChainMetadataLoader(externalHttpClient)

    @Test
    fun `load meta`() = runBlocking<Unit> {
        val url = URL(
            "https://gist.githubusercontent.com/serejke/6e5c9e1cad75956f17d6059e3b1eaf98/raw/c85ac6ab1431e2fde06bc25954c79cc91445a3f8/meta-with-collection.json"
        )
        val tokenMeta = metaplexOffChainMetadataLoader.loadOffChainMetadataJson(url)
        assertThat(tokenMeta).isEqualTo(
            MetaplexOffChainMetadataJsonSchema(
                name = "My NFT #1",
                symbol = "MY_SYMBOL",
                seller_fee_basis_points = 420,
                external_url = "",
                edition = "Class of 2021",
                description = "My description",
                background_color = "000000",
                collection = MetaplexOffChainMetadataJsonSchema.Collection(
                    name = "Collection name",
                    family = "Collection family"
                ),
                attributes = listOf(
                    MetaplexOffChainMetadataJsonSchema.Attribute(
                        trait_type = "Background",
                        value = "Blue"
                    )
                ),
                properties = MetaplexOffChainMetadataJsonSchema.Properties(
                    category = "image",
                    creators = listOf(
                        MetaplexOffChainMetadataJsonSchema.Properties.Creator(
                            address = "6G7AqEUxwbyHJtKA3aHL7SbiijGqznuvNRDb2hG7uwA4",
                            share = 100
                        )
                    ),
                    files = listOf(
                        MetaplexOffChainMetadataJsonSchema.Properties.File(
                            uri = "https://arweave.net/fRfJ7WvuCxmSub9uD41hU8_WOAaJRzLUpepmp7KUirk?ext=png",
                            type = "image/png"
                        )
                    ),
                ),
                image = "https://arweave.net/fRfJ7WvuCxmSub9uD41hU8_WOAaJRzLUpepmp7KUirk",
                animation_url = null
            )
        )
    }
}
