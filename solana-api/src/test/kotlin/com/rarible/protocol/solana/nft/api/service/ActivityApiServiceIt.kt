package com.rarible.protocol.solana.nft.api.service

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.records.OrderDirection
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.repository.SolanaAuctionHouseOrderRecordsRepository
import com.rarible.protocol.solana.common.repository.SolanaBalanceRecordsRepository
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
import com.rarible.protocol.solana.test.BalanceRecordDataFactory
import com.rarible.protocol.solana.test.OrderRecordDataFactory
import com.rarible.protocol.solana.test.createRandomBalance
import com.rarible.protocol.solana.test.randomSolanaLog
import com.rarible.protocol.solana.test.withUpdatedLog
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ActivityApiServiceIt : AbstractIntegrationTest() {

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Autowired
    private lateinit var balanceRecordsRepository: SolanaBalanceRecordsRepository

    @Autowired
    private lateinit var orderRecordsRepository: SolanaAuctionHouseOrderRecordsRepository

    @Autowired
    private lateinit var service: ActivityApiService

    @Test
    fun `all activities filter by types`() = runBlocking<Unit> {
        listOf(
            BalanceRecordDataFactory.randomMintToRecord(),
            BalanceRecordDataFactory.randomMintToRecord(),
            BalanceRecordDataFactory.randomMintToRecord(),
            BalanceRecordDataFactory.randomBurnRecord(),
            BalanceRecordDataFactory.randomBurnRecord(),
            BalanceRecordDataFactory.randomBurnRecord(),
            BalanceRecordDataFactory.randomIncomeRecord(),
            BalanceRecordDataFactory.randomIncomeRecord(),
            BalanceRecordDataFactory.randomIncomeRecord(),
            BalanceRecordDataFactory.randomOutcomeRecord(),
            BalanceRecordDataFactory.randomOutcomeRecord(),
            BalanceRecordDataFactory.randomOutcomeRecord(),
        ).forEach {
            balanceRecordsRepository.save(it)
            // Save balance for this account to have 'account -> owner' mapping in the converter.
            balanceRepository.save(createRandomBalance(account = it.account))
        }

        listOf(
            OrderRecordDataFactory.randomBuyRecord(),
            OrderRecordDataFactory.randomBuyRecord(),
            OrderRecordDataFactory.randomBuyRecord(),
            OrderRecordDataFactory.randomCancel(),
            OrderRecordDataFactory.randomCancel(),
            OrderRecordDataFactory.randomCancel(),
            OrderRecordDataFactory.randomSellRecord(),
            OrderRecordDataFactory.randomSellRecord(),
            OrderRecordDataFactory.randomSellRecord(),
            OrderRecordDataFactory.randomExecuteSaleRecord(),
            OrderRecordDataFactory.randomExecuteSaleRecord(),
            OrderRecordDataFactory.randomExecuteSaleRecord(),
        ).forEach { orderRecordsRepository.save(it) }

        val allTypes = listOf(
            ActivityFilterAllTypeDto.MINT,
            ActivityFilterAllTypeDto.BURN,
            ActivityFilterAllTypeDto.TRANSFER,
            ActivityFilterAllTypeDto.LIST,
            ActivityFilterAllTypeDto.CANCEL_LIST,
            ActivityFilterAllTypeDto.SELL,
        )

        allTypes.forEach { type ->
            val filter = ActivityFilterAllDto(listOf(type))
            val result = service.getAllActivities(filter, null, 50, true)
            assertThat(result).withFailMessage { type.name }.hasSize(3)
        }

        ActivityFilterAllDto(listOf(ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.BURN)).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertThat(result).hasSize(6)
        }

        ActivityFilterAllDto(listOf(ActivityFilterAllTypeDto.MINT, ActivityFilterAllTypeDto.LIST)).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertThat(result).hasSize(6)
        }

        ActivityFilterAllDto(emptyList()).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertThat(result).isEmpty()
        }

        ActivityFilterAllDto(allTypes).let { filter ->
            val result = service.getAllActivities(filter, null, 50, true)
            assertThat(result).hasSize(18)
        }
    }

    @Test
    fun `byItem activities filter by types`() = runBlocking<Unit> {
        val mint = randomString()

        listOf(
            BalanceRecordDataFactory.randomMintToRecord(mint = mint),
            BalanceRecordDataFactory.randomMintToRecord(),
            BalanceRecordDataFactory.randomMintToRecord(),
            BalanceRecordDataFactory.randomBurnRecord(mint = mint),
            BalanceRecordDataFactory.randomBurnRecord(),
            BalanceRecordDataFactory.randomBurnRecord(),
            BalanceRecordDataFactory.randomIncomeRecord(mint = mint),
            BalanceRecordDataFactory.randomIncomeRecord(),
            BalanceRecordDataFactory.randomIncomeRecord(),
            BalanceRecordDataFactory.randomOutcomeRecord(mint = mint),
            BalanceRecordDataFactory.randomOutcomeRecord(),
            BalanceRecordDataFactory.randomOutcomeRecord(),
        ).forEach {
            balanceRecordsRepository.save(it)
            // Save balance for this account to have 'account -> owner' mapping in the converter.
            balanceRepository.save(createRandomBalance(account = it.account))
        }

        listOf(
            OrderRecordDataFactory.randomBuyRecord(mint = mint),
            OrderRecordDataFactory.randomBuyRecord(),
            OrderRecordDataFactory.randomBuyRecord(),
            OrderRecordDataFactory.randomCancel(mint = mint),
            OrderRecordDataFactory.randomCancel(),
            OrderRecordDataFactory.randomCancel(),
            OrderRecordDataFactory.randomSellRecord(mint = mint),
            OrderRecordDataFactory.randomSellRecord(),
            OrderRecordDataFactory.randomSellRecord(),
            OrderRecordDataFactory.randomExecuteSaleRecord(mint = mint),
            OrderRecordDataFactory.randomExecuteSaleRecord(),
            OrderRecordDataFactory.randomExecuteSaleRecord(),
        ).forEach { orderRecordsRepository.save(it) }

        val allTypes = listOf(
            ActivityFilterByItemTypeDto.MINT,
            ActivityFilterByItemTypeDto.BURN,
            ActivityFilterByItemTypeDto.TRANSFER,
            ActivityFilterByItemTypeDto.LIST,
            ActivityFilterByItemTypeDto.CANCEL_LIST,
            ActivityFilterByItemTypeDto.SELL,
        )

        allTypes.forEach { type ->
            val filter = ActivityFilterByItemDto(mint, listOf(type))
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).withFailMessage { type.name }.hasSize(1)
        }

        ActivityFilterByItemDto(
            mint,
            listOf(ActivityFilterByItemTypeDto.MINT, ActivityFilterByItemTypeDto.BURN)
        ).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(2)
        }

        ActivityFilterByItemDto(
            mint,
            listOf(ActivityFilterByItemTypeDto.MINT, ActivityFilterByItemTypeDto.LIST)
        ).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(2)
        }

        ActivityFilterByItemDto(mint, emptyList()).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).isEmpty()
        }

        ActivityFilterByItemDto(mint, allTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(6)
        }
    }

    @Test
    fun `order activities - list`() = runBlocking<Unit> {
        val orderTypes = listOf(
            ActivityFilterByItemTypeDto.LIST,
            ActivityFilterByItemTypeDto.CANCEL_LIST,
            ActivityFilterByItemTypeDto.BID,
            ActivityFilterByItemTypeDto.CANCEL_BID,
            ActivityFilterByItemTypeDto.SELL,
        )

        val list = OrderRecordDataFactory.randomSellRecord()
        val cancelList = OrderRecordDataFactory.randomCancel(direction = OrderDirection.SELL)
        val simpleSell = OrderRecordDataFactory.randomExecuteSaleRecord(direction = OrderDirection.SELL)

        val fullSellSolanaLog = randomSolanaLog()
        val fullSellMint = randomString()
        val fullSell = listOf(
            OrderRecordDataFactory.randomBuyRecord(mint = fullSellMint)
                .withUpdatedLog(fullSellSolanaLog.copy(instructionIndex = 4)),
            OrderRecordDataFactory.randomExecuteSaleRecord(mint = fullSellMint, direction = OrderDirection.BUY)
                .withUpdatedLog(fullSellSolanaLog.copy(instructionIndex = 5)),
            OrderRecordDataFactory.randomExecuteSaleRecord(mint = fullSellMint, direction = OrderDirection.SELL)
                .withUpdatedLog(fullSellSolanaLog.copy(instructionIndex = 5)),
        )

        val data = listOf(list, cancelList, simpleSell) + fullSell
        data.map { orderRecordsRepository.save(it) }

        ActivityFilterByItemDto(list.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(1)
            val activity = result.single()
            assertTrue(activity is OrderListActivityDto)
            assertEquals(list.id, activity.id)
            assertEquals(list.timestamp, activity.date)
        }

        ActivityFilterByItemDto(cancelList.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(1)
            val activity = result.single()
            assertTrue(activity is OrderCancelListActivityDto)
            assertEquals(cancelList.id, activity.id)
            assertEquals(cancelList.timestamp, activity.date)
        }

        ActivityFilterByItemDto(simpleSell.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(1)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.SELL, (activity as OrderMatchActivityDto).type)
            assertEquals(simpleSell.id, activity.id)
            assertEquals(simpleSell.timestamp, activity.date)
        }
        ActivityFilterByItemDto(fullSellMint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(1)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.SELL, (activity as OrderMatchActivityDto).type)
            assertEquals(fullSell.last().id, activity.id)
            assertEquals(fullSell.last().timestamp, activity.date)
        }
    }

    @Test
    fun `order activities - bid`() = runBlocking<Unit> {
        val orderTypes = listOf(
            ActivityFilterByItemTypeDto.LIST,
            ActivityFilterByItemTypeDto.CANCEL_LIST,
            ActivityFilterByItemTypeDto.BID,
            ActivityFilterByItemTypeDto.CANCEL_BID,
            ActivityFilterByItemTypeDto.SELL,
        )

        val bid = OrderRecordDataFactory.randomBuyRecord()
        val cancelBid = OrderRecordDataFactory.randomCancel(direction = OrderDirection.BUY)
        val simpleAcceptBid = OrderRecordDataFactory.randomExecuteSaleRecord(direction = OrderDirection.BUY)

        val fullBidMint = randomString()
        val fullBidSolanaLog = randomSolanaLog()
        val fullBid = listOf(
            OrderRecordDataFactory.randomSellRecord(mint = fullBidMint)
                .withUpdatedLog(fullBidSolanaLog.copy(instructionIndex = 4)),
            OrderRecordDataFactory.randomExecuteSaleRecord(mint = fullBidMint, direction = OrderDirection.SELL)
                .withUpdatedLog(fullBidSolanaLog.copy(instructionIndex = 5)),
            OrderRecordDataFactory.randomExecuteSaleRecord(mint = fullBidMint, direction = OrderDirection.BUY)
                .withUpdatedLog(fullBidSolanaLog.copy(instructionIndex = 5)),
        )

        val data = listOf(bid, cancelBid, simpleAcceptBid) + fullBid
        data.map { orderRecordsRepository.save(it) }

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
            assertThat(result).hasSize(1)
            val activity = result.single()
            assertTrue(activity is OrderCancelBidActivityDto)
            assertEquals(cancelBid.id, activity.id)
            assertEquals(cancelBid.timestamp, activity.date)
        }

        ActivityFilterByItemDto(simpleAcceptBid.mint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(1)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.ACCEPT_BID, (activity as OrderMatchActivityDto).type)
            assertEquals(simpleAcceptBid.id, activity.id)
            assertEquals(simpleAcceptBid.timestamp, activity.date)
        }

        ActivityFilterByItemDto(fullBidMint, orderTypes).let { filter ->
            val result = service.getActivitiesByItem(filter, null, 50, true)
            assertThat(result).hasSize(1)
            val activity = result.single()
            assertTrue(activity is OrderMatchActivityDto)
            assertEquals(OrderMatchActivityDto.Type.ACCEPT_BID, (activity as OrderMatchActivityDto).type)
            assertEquals(fullBid.last().id, activity.id)
            assertEquals(fullBid.last().timestamp, activity.date)
        }
    }
}
