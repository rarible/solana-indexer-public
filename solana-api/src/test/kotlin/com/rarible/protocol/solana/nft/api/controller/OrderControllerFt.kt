package com.rarible.protocol.solana.nft.api.controller

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.PriceIdContinuation
import com.rarible.protocol.solana.common.model.AuctionHouse
import com.rarible.protocol.solana.common.model.TokenFtAssetType
import com.rarible.protocol.solana.common.model.TokenNftAssetType
import com.rarible.protocol.solana.common.repository.OrderRepository
import com.rarible.protocol.solana.dto.OrderDto
import com.rarible.protocol.solana.dto.OrderIdsDto
import com.rarible.protocol.solana.dto.OrderStatusDto
import com.rarible.protocol.solana.dto.SolanaFtAssetTypeDto
import com.rarible.protocol.solana.dto.SyncSortDto
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createAuctionHouse
import com.rarible.protocol.solana.test.randomAsset
import com.rarible.protocol.solana.test.randomBuyOrder
import com.rarible.protocol.solana.test.randomMint
import com.rarible.protocol.solana.test.randomSellOrder
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.Instant

// Since most of the cases for queries checked in OrderRepositoryIt, there will be only basic tests
class OrderControllerFt : AbstractControllerTest() {

    @Autowired
    lateinit var orderRepository: OrderRepository


    @Test
    fun `order controller sync test`() = runBlocking<Unit>  {
        val orderQuantity = 20

        repeat(orderQuantity) {
            val order =
                orderRepository.save(randomSellOrder().copy(dbUpdatedAt = Instant.ofEpochMilli((0..Long.MAX_VALUE).random())))
            auctionHouseRepository.save(createAuctionHouse(order))
        }

        val result = orderControllerApi.getOrdersSync(null, orderQuantity, SyncSortDto.DB_UPDATE_ASC).awaitFirst()

        assertThat(result.orders).hasSize(orderQuantity)
        assertThat(result.orders).isSortedAccordingTo { o1, o2 ->
            compareValues(
                o1.dbUpdatedAt,
                o2.dbUpdatedAt
            )
        }
    }

    @Test
    fun `order controller sync pagination asc test `() = runBlocking<Unit> {
        val comparator = Comparator<OrderDto> { o1, o2 -> compareValues(o1.dbUpdatedAt, o2.dbUpdatedAt) }

        testGetOrderSyncPagination(
            orderQuantity = 95,
            chunkSize = 20,
            sort = SyncSortDto.DB_UPDATE_ASC,
            comparator
        )
    }

    @Test
    fun `order controller sync pagination des test `() = runBlocking<Unit> {
        val comparator = Comparator<OrderDto> {  o1, o2 -> compareValues(o2.dbUpdatedAt, o1.dbUpdatedAt)  }

        testGetOrderSyncPagination(
            orderQuantity = 60,
            chunkSize = 20,
            sort = SyncSortDto.DB_UPDATE_DESC,
            comparator
        )
    }

    private suspend fun testGetOrderSyncPagination(
        orderQuantity: Int,
        chunkSize: Int,
        sort: SyncSortDto,
        comparator: Comparator<OrderDto>
    ) {
        repeat(orderQuantity) {
            val order =
                orderRepository.save(randomSellOrder().copy(dbUpdatedAt = Instant.ofEpochMilli((0..Long.MAX_VALUE).random())))
            auctionHouseRepository.save(createAuctionHouse(order))
        }

        var continuation: String? = null
        val activities = mutableListOf<OrderDto>()
        var totalPages = 0

        do {
            val result =
                orderControllerApi.getOrdersSync(continuation, chunkSize, sort).awaitFirst()
            activities.addAll(result.orders)
            continuation = result.continuation
            totalPages += 1
        } while (continuation != null)

        assertThat(totalPages).isEqualTo(orderQuantity / chunkSize + 1)
        assertThat(activities).hasSize(orderQuantity)
        assertThat(activities).isSortedAccordingTo(comparator)
    }

    @Test
    fun `get by id`() = runBlocking<Unit> {
        val order = orderRepository.save(randomSellOrder())
        auctionHouseRepository.save(
            AuctionHouse(
                order.auctionHouse,
                100,
                true,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )
        val result = orderControllerApi.getOrderById(order.id).awaitFirst()

        assertThat(result.hash).isEqualTo(order.id)
        assertThat(result.auctionHouseFee).isEqualTo(order.auctionHouseSellerFeeBasisPoints)
        assertThat(result.auctionHouseRequiresSignOff).isEqualTo(order.auctionHouseRequiresSignOff)
    }

    @Test
    fun `get by ids`() = runBlocking<Unit> {
        val order1 = orderRepository.save(randomSellOrder())
        val order2 = orderRepository.save(randomSellOrder())

        auctionHouseRepository.save(
            AuctionHouse(
                order1.auctionHouse,
                100,
                true,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        auctionHouseRepository.save(
            AuctionHouse(
                order2.auctionHouse,
                300,
                false,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        val payload = OrderIdsDto(listOf(order1.id, randomString(), order2.id))

        val result = orderControllerApi.getOrdersByIds(payload)
            .awaitFirst()
            .orders.map { it.hash }.toSet()

        assertThat(result).isEqualTo(setOf(order1.id, order2.id))
    }

    @Test
    fun `get item sell currencies`() = runBlocking<Unit> {
        val currencyMint = randomMint()
        val nftMint = randomMint()

        val currencyAsset = randomAsset(TokenFtAssetType(currencyMint))
        val nftAsset = randomAsset(TokenNftAssetType(nftMint))

        orderRepository.save(randomSellOrder(make = nftAsset, take = currencyAsset))
        orderRepository.save(randomSellOrder(make = nftAsset, take = currencyAsset))

        val currencies = orderControllerApi.getSellCurrencies(nftMint)
            .awaitFirst().currencies.map { (it as SolanaFtAssetTypeDto).mint }

        assertThat(currencies).hasSize(1)
        assertThat(currencies[0]).isEqualTo(currencyMint)
    }

    @Test
    fun `get best sell order by item`() = runBlocking<Unit> {
        val currencyMint = randomMint()
        val nftMint = randomMint()

        val currencyAsset = randomAsset(TokenFtAssetType(currencyMint))
        val nftAsset = randomAsset(TokenNftAssetType(nftMint))

        val order1 = orderRepository.save(randomSellOrder(nftAsset, currencyAsset).copy(makePrice = BigDecimal.ONE))
        val order2 = orderRepository.save(randomSellOrder(nftAsset, currencyAsset).copy(makePrice = BigDecimal.TEN))

        auctionHouseRepository.save(
            AuctionHouse(
                order1.auctionHouse,
                100,
                true,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        auctionHouseRepository.save(
            AuctionHouse(
                order2.auctionHouse,
                300,
                false,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        val bestSell = orderControllerApi.getSellOrdersByItem(
            nftMint,
            currencyMint,
            null,
            null,
            listOf(OrderStatusDto.ACTIVE),
            null,
            1
        ).awaitFirst()

        val expectedContinuation = PriceIdContinuation(order1.makePrice, order1.id).toString()

        assertThat(bestSell.continuation).isEqualTo(expectedContinuation)
        assertThat(bestSell.orders).hasSize(1)
        assertThat(bestSell.orders[0].hash).isEqualTo(order1.id)
        assertThat(bestSell.orders[0].auctionHouseFee).isEqualTo(order1.auctionHouseSellerFeeBasisPoints)
        assertThat(bestSell.orders[0].auctionHouseRequiresSignOff).isEqualTo(order1.auctionHouseRequiresSignOff)
    }

    @Test
    fun `get sell orders`() = runBlocking<Unit> {
        val order1 = orderRepository.save(randomSellOrder())
        val order2 = orderRepository.save(randomSellOrder())

        auctionHouseRepository.save(
            AuctionHouse(
                order1.auctionHouse,
                100,
                true,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        auctionHouseRepository.save(
            AuctionHouse(
                order2.auctionHouse,
                300,
                false,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        val sorted = listOf(order1, order2).sortedByDescending { it.updatedAt }
        val from = sorted[0]

        val result = orderControllerApi.getSellOrders(
            null,
            DateIdContinuation(from.updatedAt, from.id).toString(),
            2
        ).awaitFirst()

        assertThat(result.continuation).isNull()
        assertThat(result.orders).hasSize(1)
        assertThat(result.orders[0].hash).isEqualTo(sorted[1].id)
        assertThat(result.orders[0].auctionHouseFee).isEqualTo(sorted[1].auctionHouseSellerFeeBasisPoints)
        assertThat(result.orders[0].auctionHouseRequiresSignOff).isEqualTo(sorted[1].auctionHouseRequiresSignOff)
    }

    @Test
    fun `get sell orders by maker`() = runBlocking<Unit> {
        val order1 = orderRepository.save(randomSellOrder())
        val order2 = orderRepository.save(randomSellOrder())

        auctionHouseRepository.save(
            AuctionHouse(
                order1.auctionHouse,
                100,
                true,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        auctionHouseRepository.save(
            AuctionHouse(
                order2.auctionHouse,
                300,
                false,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        val result = orderControllerApi.getSellOrdersByMaker(
            order2.maker,
            null,
            null,
            null,
            2
        ).awaitFirst()

        assertThat(result.continuation).isNull()
        assertThat(result.orders).hasSize(1)
        assertThat(result.orders[0].hash).isEqualTo(order2.id)
        assertThat(result.orders[0].auctionHouseFee).isEqualTo(order2.auctionHouseSellerFeeBasisPoints)
        assertThat(result.orders[0].auctionHouseRequiresSignOff).isEqualTo(order2.auctionHouseRequiresSignOff)
    }

    @Test
    fun `get item buy currencies`() = runBlocking<Unit> {
        val currencyMint = randomMint()
        val nftMint = randomMint()

        val currencyAsset = randomAsset(TokenFtAssetType(currencyMint))
        val nftAsset = randomAsset(TokenNftAssetType(nftMint))

        orderRepository.save(randomBuyOrder(take = nftAsset, make = currencyAsset))
        orderRepository.save(randomBuyOrder(take = nftAsset, make = currencyAsset))

        val currencies = orderControllerApi.getBidCurrencies(nftMint)
            .awaitFirst().currencies.map { (it as SolanaFtAssetTypeDto).mint }

        assertThat(currencies).hasSize(1)
        assertThat(currencies[0]).isEqualTo(currencyMint)
    }

    @Test
    fun `get best buy order by item`() = runBlocking<Unit> {
        val currencyMint = randomMint()
        val nftMint = randomMint()

        val currencyAsset = randomAsset(TokenFtAssetType(currencyMint))
        val nftAsset = randomAsset(TokenNftAssetType(nftMint))

        val order1 = orderRepository.save(randomBuyOrder(currencyAsset, nftAsset).copy(takePrice = BigDecimal.ONE))
        val order2 = orderRepository.save(randomBuyOrder(currencyAsset, nftAsset).copy(takePrice = BigDecimal.TEN))

        auctionHouseRepository.save(
            AuctionHouse(
                order1.auctionHouse,
                100,
                true,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        auctionHouseRepository.save(
            AuctionHouse(
                order2.auctionHouse,
                300,
                false,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        val bestSell = orderControllerApi.getOrderBidsByItem(
            nftMint,
            currencyMint,
            listOf(OrderStatusDto.ACTIVE),
            null,
            null,
            null,
            null,
            null,
            1
        ).awaitFirst()

        val expectedContinuation = PriceIdContinuation(order2.takePrice, order2.id).toString()

        assertThat(bestSell.continuation).isEqualTo(expectedContinuation)
        assertThat(bestSell.continuation).isEqualTo(expectedContinuation)
        assertThat(bestSell.orders).hasSize(1)
        assertThat(bestSell.orders[0].hash).isEqualTo(order2.id)
        assertThat(bestSell.orders[0].auctionHouseFee).isEqualTo(order2.auctionHouseSellerFeeBasisPoints)
        assertThat(bestSell.orders[0].auctionHouseRequiresSignOff).isEqualTo(order2.auctionHouseRequiresSignOff)
    }

    @Test
    fun `get buy orders by maker`() = runBlocking<Unit> {
        val order1 = orderRepository.save(randomBuyOrder())
        val order2 = orderRepository.save(randomBuyOrder())

        auctionHouseRepository.save(
            AuctionHouse(
                order1.auctionHouse,
                100,
                true,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        auctionHouseRepository.save(
            AuctionHouse(
                order2.auctionHouse,
                300,
                false,
                emptyList(),
                emptyList(),
                Instant.EPOCH,
                Instant.EPOCH
            )
        )

        val result = orderControllerApi.getOrderBidsByMaker(
            order2.maker,
            null,
            null,
            null,
            null,
            null,
            1
        ).awaitFirst()

        assertThat(result.continuation).isEqualTo(DateIdContinuation(order2.updatedAt, order2.id).toString())
        assertThat(result.orders).hasSize(1)
        assertThat(result.orders[0].hash).isEqualTo(order2.id)
        assertThat(result.orders[0].auctionHouseFee).isEqualTo(order2.auctionHouseSellerFeeBasisPoints)
        assertThat(result.orders[0].auctionHouseRequiresSignOff).isEqualTo(order2.auctionHouseRequiresSignOff)
    }
}