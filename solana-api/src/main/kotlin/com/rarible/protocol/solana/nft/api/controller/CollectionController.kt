package com.rarible.protocol.solana.nft.api.controller

import com.rarible.solana.protocol.api.controller.CollectionControllerApi
import com.rarible.solana.protocol.dto.CollectionDto
import com.rarible.solana.protocol.dto.CollectionsDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class CollectionController : CollectionControllerApi {

    override suspend fun getAllCollections(continuation: String?, size: Int?): ResponseEntity<CollectionsDto> {
        // TODO implement
        return ResponseEntity.ok(CollectionsDto())
    }

    override suspend fun getCollectionById(collection: String): ResponseEntity<CollectionDto> {
        // TODO implement in right way
        return ResponseEntity.ok(
            CollectionDto(
                address = "",
                name = ""
            )
        )
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
        // TODO implement
        return ResponseEntity.ok().build()
    }
}