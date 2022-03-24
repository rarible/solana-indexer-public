package com.rarible.protocol.solana.nft.listener.block.cache

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.Binary
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findById
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
    suspend fun save(id: Long, content: ByteArray) {
        mongo.save(BlockCache(id, Binary(gzip(content)))).awaitFirst()
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