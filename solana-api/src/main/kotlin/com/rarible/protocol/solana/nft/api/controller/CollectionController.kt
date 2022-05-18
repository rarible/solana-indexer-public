package com.rarible.protocol.solana.nft.api.controller

import com.rarible.protocol.solana.api.controller.CollectionControllerApi
import com.rarible.protocol.solana.common.continuation.CollectionContinuation
import com.rarible.protocol.solana.common.continuation.Paging
import com.rarible.protocol.solana.common.model.SolanaCollection
import com.rarible.protocol.solana.common.model.SolanaCollectionV2
import com.rarible.protocol.solana.common.service.CollectionConverter
import com.rarible.protocol.solana.common.service.CollectionService
import com.rarible.protocol.solana.common.update.CollectionEventListener
import com.rarible.protocol.solana.dto.CollectionDto
import com.rarible.protocol.solana.dto.CollectionsDto
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import com.rarible.protocol.solana.nft.api.service.TokenApiService
import com.rarible.protocol.union.dto.continuation.page.PageSize
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionController(
    private val collectionService: CollectionService,
    private val tokenApiService: TokenApiService,
    private val collectionConverter: CollectionConverter,
    private val collectionEventListener: CollectionEventListener
) : CollectionControllerApi {

    override suspend fun getAllCollections(continuation: String?, size: Int?): ResponseEntity<CollectionsDto> {
        val safeSize = PageSize.COLLECTION.limit(size)

        val collections = collectionService.findAll(continuation, safeSize)

        val dto = toSlice(collections, safeSize)
        return ResponseEntity.ok(dto)
    }

    override suspend fun getCollectionById(collection: String): ResponseEntity<CollectionDto> {
        val solanaCollection = collectionService.findById(collection)
        if (solanaCollection != null) {
            return ResponseEntity.ok(collectionConverter.toDto(solanaCollection))
        }

        /**
         * Empty collections on Solana cannot be distinguished from regular NFTs.
         * As a fallback, we request the token by this address and convert it to the collection DTO.
         * This is only done for "getCollectionById" endpoint, when the requester knows a concrete collection ID.
         * For example, SDK creates an empty collection and wants to mint some items to it.
         */
        val tokenAsCollection = try {
            tokenApiService.getTokenWithMeta(collection)
        } catch (e: EntityNotFoundApiException) {
            null
        }?.takeIf { it.hasMeta } ?: throw EntityNotFoundApiException("Collection", collection)

        val collectionDto = collectionConverter.convertV2(
            collection = SolanaCollectionV2(tokenAsCollection.token.id),
            tokenMeta = tokenAsCollection.tokenMeta!!
        )

        val updatedCollectionV2 = collectionService.updateCollectionV2(tokenAsCollection.token.mint)
        if (updatedCollectionV2 != null) {
            // Also, we must notify the clients about the new empty collection.
            collectionEventListener.onCollectionChanged(collectionDto)
        }

        return ResponseEntity.ok(collectionDto)
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
        val result = collections.map { collectionConverter.toDto(it) }

        val slice = Paging(continuationFactory, result).getSlice(size)

        return CollectionsDto(slice.entities, slice.continuation)
    }
}