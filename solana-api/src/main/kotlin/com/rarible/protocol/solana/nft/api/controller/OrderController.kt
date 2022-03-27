package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.OrderControllerApi
import com.rarible.protocol.solana.common.converter.OrderConverter
import com.rarible.protocol.solana.common.service.OrderService
import com.rarible.protocol.solana.dto.OrderCurrenciesDto
import com.rarible.protocol.solana.dto.OrderDto
import com.rarible.protocol.solana.dto.OrderIdsDto
import com.rarible.protocol.solana.dto.OrderSortDto
import com.rarible.protocol.solana.dto.OrderStatusDto
import com.rarible.protocol.solana.dto.OrdersDto
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class OrderController(
    private val orderService: OrderService
) : OrderControllerApi {

    override suspend fun getBidCurrencies(itemId: String): ResponseEntity<OrderCurrenciesDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrderCurrenciesDto(emptyList()))
    }

    override suspend fun getOrderBidsByItem(
        itemId: String, currencyId: String, status: List<OrderStatusDto>, maker: List<String>?, origin: String?,
        startDate: Long?, endDate: Long?, continuation: String?, size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getOrderBidsByMaker(
        maker: String, origin: String?, status: List<OrderStatusDto>?, start: Long?, end: Long?, continuation: String?,
        size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getOrderById(id: String): ResponseEntity<OrderDto> {
        val order = orderService.findById(id) ?: throw EntityNotFoundApiException("order", id)
        val dto = OrderConverter.convert(order)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getOrdersAll(
        continuation: String?, size: Int?, sort: OrderSortDto?, status: List<OrderStatusDto>?
    ): ResponseEntity<OrdersDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getOrdersByIds(orderIdsDto: OrderIdsDto): ResponseEntity<OrdersDto> {
        val orders = orderService.findByIds(orderIdsDto.ids)
            .map { OrderConverter.convert(it) }
        return ResponseEntity.ok(OrdersDto(null, orders))
    }

    override suspend fun getSellCurrencies(itemId: String): ResponseEntity<OrderCurrenciesDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrderCurrenciesDto(emptyList()))
    }

    override suspend fun getSellOrders(origin: String?, continuation: String?, size: Int?): ResponseEntity<OrdersDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getSellOrdersByItem(
        itemId: String, currencyId: String, maker: String?, origin: String?, status: List<OrderStatusDto>?,
        continuation: String?, size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

    override suspend fun getSellOrdersByMaker(
        maker: String, origin: String?, status: List<OrderStatusDto>?, continuation: String?, size: Int?
    ): ResponseEntity<OrdersDto> {
        // TODO IMPLEMENT
        return ResponseEntity.ok(OrdersDto(null, emptyList()))
    }

}