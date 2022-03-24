package com.rarible.protocol.solana.nft.listener.block.cache

import com.rarible.blockchain.scanner.solana.client.SolanaHttpRpcApi
import com.rarible.blockchain.scanner.solana.client.dto.GetBlockRequest
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class BlockCacheClient(
    private val urls: List<String>,
    timeoutMillis: Long = SolanaHttpRpcApi.DEFAULT_TIMEOUT,
) {
    private val client = WebClient.builder().apply {
        SolanaRpcApiWebClientCustomizer(
            timeout = Duration.ofMillis(timeoutMillis),
            maxBodySize = SolanaHttpRpcApi.MAX_BODY_SIZE
        ).customize(it)
    }.build()

    private val uri
        get() = urls[Random.nextInt(urls.size)]

    suspend fun getBlock(slot: Long, details: GetBlockRequest.TransactionDetails): ByteArray = client.post()
        .uri(uri)
        .body(BodyInserters.fromValue(GetBlockRequest(slot, details)))
        .retrieve()
        .bodyToMono<ByteArrayResource>()
        .map { it.byteArray }
        .awaitSingle()

}

private class SolanaRpcApiWebClientCustomizer(
    private val timeout: Duration,
    private val maxBodySize: Int
) : WebClientCustomizer {

    override fun customize(webClientBuilder: WebClient.Builder) {
        webClientBuilder.codecs { clientCodecConfigurer ->
            clientCodecConfigurer.defaultCodecs().maxInMemorySize(maxBodySize)
        }
        val provider = ConnectionProvider.builder("solana-connection-provider")
            .maxConnections(200)
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(timeout)
            .maxLifeTime(timeout)
            .lifo()
            .build()

        val client = HttpClient
            .create(provider)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(timeout.toMillis(), TimeUnit.MILLISECONDS))
                it.addHandlerLast(WriteTimeoutHandler(timeout.toMillis(), TimeUnit.MILLISECONDS))
            }
            .compress(true)
            .responseTimeout(timeout)
            .followRedirect(true)

        val connector = ReactorClientHttpConnector(client)
        webClientBuilder
            .clientConnector(connector)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    }
}
