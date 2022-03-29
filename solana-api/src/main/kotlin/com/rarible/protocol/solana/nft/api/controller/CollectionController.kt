package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.CollectionControllerApi
import com.rarible.protocol.solana.common.continuation.CollectionContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.common.model.SolanaCollection
import com.rarible.protocol.solana.common.service.CollectionConversionService
import com.rarible.protocol.solana.common.service.CollectionService
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.dto.CollectionsDto
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import com.rarible.protocol.union.dto.continuation.page.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionController(
    private val collectionService: CollectionService,
    private val collectionConversionService: CollectionConversionService
) : CollectionControllerApi {

    override suspend fun getAllCollections(continuation: String?, size: Int?): ResponseEntity<CollectionsDto> {
        val safeSize = PageSize.COLLECTION.limit(size)

        val collections = collectionService.findAll(continuation, safeSize)

        val dto = toSlice(collections, safeSize)
        return ResponseEntity.ok(dto)
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

    private suspend fun toSlice(collections: List<SolanaCollection>, size: Int): CollectionsDto {
        val continuationFactory = CollectionContinuation.ById
        val result = collections.map { collectionConversionService.toDto(it) }

        val slice = Paging(continuationFactory, result).getSlice(size)

        return CollectionsDto(slice.entities, slice.continuation)
    }
}