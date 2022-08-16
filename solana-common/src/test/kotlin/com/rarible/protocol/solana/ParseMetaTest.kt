package com.rarible.protocol.solana

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rarible.protocol.solana.common.meta.MetaplexOffChainMetadataJsonSchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ParseMetaTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `Collection is string`() {
        val meta = mapper.readValue<MetaplexOffChainMetadataJsonSchema>(readFile("9hz2ABiFx149Hz5LVDAsafok7DMhwd9J6iHcZgKgUBuh.json"))

        assertEquals("EsKvdYEGgyXop1M3Quif27AzGvzQSBwqRV9Y45NsTmtZ", meta.collection?.name)
        assertNull(meta.collection?.family)
    }

    @Test
    fun `Collection is object`() {
        val meta = mapper.readValue<MetaplexOffChainMetadataJsonSchema>(readFile("yYUPr1ju4fHgyosBFhCGTCzneAv7sAzQbLTtkCrzq7j.json"))

        assertEquals("The Court 2.3.2", meta.collection?.name)
        assertEquals("LazyBearZ", meta.collection?.family)
    }

    @Test
    fun `Collection is array of objects`() {
       val meta = mapper.readValue<MetaplexOffChainMetadataJsonSchema>(readFile("JBMXQrd1Ww7P3uBGW9Rdz1RsGi347EWZ9rq7HGCaDLcc.json"))

        assertEquals("SolAngel", meta.collection?.name)
        assertEquals("SolAngel", meta.collection?.family)
    }

    @Test
    fun `Collection is object inside properties`() {
        val meta = mapper.readValue<MetaplexOffChainMetadataJsonSchema>(readFile("wyaFmb9C8uFgrTD2oRoAuWPpmCJCnqLRK7aW8ARR3yW.json"))

        assertNull(meta.collection)
        assertEquals("The MisFit Derby(CCC)", meta.properties?.collection?.name)
        assertEquals("Corrupt Camel", meta.properties?.collection?.family)
    }

    private fun readFile(name: String): String =
        this.javaClass.classLoader.getResourceAsStream("meta/$name").bufferedReader().readText()
}