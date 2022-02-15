package com.rarible.protocol.solana.common.configuration

import com.rarible.core.mongo.configuration.EnableRaribleMongo
import com.rarible.protocol.solana.common.repository.MetaRepository
import com.rarible.protocol.solana.common.repository.PackageRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@EnableMongoAuditing
@EnableRaribleMongo
@EnableReactiveMongoRepositories(basePackageClasses = [PackageRepository::class])
@ComponentScan(basePackageClasses = [PackageRepository::class])
class RepositoryConfiguration {

    @Bean
    fun ensureIndexes(
        metaRepository: MetaRepository
    ): CommandLineRunner {
        val logger = LoggerFactory.getLogger(RepositoryConfiguration::class.java)
        return CommandLineRunner {
            runBlocking {
                metaRepository.createIndexes()
            }
        }
    }
}
