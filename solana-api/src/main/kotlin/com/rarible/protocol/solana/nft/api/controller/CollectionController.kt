package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.CollectionControllerApi
import com.rarible.protocol.solana.common.continuation.CollectionContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.common.pubkey.PublicKey
import com.rarible.protocol.solana.common.service.CollectionConverter
import com.rarible.protocol.solana.common.service.CollectionService
import com.rarible.protocol.solana.common.service.CollectionUpdateService
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.dto.CollectionsByIdRequestDto
import com.rarible.protocol.solana.dto.CollectionsDto
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import com.rarible.protocol.union.dto.continuation.page.PageSize
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionController(
    private val collectionService: CollectionService,
    private val collectionUpdateService: CollectionUpdateService,
    private val collectionConverter: CollectionConverter
) : CollectionControllerApi {

    override suspend fun getAllCollections(
        continuation: String?,
        size: Int?
    ): ResponseEntity<CollectionsDto> {
        val safeSize = PageSize.COLLECTION.limit(size)

        val collectionsDto = collectionService.findAll(continuation)
            .mapNotNull { collectionConverter.toDto(it) }
            .take(safeSize).toList()
        val dto = toSlice(collectionsDto, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getCollectionById(collection: String): ResponseEntity<CollectionDto> {
        val collectionDto = collectionService.findById(collection)
            ?.let { collectionConverter.toDto(it) }
            ?: throw EntityNotFoundApiException("Collection", collection)
        return ResponseEntity.ok(collectionDto)
    }

    override suspend fun searchCollectionsByIds(collectionsByIdRequestDto: CollectionsByIdRequestDto): ResponseEntity<CollectionsDto> {
        val collections = collectionService.findByIds(collectionsByIdRequestDto.ids)
            .mapNotNull { collectionConverter.toDto(it) }
            .toList()
        return ResponseEntity.ok(CollectionsDto(
            collections = collections,
            continuation = null,
        ))
    }

    override suspend fun getCollectionsByOwner(
        owner: String,
        continuation: String?,
        size: Int?
    ): ResponseEntity<CollectionsDto> {
        // TODO implement
        return ResponseEntity.ok(CollectionsDto())
    }

    override suspend fun refreshCollectionMeta(collection: String): ResponseEntity<Unit> {
        if (!PublicKey.isPubKey(collection)) {
            return ResponseEntity.ok().build()
        }
        collectionUpdateService.markNftAsCollection(collection)
        return ResponseEntity.ok().build()
    }


    private suspend fun toSlice(collections: List<CollectionDto>, size: Int): CollectionsDto {
        val continuationFactory = CollectionContinuation.ById
        val slice = Paging(continuationFactory, collections).getSlice(size)
        return CollectionsDto(slice.entities, slice.continuation)
    }
}