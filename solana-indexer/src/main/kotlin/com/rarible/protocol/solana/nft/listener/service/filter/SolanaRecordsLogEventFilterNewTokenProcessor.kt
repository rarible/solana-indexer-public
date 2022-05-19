package com.rarible.protocol.solana.nft.listener.service.filter

import com.rarible.blockchain.scanner.framework.data.LogEvent
import com.rarible.blockchain.scanner.framework.data.NewBlockEvent
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.model.SolanaDescriptor
import com.rarible.blockchain.scanner.solana.model.SolanaLogRecord
import com.rarible.protocol.solana.common.filter.token.SolanaTokenFilter
import com.rarible.protocol.solana.common.records.SolanaTokenRecord
import com.rarible.protocol.solana.nft.listener.util.hasCreateMetaplexMeta
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SolanaRecordsLogEventFilterNewTokenProcessor(
    private val tokenFilter: SolanaTokenFilter
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Processes InitializeMint records for the new tokens. Checks if those new tokens are NFTs
     * by presence of "Create Metaplex Metadata" instruction in the same block.
     * If metadata is not available, add the tokens to the blacklist.
     */
    suspend fun addNewTokensWithoutMetaToBlacklist(events: List<LogEvent<SolanaLogRecord, SolanaDescriptor>>) {
        val blacklistedTokens = hashSetOf<String>()
        for (event in events) {
            for (logRecord in event.logRecordsToInsert) {
                if (logRecord is SolanaTokenRecord.InitializeMintRecord) {
                    val solanaBlockchainBlock = (event.blockEvent as? NewBlockEvent)?.block as? SolanaBlockchainBlock
                    solanaBlockchainBlock ?: continue
                    val hasMetaplexMeta = solanaBlockchainBlock.hasCreateMetaplexMeta(
                        transactionHash = logRecord.log.transactionHash,
                        matcher = { it.mint == logRecord.mint }
                    )
                    if (!hasMetaplexMeta) {
                        logger.info("Token ${logRecord.mint} is created without associated Metaplex meta")
                        blacklistedTokens += logRecord.mint
                    }
                }
            }
        }
        if (blacklistedTokens.isNotEmpty()) {
            tokenFilter.addToBlacklist(blacklistedTokens,
                SolanaRecordsLogEventFilter.TOKEN_WITHOUT_META_BLACKLIST_REASON
            )
        }
    }


}