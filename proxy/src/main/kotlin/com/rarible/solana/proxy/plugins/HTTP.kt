package com.rarible.solana.proxy.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configureHTTP() {
    install(Compression) {
        gzip {
            matchContentType(ContentType.Any)
        }
    }
}
