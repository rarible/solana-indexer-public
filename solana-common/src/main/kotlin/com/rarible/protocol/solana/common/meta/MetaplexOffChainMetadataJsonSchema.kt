package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode

// TODO[meta]: consider renaming fields without _
@JsonIgnoreProperties(ignoreUnknown = true)
data class MetaplexOffChainMetadataJsonSchema(
    val name: String,
    val symbol: String = "",
    val description: String?,
    @JsonDeserialize(using = CollectionDeserializer::class)
    val collection: Collection?,
    val seller_fee_basis_points: Int?,
    val external_url: String?,
    val edition: String?,
    val background_color: String?,
    val attributes: List<Attribute>?,
    val properties: Properties?,
    val image: String?,
    val animation_url: String?
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Collection(
        val name: String,
        val family: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Attribute(
        val trait_type: String?,
        val value: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Properties(
        val category: String?,
        val creators: List<Creator>?,
        // Non-standard, used in some collections.
        val collection: String?,
        val files: List<File>?
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        data class Creator(
            val address: String,
            val share: Int
        )

        @JsonIgnoreProperties(ignoreUnknown = true)
        data class File(
            val uri: String?,
            val type: String?
        )
    }
}

private class CollectionDeserializer : JsonDeserializer<MetaplexOffChainMetadataJsonSchema.Collection>() {
    override fun deserialize(
        parser: JsonParser,
        context: DeserializationContext
    ): MetaplexOffChainMetadataJsonSchema.Collection {
        return when (val node = parser.codec.readTree<JsonNode>(parser)) {
            is ObjectNode -> MetaplexOffChainMetadataJsonSchema.Collection(
                name = node["name"].textValue(),
                family = node["family"]?.textValue(),
            )

            is TextNode -> MetaplexOffChainMetadataJsonSchema.Collection(
                name = node.textValue(),
                family = null
            )

            else -> error("Failed to parse $node")
        }
    }
}