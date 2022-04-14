package com.rarible.solana.proxy.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*

fun Application.configureHTTP() {
    install(Compression) {
        gzip {
            matchContentType(ContentType.Any)
        }
    }
}
