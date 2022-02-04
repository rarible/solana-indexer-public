package com.rarible.protocol.solana.common.meta

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.protocol.solana.common.model.TokenId
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URL
import java.time.Duration

@Component
class SolanaMetaLoader(
    private val externalHttpClient: ExternalHttpClient
) {
    private companion object {
        val metadataRequestTimeout: Duration = Duration.ofSeconds(10)

        val jacksonMapper = jacksonObjectMapper()
    }

    suspend fun loadMeta(tokenAddress: TokenId, metadataUrl: URL): SolanaMeta {
        logMetaLoading(tokenAddress, "loading meta by URL $metadataUrl")
        val metadataJsonContent = externalHttpClient
            .get(metadataUrl)
            .bodyToMono<String>()
            .timeout(metadataRequestTimeout)
            .awaitFirst()
        return jacksonMapper
            .readValue<SolanaMetaJsonSchema>(metadataJsonContent)
            .toMeta(metadataUrl = metadataUrl.toExternalForm())
    }

    private fun SolanaMetaJsonSchema.toMeta(metadataUrl: String): SolanaMeta =
        SolanaMeta(
            name = name,
            description = description,
            url = metadataUrl
        )
}
