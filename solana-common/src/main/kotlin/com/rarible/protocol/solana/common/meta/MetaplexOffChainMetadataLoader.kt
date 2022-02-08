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
class MetaplexOffChainMetadataLoader(
    private val externalHttpClient: ExternalHttpClient
) {
    private companion object {
        // TODO[meta]: add a configuration for this field.
        private val metadataRequestTimeout = Duration.ofSeconds(10)

        private val jacksonMapper = jacksonObjectMapper()
    }

    suspend fun loadOffChainMetadataJson(metadataUrl: URL): MetaplexOffChainMetadataJsonSchema {
        val jsonContent = externalHttpClient
            .get(metadataUrl)
            .bodyToMono<String>()
            // TODO[meta]: limit the size of the loaded JSON.
            .timeout(metadataRequestTimeout)
            .awaitFirst()
        return jacksonMapper.readValue(jsonContent)
    }
}
