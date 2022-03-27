package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.CollectionControllerApi
import com.rarible.protocol.solana.common.service.CollectionConversionService
import com.rarible.protocol.solana.common.service.CollectionService
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.dto.CollectionsDto
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionController(
    private val collectionService: CollectionService,
    private val collectionConversionService: CollectionConversionService
) : CollectionControllerApi {

    private val defaultSize = 50

    override suspend fun getAllCollections(continuation: String?, size: Int?): ResponseEntity<CollectionsDto> {
        val collections = collectionService.findAll(continuation, size ?: defaultSize)
        val dto = collections.map { collectionConversionService.toDto(it) }.sortedBy { it.address }

        return ResponseEntity.ok(
            CollectionsDto(
                collections = dto,
                continuation = dto.lastOrNull()?.address
            )
        )
    }

    override suspend fun getCollectionById(collection: String): ResponseEntity<CollectionDto> {
        val result = collectionService.findById(collection)
            ?: throw EntityNotFoundApiException("collection", collection)

        return ResponseEntity.ok(collectionConversionService.toDto(result))
    }

    override suspend fun getCollectionsByOwner(
        owner: String, continuation: String?, size: Int?
    ): ResponseEntity<CollectionsDto> {
        // TODO implement
        return ResponseEntity.ok(CollectionsDto())
    }

    override suspend fun refreshCollectionMeta(collection: String): ResponseEntity<Unit> {
        // TODO implement
        return ResponseEntity.ok().build()
    }
}