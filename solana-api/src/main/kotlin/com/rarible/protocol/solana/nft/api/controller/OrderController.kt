package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.OrderControllerApi
import com.rarible.protocol.solana.common.continuation.ContinuationFactory
import com.rarible.protocol.solana.common.continuation.DateIdContinuation
import com.rarible.protocol.solana.common.continuation.OrderContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.common.converter.OrderConverter
import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.order.filter.OrderFilter
import com.rarible.protocol.solana.common.model.order.filter.OrderFilterSort
import com.rarible.protocol.solana.dto.OrderCurrenciesDto
import com.rarible.protocol.solana.dto.OrderDto
import com.rarible.protocol.solana.dto.OrderIdsDto
import com.rarible.protocol.solana.dto.OrderSortDto
import com.rarible.protocol.solana.dto.OrderStatusDto
import com.rarible.protocol.solana.dto.OrdersDto
import com.rarible.protocol.solana.nft.api.service.OrderApiService
import com.rarible.protocol.union.dto.continuation.page.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderApiService: OrderApiService,
    private val orderConverter: OrderConverter
) : OrderControllerApi {

    override suspend fun getOrderById(id: String): ResponseEntity<OrderDto> {
        val order = orderApiService.getOrderById(id)
        return ResponseEntity.ok(orderConverter.convert(order))
    }

    override suspend fun getOrdersByIds(orderIdsDto: OrderIdsDto): ResponseEntity<OrdersDto> {
        val orders = orderApiService.findByIds(orderIdsDto.ids)
            .map { orderConverter.convert(it) }
        return ResponseEntity.ok(OrdersDto(null, orders))
    }

    override suspend fun getOrdersAll(
        continuation: String?,
        size: Int?,
        sort: OrderSortDto?,
        status: List<OrderStatusDto>?
    ): ResponseEntity<OrdersDto> {
        val safeSize = PageSize.ORDER.limit(size)
        val safeSort = sort?.fromDto() ?: OrderFilterSort.LAST_UPDATE_DESC

        val orderFilter = OrderFilter.All(
            statuses = status?.fromDto(),
            sort = safeSort,
            continuation = DateIdContinuation.parse(continuation)
        )

        val orders = orderApiService.getOrders(orderFilter, safeSize)

        val continuationFactory = when (safeSort) {
            OrderFilterSort.LAST_UPDATE_ASC -> OrderContinuation.ByLastUpdatedAndIdAsc
            OrderFilterSort.LAST_UPDATE_DESC -> OrderContinuation.ByLastUpdatedAndIdDesc
        }

        return ResponseEntity.ok(toSlice(orders, continuationFactory, safeSize))
    }

    override suspend fun getSellCurrencies(itemId: String): ResponseEntity<OrderCurrenciesDto> {
        // TODO[orders]: implement
        return ResponseEntity.ok(OrderCurrenciesDto(emptyList()))
    }

    override suspend fun getSellOrders(origin: String?, continuation: String?, size: Int?): ResponseEntity<OrdersDto> {
        // TODO[orders] implement
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getSellOrdersByItem(
        itemId: String,
        currencyId: String,
        maker: String?,
        origin: String?,
        status: List<OrderStatusDto>?,
        continuation: String?,
        size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO[orders] implement
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getSellOrdersByMaker(
        maker: String,
        origin: String?,
        status: List<OrderStatusDto>?,
        continuation: String?,
        size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO[orders] implement
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getBidCurrencies(itemId: String): ResponseEntity<OrderCurrenciesDto> {
        // TODO[orders] implement
        return ResponseEntity.ok(OrderCurrenciesDto(emptyList()))
    }

    override suspend fun getOrderBidsByItem(
        itemId: String,
        currencyId: String,
        status: List<OrderStatusDto>,
        maker: List<String>?,
        origin: String?,
        startDate: Long?,
        endDate: Long?,
        continuation: String?,
        size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO[orders] implement
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getOrderBidsByMaker(
        maker: String,
        origin: String?,
        status: List<OrderStatusDto>?,
        start: Long?,
        end: Long?,
        continuation: String?,
        size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO[orders] implement
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    private suspend fun toSlice(
        orders: List<Order>,
        continuationFactory: ContinuationFactory<OrderDto, *>,
        size: Int
    ): OrdersDto {
        val dto = orders.map { orderConverter.convert(it) }

        val slice = Paging(continuationFactory, dto).getSlice(size)
        return OrdersDto(slice.continuation, slice.entities)
    }

    private fun List<OrderStatusDto>.fromDto(): List<OrderStatus> = map { it.fromDto() }
    private fun OrderStatusDto.fromDto(): OrderStatus = when (this) {
        OrderStatusDto.ACTIVE -> OrderStatus.ACTIVE
        OrderStatusDto.FILLED -> OrderStatus.FILLED
        OrderStatusDto.CANCELLED -> OrderStatus.CANCELLED
    }

    private fun OrderSortDto.fromDto(): OrderFilterSort = when (this) {
        OrderSortDto.LAST_UPDATE_ASC -> OrderFilterSort.LAST_UPDATE_ASC
        OrderSortDto.LAST_UPDATE_DESC -> OrderFilterSort.LAST_UPDATE_DESC
    }
}
