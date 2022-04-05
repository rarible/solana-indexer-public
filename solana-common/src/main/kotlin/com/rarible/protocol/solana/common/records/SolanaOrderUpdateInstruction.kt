package com.rarible.protocol.solana.common.records

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

// Fake log-record instructions for internal updates, caused by indexer logic, not blockchain events
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(name = "BALANCE_UPDATE", value = SolanaOrderUpdateInstruction.BalanceUpdate::class)
)
sealed class SolanaOrderUpdateInstruction {

    data class BalanceUpdate(
        val account: String // Originally not needed, but data class requires at least 1 field
    ) : SolanaOrderUpdateInstruction()

}
