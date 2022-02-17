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

    private val metaLoadingErrorCounter = Counter
        .builder(META_PARSING_ERROR)
        .tag("sync", "true")
        .register(meterRegistry)

    private val metaParsingErrorCounter = Counter
        .builder(META_PARSING_ERROR)
        .register(meterRegistry)

    fun onMetaLoadingError(
        tokenAddress: String,
        metadataUrl: String,
        exception: Exception
    ) {
        logger.error("Failed to load metadata for token $tokenAddress by URL", exception)
        metaLoadingErrorCounter.increment()
    }

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
        const val META_LOADING_ERROR = "meta_loading_error"
        const val META_PARSING_ERROR = "meta_parsing_error"
    }
}
