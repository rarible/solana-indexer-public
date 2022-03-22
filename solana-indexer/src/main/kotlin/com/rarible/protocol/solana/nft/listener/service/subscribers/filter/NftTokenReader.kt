package com.rarible.protocol.solana.nft.listener.service.subscribers.filter

import org.slf4j.LoggerFactory
import java.util.stream.Collectors

class NftTokenReader(
    private val folder: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun readTokens(fileNames: List<String>): Set<String> {
        val result = HashSet<String>()
        for (file in fileNames) {
            val fullPath = "$folder/$file"

            val tokens = this::class.java.getResourceAsStream(fullPath)!!
                .bufferedReader()
                .use { it.lines().collect(Collectors.toList()) }
                .filterNot { it.isBlank() }

            logger.info("Found {} tokens in file '{}'", tokens.size, fullPath)
            result.addAll(tokens)
        }
        logger.info("Found {} tokens in all {} files", result.size, fileNames.size)
        return result
    }
}