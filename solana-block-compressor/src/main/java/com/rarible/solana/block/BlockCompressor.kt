package com.rarible.solana.block

import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.SolanaBlockDto
import com.rarible.blockchain.scanner.solana.client.dto.SolanaTransactionDto

/**
 * Compresses full Solana JSON RPC block by dropping all non-interesting programs and instructions.
 * Preserves 100% JSON compatibility with the official Solana JSON RPC.
 *
 * Original blocks size in JSON format is around 3-5Mb. After compression, they become 10-30Kb.
 */
class BlockCompressor(
    private val solanaProgramIdsToKeep: Set<String> = DEFAULT_COMPRESSOR_PROGRAM_IDS
) {
    private fun SolanaTransactionDto.Instruction.isOk(programId: String): SolanaTransactionDto.Instruction? {
        return takeIf { solanaProgramIdsToKeep.isEmpty() || programId in solanaProgramIdsToKeep }
    }

    fun compress(response: ApiResponse<SolanaBlockDto>): ApiResponse<SolanaBlockDto> {
        val block = response.result!!
        val newTransactions = block.transactions.map { transactionDto ->
            val transaction = transactionDto.transaction ?: return@map transactionDto
            val accountKeys = transaction.message.accountKeys +
                    (transactionDto.meta?.loadedAddresses?.let { it.writable + it.readonly } ?: emptyList())
            val newInstructions = transaction.message.instructions.map {
                it?.isOk(accountKeys[it.programIdIndex])
            }
            val newInnerInstructions = transactionDto.meta?.innerInstructions?.map { innerInstruction ->
                SolanaTransactionDto.InnerInstruction(
                    index = innerInstruction.index,
                    instructions = innerInstruction.instructions.map { it?.isOk(accountKeys[it.programIdIndex]) }
                )
            }

            transactionDto.copy(
                meta = SolanaTransactionDto.Meta(
                    err = transactionDto.meta?.err?.let { true },
                    loadedAddresses = transactionDto.meta?.loadedAddresses,
                    innerInstructions = newInnerInstructions ?: emptyList()
                ),
                transaction = if (newInstructions.all { it == null }) {
                    if (newInnerInstructions.isNullOrEmpty()) {
                        null
                    } else {
                        transaction.copy(
                            message = transaction.message.copy(
                                instructions = emptyList(),
                                accountKeys = transaction.message.accountKeys,
                                recentBlockhash = transaction.message.recentBlockhash
                            )
                        )
                    }
                } else
                    transaction.copy(
                        message = transaction.message.copy(
                            instructions = newInstructions
                        )
                    )
            )
        }

        return ApiResponse(
            id = response.id,
            result = block.copy(transactions = newTransactions),
            error = response.error,
            jsonrpc = response.jsonrpc
        )
    }

    companion object {
        val DEFAULT_COMPRESSOR_PROGRAM_IDS = setOf(
            "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA",
            "metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s",
            "hausS13jsjafwWwGqZTUQRmWyvyxn9EQpqMwV1PBBmk",
        )
    }
}