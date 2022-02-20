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
        .builder(META_LOADING_ERROR)
        .register(meterRegistry)

    private val metaParsingErrorCounter = Counter
        .builder(META_PARSING_ERROR)
        .register(meterRegistry)

    fun onMetaLoadingError() {
        metaLoadingErrorCounter.increment()
    }

    fun onMetaParsingError() {
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
