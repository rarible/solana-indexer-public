package com.rarible.solana.proxy.converter

import com.rarible.blockchain.scanner.solana.client.dto.ApiResponse
import com.rarible.blockchain.scanner.solana.client.dto.SolanaBlockDto
import com.rarible.blockchain.scanner.solana.client.dto.SolanaTransactionDto

object SolanaProgramId {
    const val SPL_TOKEN_PROGRAM = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TOKEN_METADATA_PROGRAM = "metaqbxxUerdq28cj1RbAWkYQm3ybzjb6a8bt518x1s"
    const val AUCTION_HOUSE_PROGRAM = "hausS13jsjafwWwGqZTUQRmWyvyxn9EQpqMwV1PBBmk"

    val programs = listOf(SPL_TOKEN_PROGRAM, TOKEN_METADATA_PROGRAM, AUCTION_HOUSE_PROGRAM)
}

object BlockConverter {
    private fun SolanaTransactionDto.Instruction?.isOk(programId: String): SolanaTransactionDto.Instruction? {
        return takeIf { programId in SolanaProgramId.programs }
    }

    fun convert(response: ApiResponse<SolanaBlockDto>) : ApiResponse<SolanaBlockDto>{
        val block = response.result!!
        val newTransactions = block.transactions.map { transactionDto ->
            val accountKeys = transactionDto.transaction!!.message.accountKeys
            val newInstructions = transactionDto.transaction!!.message.instructions.map { it.isOk(accountKeys[it!!.programIdIndex]) }
            val newInnerInstructions = transactionDto.meta?.innerInstructions?.map { innerInstruction ->
                SolanaTransactionDto.InnerInstruction(
                    index = innerInstruction.index,
                    instructions = innerInstruction.instructions.map { it.isOk(accountKeys[it!!.programIdIndex]) }
                )
            }

            transactionDto.copy(
                meta = SolanaTransactionDto.Meta(
                    err = transactionDto.meta?.err?.let { true },
                    innerInstructions = newInnerInstructions ?: emptyList()
                ),
                transaction = if (newInstructions.all { it == null }) {
                    null
                } else
                    transactionDto.transaction!!.copy(
                        message = transactionDto.transaction!!.message.copy(
                            instructions = newInstructions
                        )
                    )
            )
        }

        return ApiResponse(block.copy(transactions = newTransactions), response.error)
    }
}