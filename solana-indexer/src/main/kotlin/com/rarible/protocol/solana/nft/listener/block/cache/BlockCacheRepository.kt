package com.rarible.protocol.solana.nft.listener.block.cache

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.Binary
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.count
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.annotation.Resource

@Component
class BlockCacheRepository(
    @Resource(name = "reactiveMongoTemplateBlockCache")
    private val mongo: ReactiveMongoOperations
) {
    private val logger = LoggerFactory.getLogger(BlockCacheRepository::class.java)

    suspend fun isPresent(id: Long): Boolean {
        val cnt = mongo.count<BlockCache>(Query(Criteria.where("id").`is`(id))).awaitFirst()
        return cnt > 0
    }

    suspend fun save(id: Long, content: ByteArray) {
        val gzip = gzip(content)
        mongo.save(BlockCache(id, Binary(gzip))).awaitFirst()
        logger.info("Saved block #$id to the cache: original size {}, gzip size: {}", content.size, gzip.size)
    }

    suspend fun findAll(ids: List<Long>): Map<Long, ByteArray> {
        val criteria = Criteria.where(BlockCache::id.name).`in`(ids)
        val sort = Sort.by(Sort.Direction.ASC, "_id")

        return mongo.find(Query(criteria).with(sort), BlockCache::class.java)
            .asFlow()
            .toList()
            .associate {
                it.id to ungzip(it.data.data)
            }
    }

    suspend fun find(id: Long): ByteArray? {
        val found = mongo.findById<BlockCache>(id).awaitFirstOrNull()
        return if (found != null) {
            ungzip(found.data.data)
        } else {
            null
        }
    }
}

private fun gzip(content: ByteArray): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).buffered().use { it.write(content) }
    return bos.toByteArray()
}

private fun ungzip(content: ByteArray): ByteArray =
    GZIPInputStream(content.inputStream()).buffered().use { it.readBytes() }