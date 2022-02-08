package com.rarible.protocol.solana.common.meta

import io.netty.channel.ChannelOption
import io.netty.channel.epoll.EpollChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.netty.transport.ProxyProvider
import java.net.URI
import java.net.URL
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Client responsible for making HTTP requests to external APIs.
 */
@Component
class ExternalHttpClient {

    protected val defaultClient = WebClient.builder().apply {
        ExternalHttpWebClientCustomizer().customize(it)
    }.build()

    fun get(url: String): WebClient.ResponseSpec = get(URI(url))

    fun get(url: URL): WebClient.ResponseSpec = get(url.toURI())

    fun get(url: URI): WebClient.ResponseSpec {
        val get = defaultClient.get()
        // May throw "invalid URL" exception.
        get.uri(url)
        return get.retrieve()
    }
}

// TODO[meta]: validate URL is correct (http/https, not a hack).
fun url(url: String) = URL(url)

private const val X_API_KEY = "X-API-KEY"
private val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(60)

private fun createConnector(
    connectTimeoutMs: Int,
    readTimeoutMs: Int,
    proxyUrl: String,
    @Suppress("SameParameterValue") followRedirect: Boolean
): ClientHttpConnector {
    val provider = ConnectionProvider.builder("protocol-default-open_sea-connection-provider")
        .maxConnections(200)
        .pendingAcquireMaxCount(-1)
        .maxIdleTime(DEFAULT_TIMEOUT)
        .maxLifeTime(DEFAULT_TIMEOUT)
        .lifo()
        .build()

    val tcpClient = reactor.netty.tcp.TcpClient.create(provider)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
        .doOnConnected {
            it.addHandlerLast(ReadTimeoutHandler(readTimeoutMs.toLong(), TimeUnit.MILLISECONDS))
        }

    if (proxyUrl.isNotBlank()) {
        val finalTcpClient = tcpClient.proxy {
            val uri = URI.create(proxyUrl)
            val user = uri.userInfo.split(":")
            it.type(ProxyProvider.Proxy.HTTP)
                .host(uri.host)
                .username(user[0])
                .password { user[1] }
                .port(uri.port)
        }
        return ReactorClientHttpConnector(
            HttpClient.from(finalTcpClient).followRedirect(followRedirect)
        )
    }

    return ReactorClientHttpConnector(
        HttpClient.from(tcpClient).followRedirect(followRedirect)
    )
}

private class ExternalHttpWebClientCustomizer : WebClientCustomizer {

    override fun customize(webClientBuilder: WebClient.Builder) {
        webClientBuilder.codecs { clientCodecConfigurer ->
            clientCodecConfigurer.defaultCodecs().maxInMemorySize(DEFAULT_MAX_BODY_SIZE)
        }
        val provider = ConnectionProvider.builder("solana-protocol-default-connection-provider")
            .maxConnections(200)
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(DEFAULT_TIMEOUT)
            .maxLifeTime(DEFAULT_TIMEOUT)
            .lifo()
            .build()

        val client = HttpClient
            .create(provider)
            .tcpConfiguration {
                it.option(ChannelOption.SO_KEEPALIVE, true)
                    .option(EpollChannelOption.TCP_KEEPIDLE, 300)
                    .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                    .option(EpollChannelOption.TCP_KEEPCNT, 8)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIMEOUT_MILLIS.toInt())
                    .doOnConnected { connection ->
                        connection.addHandlerLast(ReadTimeoutHandler(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                        connection.addHandlerLast(WriteTimeoutHandler(DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                    }
            }
            .responseTimeout(DEFAULT_TIMEOUT)
            .followRedirect(true)

        val connector = ReactorClientHttpConnector(client)

        webClientBuilder.clientConnector(connector)
    }

    companion object {
        val DEFAULT_MAX_BODY_SIZE = DataSize.ofMegabytes(10).toBytes().toInt()
        val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(30)
        val DEFAULT_TIMEOUT_MILLIS: Long = DEFAULT_TIMEOUT.toMillis()
    }
}
