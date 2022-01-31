package com.rarible.protocol.solana.common.configuration

import com.rarible.core.mongo.configuration.EnableRaribleMongo
import com.rarible.protocol.solana.common.repository.PackageRepository
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories

@EnableMongoAuditing
@EnableRaribleMongo
@EnableReactiveMongoRepositories(basePackageClasses = [PackageRepository::class])
@ComponentScan(basePackageClasses = [PackageRepository::class])
class RepositoryConfiguration
