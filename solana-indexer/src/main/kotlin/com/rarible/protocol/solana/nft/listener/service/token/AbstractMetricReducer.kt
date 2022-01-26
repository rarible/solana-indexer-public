package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.core.entity.reducer.service.Reducer
import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import java.util.concurrent.ConcurrentHashMap

abstract class AbstractMetricReducer<Event, E>(
    private val meterRegistry: MeterRegistry,
    properties: NftIndexerProperties,
    prefix: String
) : Reducer<Event, E> {

    private val fullPrefix = "${properties.metricRootPath}.reduce.$prefix"
    private val counters = ConcurrentHashMap<Class<out Event>, Counter>()

    override suspend fun reduce(entity: E, event: Event): E {
        counters.computeIfAbsent(requireNotNull(event)::class.java) {
            createCounter(getMetricName(event))
        }.increment()

        return entity
    }

    protected abstract fun getMetricName(event: Event): String

    private fun createCounter(metricName: String): Counter {
        return Counter.builder("$fullPrefix.$metricName")
            .tag("blockchain", "solana")
            .register(meterRegistry)
    }

    override fun toString(): String {
        return "${this.javaClass.name}(solana)"
    }
}