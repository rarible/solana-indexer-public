package com.rarible.protocol.solana.nft.listener.configuration

import com.mongodb.*
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.rarible.protocol.solana.nft.listener.block.cache.BlockCacheProperties
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.ReactiveMongoTemplate


@Configuration
class MongoConfiguration(
    private val mongoProperties: MongoProperties,
    private val blockCacheProperties: BlockCacheProperties,
) {
    @Primary
    @Bean
    fun reactiveMongoClient(): MongoClient {
        return MongoClients.create(createMongoClientSettings(mongoProperties))
    }

    @Primary
    @Bean
    fun reactiveMongoTemplate(): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(reactiveMongoClient(), mongoProperties.database)
    }

    @Bean
    fun reactiveMongoClientBlockCache(): MongoClient {
        return MongoClients.create(createMongoClientSettings(getBlockCacheMongoProperties()))
    }

    @Bean("reactiveMongoTemplateBlockCache")
    fun reactiveMongoTemplateBlockCache(): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(reactiveMongoClientBlockCache(), getBlockCacheMongoProperties().database)
    }

    private fun getBlockCacheMongoProperties(): MongoProperties {
        val properties = MongoProperties()
        properties.uri = blockCacheProperties.mongo?.uri ?: mongoProperties.uri
        properties.database = blockCacheProperties.mongo?.database ?: mongoProperties.database
        return properties
    }

    private fun createMongoClientSettings(mongoProperties: MongoProperties): MongoClientSettings {
        val connectionString = ConnectionString(mongoProperties.uri)
        return MongoClientSettings.builder()
            .readConcern(ReadConcern.DEFAULT)
            .writeConcern(WriteConcern.MAJORITY)
            .readPreference(ReadPreference.primary())
            .applyConnectionString(connectionString)
            .build()
    }
}