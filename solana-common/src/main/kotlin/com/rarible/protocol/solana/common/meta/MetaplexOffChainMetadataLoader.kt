package com.rarible.protocol.solana.common.meta

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
    }

    suspend fun loadOffChainMetadataJson(metadataUrl: URL): String =
        externalHttpClient
            .get(metadataUrl)
            .bodyToMono<String>()
            // TODO[meta]: limit the size of the loaded JSON.
            .timeout(metadataRequestTimeout)
            .awaitFirst()
}
