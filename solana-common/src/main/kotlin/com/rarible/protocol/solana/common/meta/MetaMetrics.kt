package com.rarible.protocol.solana.common.meta

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MetaMetrics(
    private val meterRegistry: MeterRegistry
) {

    private val logger = LoggerFactory.getLogger(MetaMetrics::class.java)

    private val metaParsingErrorCounter = Counter
        .builder(META_PARSING_ERROR)
        .tag("sync", "true")
        .register(meterRegistry)

    fun onMetaParsingError(
        tokenAddress: String,
        metadataUrl: String,
        exception: Exception
    ) {
        logger.error("Failed to parse metadata for token $tokenAddress by URL", exception)
        metaParsingErrorCounter.increment()
    }

    fun reset() {
        meterRegistry.clear()
    }

    private companion object {
        const val META_PARSING_ERROR = "meta_parsing_error"
    }
}
