package com.rarible.solana.proxy

import com.rarible.solana.proxy.plugins.configureHTTP
import com.rarible.solana.proxy.plugins.configureRouting
import com.rarible.solana.proxy.plugins.configureSerialization
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSerialization()
        configureHTTP()
    }.start(wait = true)
}
