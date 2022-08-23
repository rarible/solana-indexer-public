package com.rarible.protocol.solana.common.service

import com.rarible.core.meta.resource.model.UrlResource
import com.rarible.core.meta.resource.parser.UrlParser
import com.rarible.core.meta.resource.resolver.UrlResolver
import com.rarible.core.meta.resource.util.MetaLogger.logMetaLoading
import org.springframework.stereotype.Component

@Component
class UrlService(
    private val urlParser : UrlParser,
    private val urlResolver: UrlResolver
) {
    fun parseUrl(url: String, id: String): UrlResource? {
        val resource = urlParser.parse(url)
        if (resource == null) {
            logMetaLoading(id = id, message = "UrlService: Cannot parse and resolve url: $url", warn = true)
        }
        return resource
    }

    // Used only for internal operations, such urls should NOT be stored anywhere
    fun resolveInternalHttpUrl(resource: UrlResource): String = urlResolver.resolveInternalUrl(resource)
}