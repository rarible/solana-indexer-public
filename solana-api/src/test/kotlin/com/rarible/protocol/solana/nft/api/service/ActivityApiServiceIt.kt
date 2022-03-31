package com.rarible.protocol.solana.nft.api.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.repository.RecordsBalanceRepository
import com.rarible.protocol.solana.common.repository.RecordsOrderRepository
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import com.rarible.protocol.solana.test.ActivityDataFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ActivityApiServiceIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var recordsBalanceRepository: RecordsBalanceRepository

    @Autowired
    private lateinit var recordsOrderRepository: RecordsOrderRepository

    @Autowired
    private lateinit var service: ActivityApiService

    @Test
    fun `all activities  filter by types`() = runBlocking<Unit> {
        val balances = listOf(
            ActivityDataFactory.randomMintToRecord(),
            ActivityDataFactory.randomMintToRecord(),
            ActivityDataFactory.randomMintToRecord(),
            ActivityDataFactory.randomBurnRecord(),
            ActivityDataFactory.randomBurnRecord(),
            ActivityDataFactory.randomBurnRecord(),
            ActivityDataFactory.randomIncomeRecord(),
            ActivityDataFactory.randomIncomeRecord(),
            ActivityDataFactory.randomIncomeRecord(),
            ActivityDataFactory.randomOutcomeRecord(),
            ActivityDataFactory.randomOutcomeRecord(),
            ActivityDataFactory.randomOutcomeRecord(),
        )
        balances.map { recordsBalanceRepository.save(it) }

        val orders = listOf(
            ActivityDataFactory.randomBuyRecord(),
            ActivityDataFactory.randomBuyRecord(),
            ActivityDataFactory.randomBuyRecord(),
            ActivityDataFactory.randomCancel(),
            ActivityDataFactory.randomCancel(),
            ActivityDataFactory.randomCancel(),
            ActivityDataFactory.randomSellRecord(),
            ActivityDataFactory.randomSellRecord(),
            ActivityDataFactory.randomSellRecord(),
            ActivityDataFactory.randomExecuteSaleRecord(),
            ActivityDataFactory.randomExecuteSaleRecord(),
            ActivityDataFactory.randomExecuteSaleRecord(),
        )
        orders.map { recordsOrderRepository.save(it) }

        val allTypes = listOf(
            ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.BURN, ActivityFilterAllTypeDto.TRANSFER,
            ActivityFilterAllTypeDto.LIST, ActivityFilterAllTypeDto.CANCEL_LIST, ActivityFilterAllTypeDto.SELL,
        )

        allTypes.forEach { type ->
            val filter = ActivityFilterAllDto(listOf(type))
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(3, result.size)
            println(result)
        }

        ActivityFilterAllDto(listOf(ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.BURN)).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(6, result.size)
            println(result)
        }

        ActivityFilterAllDto(listOf(ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.LIST)).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(6, result.size)
            println(result)
        }

        ActivityFilterAllDto(emptyList()).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(0, result.size)
        }

        ActivityFilterAllDto(allTypes).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(18, result.size)
            println(result)
        }
    }

    @Test
    fun `byItem activities  filter by types`() = runBlocking<Unit> {
        val mint = randomString()

        val balances = listOf(
            ActivityDataFactory.randomMintToRecord(mint = mint),
            ActivityDataFactory.randomMintToRecord(),
            ActivityDataFactory.randomMintToRecord(),
            ActivityDataFactory.randomBurnRecord(mint = mint),
            ActivityDataFactory.randomBurnRecord(),
            ActivityDataFactory.randomBurnRecord(),
            ActivityDataFactory.randomIncomeRecord(mint = mint),
            ActivityDataFactory.randomIncomeRecord(),
            ActivityDataFactory.randomIncomeRecord(),
            ActivityDataFactory.randomOutcomeRecord(mint = mint),
            ActivityDataFactory.randomOutcomeRecord(),
            ActivityDataFactory.randomOutcomeRecord(),
        )
        balances.map { recordsBalanceRepository.save(it) }

        val orders = listOf(
            ActivityDataFactory.randomBuyRecord(mint = mint),
            ActivityDataFactory.randomBuyRecord(),
            ActivityDataFactory.randomBuyRecord(),
            ActivityDataFactory.randomCancel(mint = mint),
            ActivityDataFactory.randomCancel(),
            ActivityDataFactory.randomCancel(),
            ActivityDataFactory.randomSellRecord(mint = mint),
            ActivityDataFactory.randomSellRecord(),
            ActivityDataFactory.randomSellRecord(),
            ActivityDataFactory.randomExecuteSaleRecord(mint = mint),
            ActivityDataFactory.randomExecuteSaleRecord(),
            ActivityDataFactory.randomExecuteSaleRecord(),
        )
        orders.map { recordsOrderRepository.save(it) }

        val allTypes = listOf(
            ActivityFilterByItemTypeDto.MINT, ActivityFilterByItemTypeDto.BURN, ActivityFilterByItemTypeDto.TRANSFER,
            ActivityFilterByItemTypeDto.LIST, ActivityFilterByItemTypeDto.CANCEL_LIST, ActivityFilterByItemTypeDto.SELL,
        )

        allTypes.forEach { type ->
            val filter = ActivityFilterByItemDto(mint, listOf(type))
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            println(result)
        }

        ActivityFilterByItemDto(mint,
            listOf(ActivityFilterByItemTypeDto.MINT, ActivityFilterByItemTypeDto.BURN)).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(2, result.size)
            println(result)
        }

        ActivityFilterByItemDto(mint,
            listOf(ActivityFilterByItemTypeDto.MINT, ActivityFilterByItemTypeDto.LIST)).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(2, result.size)
            println(result)
        }

        ActivityFilterByItemDto(mint, emptyList()).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(0, result.size)
        }

        ActivityFilterByItemDto(mint, allTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(6, result.size)
            println(result)
        }
    }
}
