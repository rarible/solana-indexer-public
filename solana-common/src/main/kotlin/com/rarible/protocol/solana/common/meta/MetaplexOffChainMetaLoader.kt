package com.rarible.protocol.solana.common.meta

import com.rarible.core.meta.resource.util.MetaLogger.logMetaLoading
import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.TokenId
import com.rarible.protocol.solana.common.repository.MetaplexOffChainMetaRepository
import com.rarible.protocol.solana.common.service.UrlService
import com.rarible.protocol.solana.common.util.nowMillis
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import java.net.URL
import java.time.Clock
import java.time.Duration
import java.util.concurrent.TimeoutException

@Component
class MetaplexOffChainMetaLoader(
    private val metaplexOffChainMetaRepository: MetaplexOffChainMetaRepository,
    private val externalHttpClient: ExternalHttpClient,
    private val metaMetrics: MetaMetrics,
    private val solanaIndexerProperties: SolanaIndexerProperties,
    private val clock: Clock,
    private val urlService: UrlService
) {

    private companion object {

        private val logger = LoggerFactory.getLogger(MetaplexOffChainMetaLoader::class.java)
    }

    private suspend fun loadOffChainMetadataJson(metadataUrl: URL): String =
        externalHttpClient
            .get(metadataUrl)
            .bodyToMono<String>()
            // TODO[meta]: limit the size of the loaded JSON.
            .timeout(Duration.ofMillis(solanaIndexerProperties.metaplexOffChainMetaLoadingTimeout))
            .awaitFirst()

    suspend fun loadMetaplexOffChainMeta(
        tokenAddress: TokenId,
        metaplexMetaFields: MetaplexMetaFields
    ): MetaplexOffChainMeta? {
        val resource = urlService.parseUrl(metaplexMetaFields.uri, tokenAddress)
        if (resource == null) {
            metaMetrics.onMetaLoadingError()

            throw MetaUnparseableLinkException(
                "Can't parse metadata URL for token $tokenAddress: ${metaplexMetaFields.uri.take(1024)}"
            )
        }
        val internalUrl = urlService.resolveInternalHttpUrl(resource)

        if (internalUrl == resource.original) {
            logMetaLoading(tokenAddress, "Fetching property string by URL $internalUrl")
        } else {
            logMetaLoading(
                tokenAddress, "Fetching property string by URL $internalUrl (original URL is ${resource.original})"
            )
        }

        val metadataUrl = try {
            URL(TokenMetaParser.amendUrl(internalUrl)) // TODO: remove workaround
        } catch (e: Exception) {
            logMetaLoading(tokenAddress, "Wrong URL: $internalUrl, $e")
            metaMetrics.onMetaLoadingError()

            throw MetaUnparseableLinkException(
                "Invalid metadata URL for token $tokenAddress: ${metaplexMetaFields.uri.take(1024)}"
            )
        }

        // TODO: when meta gets changed, we have to send a token update event.
        logger.info("Loading off-chain metadata for token $tokenAddress by URL $metadataUrl")
        val offChainMetadataJsonContent = try {
            loadOffChainMetadataJson(metadataUrl)
        } catch (e: TimeoutException) {
            val message = "Timeout during loading metadata for token $tokenAddress by URL $metadataUrl"

            logger.error(message, e)
            metaMetrics.onMetaLoadingError()
            throw MetaTimeoutException(message)
        } catch (e: WebClientResponseException) {
            if (e.statusCode.is4xxClientError) {
                val message = "Metadata for token $tokenAddress by URL $metadataUrl not found"

                logger.error(message, e)
                metaMetrics.onMetaLoadingError()

                return null
            } else {
                val message = "Failed to load metadata for token $tokenAddress by URL $metadataUrl"

                logger.error(message, e)
                metaMetrics.onMetaLoadingError()

                error(message)
            }
        }

        val metaplexOffChainMetaFields = try {
            MetaplexOffChainMetadataParser.parseMetaplexOffChainMetaFields(
                offChainMetadataJsonContent = offChainMetadataJsonContent,
                metaplexMetaFields = metaplexMetaFields
            )
        } catch (e: Exception) {
            val message = "Failed to parse metadata for token $tokenAddress by URL $metadataUrl"
            logger.error(message, e)
            metaMetrics.onMetaParsingError()

            throw MetaUnparseableJsonException(message)
        }
        val metaplexOffChainMeta = MetaplexOffChainMeta(
            tokenAddress = tokenAddress,
            metaFields = metaplexOffChainMetaFields,
            loadedAt = clock.nowMillis()
        )

        return metaplexOffChainMetaRepository.save(metaplexOffChainMeta)
    }

}
