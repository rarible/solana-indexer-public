package com.rarible.protocol.solana.nft.api.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.repository.SolanaBalanceRecordsRepository
import com.rarible.protocol.solana.common.repository.SolanaAuctionHouseOrderRecordsRepository
import com.rarible.protocol.solana.dto.ActivityFilterAllDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.nft.api.test.AbstractIntegrationTest
import com.rarible.protocol.solana.test.ActivityDataFactory
import com.rarible.protocol.solana.test.ActivityDataFactory.turnLog
import com.rarible.protocol.solana.test.randomSolanaLog
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ActivityApiServiceIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var balanceRecordsRepository: SolanaBalanceRecordsRepository

    @Autowired
    private lateinit var orderRecordsRepository: SolanaAuctionHouseOrderRecordsRepository

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
        balances.map { balanceRecordsRepository.save(it) }

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
        orders.map { orderRecordsRepository.save(it) }

        val allTypes = listOf(
            ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.BURN, ActivityFilterAllTypeDto.TRANSFER,
            ActivityFilterAllTypeDto.LIST, ActivityFilterAllTypeDto.CANCEL_LIST, ActivityFilterAllTypeDto.SELL,
        )

        allTypes.forEach { type ->
            val filter = ActivityFilterAllDto(listOf(type))
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(3, result.size)
        }

        ActivityFilterAllDto(listOf(ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.BURN)).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(6, result.size)
        }

        ActivityFilterAllDto(listOf(ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.LIST)).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(6, result.size)
        }

        ActivityFilterAllDto(emptyList()).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(0, result.size)
        }

        ActivityFilterAllDto(allTypes).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertEquals(18, result.size)
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
        balances.map { balanceRecordsRepository.save(it) }

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
        orders.map { orderRecordsRepository.save(it) }

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

    @Test
    fun `order activities formation`() = runBlocking<Unit> {
        val orderTypes = listOf(
            ActivityFilterByItemTypeDto.LIST, ActivityFilterByItemTypeDto.CANCEL_LIST,
            ActivityFilterByItemTypeDto.BID, ActivityFilterByItemTypeDto.CANCEL_BID,
            ActivityFilterByItemTypeDto.SELL,
        )
        val mint1 = randomString()
        val mint2 = randomString()

        val list = ActivityDataFactory.randomSellRecord()
        val cancelList = ActivityDataFactory.randomCancel(direction = OrderDirection.SELL)
        val simpleSell = ActivityDataFactory.randomExecuteSaleRecord(direction = OrderDirection.SELL)
        val fullSell = listOf(
            ActivityDataFactory.randomBuyRecord(mint = mint1),
            ActivityDataFactory.randomExecuteSaleRecord(mint = mint1, direction = OrderDirection.BUY),
            ActivityDataFactory.randomExecuteSaleRecord(mint = mint1, direction = OrderDirection.SELL),
        ).turnLog(randomSolanaLog())
        val fullSellActual = fullSell.last()

        val bid = ActivityDataFactory.randomBuyRecord()
        val cancelBid = ActivityDataFactory.randomCancel(direction = OrderDirection.BUY)
        val simpleAcceptBid = ActivityDataFactory.randomExecuteSaleRecord(direction = OrderDirection.BUY)
        val fullBid = listOf(
            ActivityDataFactory.randomSellRecord(mint = mint2),
            ActivityDataFactory.randomExecuteSaleRecord(mint = mint2, direction = OrderDirection.SELL),
            ActivityDataFactory.randomExecuteSaleRecord(mint = mint2, direction = OrderDirection.BUY),
        ).turnLog(randomSolanaLog())
        val fullAcceptBidActual = fullBid.last()

        val data = listOf(list, cancelList, simpleSell) + fullSell + listOf(bid, cancelBid, simpleAcceptBid) + fullBid
        data.map { orderRecordsRepository.save(it) }

        ActivityFilterByItemDto(list.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderListActivityDto)
            assertEquals(list.id, activity.id)
            assertEquals(list.timestamp, activity.date)
        }

        ActivityFilterByItemDto(cancelList.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderCancelListActivityDto)
            assertEquals(cancelList.id, activity.id)
            assertEquals(cancelList.timestamp, activity.date)
        }

        ActivityFilterByItemDto(simpleSell.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.SELL, (activity as OrderMatchActivityDto).type)
            assertEquals(simpleSell.id, activity.id)
            assertEquals(simpleSell.timestamp, activity.date)
        }

        ActivityFilterByItemDto(fullSellActual.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.SELL, (activity as OrderMatchActivityDto).type)
            assertEquals(fullSellActual.id, activity.id)
            assertEquals(fullSellActual.timestamp, activity.date)
        }

        ActivityFilterByItemDto(bid.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderBidActivityDto)
            assertEquals(bid.id, activity.id)
            assertEquals(bid.timestamp, activity.date)
        }

        ActivityFilterByItemDto(cancelBid.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderCancelBidActivityDto)
            assertEquals(cancelBid.id, activity.id)
            assertEquals(cancelBid.timestamp, activity.date)
        }

        ActivityFilterByItemDto(simpleAcceptBid.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.ACCEPT_BID, (activity as OrderMatchActivityDto).type)
            assertEquals(simpleAcceptBid.id, activity.id)
            assertEquals(simpleAcceptBid.timestamp, activity.date)
        }

        ActivityFilterByItemDto(fullAcceptBidActual.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertEquals(1, result.size)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.ACCEPT_BID, (activity as OrderMatchActivityDto).type)
            assertEquals(fullAcceptBidActual.id, activity.id)
            assertEquals(fullAcceptBidActual.timestamp, activity.date)
        }
    }
}
