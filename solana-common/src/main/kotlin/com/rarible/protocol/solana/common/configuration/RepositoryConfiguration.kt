package com.rarible.protocol.solana.common.configuration

import com.rarible.core.mongo.configuration.EnableRaribleMongo
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.repository.MetaplexMetaRepository
import com.rarible.protocol.solana.common.repository.PackageRepository
import com.rarible.protocol.solana.common.repository.TokenOffChainCollectionRepository
import kotlinx.coroutines.runBlocking
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@EnableMongoAuditing
@EnableRaribleMongo
@EnableReactiveMongoRepositories(basePackageClasses = [PackageRepository::class])
@ComponentScan(basePackageClasses = [PackageRepository::class])
class RepositoryConfiguration {

    @Bean
    fun ensureIndexes(
        metaplexMetaRepository: MetaplexMetaRepository,
        tokenOffChainCollectionRepository: TokenOffChainCollectionRepository,
        balanceRepository: BalanceRepository
    ): CommandLineRunner {
        return CommandLineRunner {
            runBlocking {
                metaplexMetaRepository.createIndexes()
                balanceRepository.createIndexes()
                tokenOffChainCollectionRepository.createIndexes()
            }
        }
    }
}
