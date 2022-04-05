package com.rarible.protocol.solana.nft.api.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.dto.AssetDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import com.rarible.protocol.solana.test.activityClassByType
import com.rarible.protocol.solana.test.randomBid
import com.rarible.protocol.solana.test.randomBurn
import com.rarible.protocol.solana.test.randomCancelBid
import com.rarible.protocol.solana.test.randomCancelList
import com.rarible.protocol.solana.test.randomList
import com.rarible.protocol.solana.test.randomMint
import com.rarible.protocol.solana.test.randomSell
import com.rarible.protocol.solana.test.randomTransfer
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

class ActivityApiServiceIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var activityRepository: ActivityRepository

    @Autowired
    private lateinit var activityApiService: ActivityApiService

    @Test
    fun `find by type`() = runBlocking<Unit> {
        val itemId = randomString()

        val mint = randomMint(tokenAddress = itemId)
        val burn = randomBurn(tokenAddress = itemId)
        val transfer = randomTransfer(tokenAddress = itemId)
        val list = randomList(make = AssetDto(SolanaNftAssetTypeDto(itemId), BigDecimal.ONE))
        val cancelList = randomCancelList(make = SolanaNftAssetTypeDto(itemId))
        val bid = randomBid(take = AssetDto(SolanaNftAssetTypeDto(itemId), BigDecimal.ONE))
        val cancelBid = randomCancelBid(take = SolanaNftAssetTypeDto(itemId))
        val sell = randomSell(nft = AssetDto(SolanaNftAssetTypeDto(itemId), BigDecimal.ONE))

        listOf(mint, burn, transfer, list, cancelList, bid, cancelBid, sell).forEach { activityRepository.save(it) }

        ActivityFilterAllTypeDto.values().forEach { type ->
            val filter = ActivityFilterAllDto(listOf(type))
            val result = activityApiService.getAllActivities(filter, null, 50, true)
            assertThat(result).hasSize(1)
            assertThat(result.single()).isInstanceOf(activityClassByType(type))
        }

        ActivityFilterByItemTypeDto.values().forEach { type ->
            val filter = ActivityFilterByItemDto(itemId, listOf(type))
            val result = activityApiService.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(1)
            assertThat(result.single()).isInstanceOf(activityClassByType(type))
        }

    }

}
