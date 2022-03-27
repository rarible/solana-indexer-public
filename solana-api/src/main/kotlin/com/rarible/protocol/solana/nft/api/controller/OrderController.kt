package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.OrderControllerApi
import com.rarible.protocol.solana.common.converter.OrderConverter
import com.rarible.protocol.solana.common.model.OrderStatus
import com.rarible.protocol.solana.common.model.order.filter.OrderFilter
import com.rarible.protocol.solana.common.model.order.filter.OrderFilterSort
import com.rarible.protocol.solana.dto.OrderCurrenciesDto
import com.rarible.protocol.solana.dto.OrderDto
import com.rarible.protocol.solana.dto.OrderIdsDto
import com.rarible.protocol.solana.dto.OrderSortDto
import com.rarible.protocol.solana.dto.OrderStatusDto
import com.rarible.protocol.solana.dto.OrdersDto
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import com.rarible.protocol.solana.nft.api.service.OrderApiService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderApiService: OrderApiService
) : OrderControllerApi {
    override suspend fun getOrderById(id: String): ResponseEntity<OrderDto> {
        val order = orderApiService.getOrderById(id)
        return ResponseEntity.ok(OrderConverter.convert(order))
    }

    override suspend fun getOrdersByIds(orderIdsDto: OrderIdsDto): ResponseEntity<OrdersDto> {
        val orders = orderApiService.findByIds(orderIdsDto.ids)
            .map { OrderConverter.convert(it) }
        return ResponseEntity.ok(OrdersDto(null, orders))
    }

    override suspend fun getOrdersAll(
        continuation: String?,
        size: Int?,
        sort: OrderSortDto?,
        status: List<OrderStatusDto>?
    ): ResponseEntity<OrdersDto> {
        val orderFilter = OrderFilter.All(
            statuses = (status ?: listOf(OrderStatusDto.ACTIVE)).fromDto(),
            sort = sort?.fromDto() ?: OrderFilterSort.LAST_UPDATE_DESC
        )
        val requestSize = limitSize(size)
        val ordersWithContinuation = orderApiService.getOrders(orderFilter, continuation, requestSize)
        return ResponseEntity.ok(OrderConverter.convert(ordersWithContinuation))
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

    private fun limitSize(size: Int?) = if (size != null) minOf(size, 50) else 50
}
