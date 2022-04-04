package com.rarible.protocol.solana.nft.migration.configuration

import com.github.cloudyrock.spring.v5.EnableMongock
import com.rarible.core.mongo.configuration.EnableRaribleMongo
import org.springframework.context.annotation.Configuration

@EnableMongock
@Configuration
@EnableRaribleMongo
class NftMigrationConfiguration
