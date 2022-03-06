@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class TokenInstruction

typealias Pubkey = String

data class InitializeMint1and2(
    val decimal: UByte,
    val mintAuthority: Pubkey,
    val freezeAuthority: Pubkey?
) : TokenInstruction()

object InitializeAccount : TokenInstruction()

data class InitializeMultisig(
    val m: UByte
) : TokenInstruction()

data class Transfer(
    val amount: ULong
) : TokenInstruction()

data class Approve(
    val amount: ULong
) : TokenInstruction()

object Revoke : TokenInstruction()

data class SetAuthority(
    val authorityType: AuthorityType,
    val newAuthority: Pubkey?
) : TokenInstruction()

data class MintTo(
    val amount: ULong
) : TokenInstruction()

data class Burn(
    val amount: ULong
) : TokenInstruction()

object CloseAccount : TokenInstruction()
object FreezeAccount : TokenInstruction()
object ThawAccount : TokenInstruction()

data class TransferChecked(
    val amount: ULong,
    val decimals: UByte
) : TokenInstruction()

data class ApproveChecked(
    val amount: ULong,
    val decimals: UByte
) : TokenInstruction()

data class MintToChecked(
    val amount: ULong,
    val decimals: UByte
) : TokenInstruction()

data class BurnChecked(
    val amount: ULong,
    val decimals: UByte
) : TokenInstruction()

data class InitializeAccount2and3(
    val owner: Pubkey
) : TokenInstruction()

object SyncNative : TokenInstruction()

enum class AuthorityType {
    MintTokens,
    FreezeAccount,
    AccountOwner,
    CloseAccount,
}

internal fun ByteBuffer.parseInitializeMint(): InitializeMint1and2 {
    val decimals = get().toUByte()
    val mintAuthority = readPubkey()
    val freezeAuthority = readNullable { readPubkey() }

    return InitializeMint1and2(decimals, mintAuthority, freezeAuthority)
}

internal fun ByteBuffer.parseInitializeMint2(): InitializeMint1and2 {
    val decimals = get().toUByte()
    val mintAuthority = readPubkey()
    val freezeAuthority = readNullable { readPubkey() }

    return InitializeMint1and2(decimals, mintAuthority, freezeAuthority)
}

internal fun ByteBuffer.parseInitializeMultisig(): InitializeMultisig {
    val m = get().toUByte()

    return InitializeMultisig(m)
}

internal fun ByteBuffer.parseTransfer(): Transfer {
    val amount = getLong().toULong()

    return Transfer(amount)
}

internal fun ByteBuffer.parseApprove(): Approve {
    val amount = getLong().toULong()

    return Approve(amount)
}

internal fun ByteBuffer.parseSetAuthority(): SetAuthority {
    val authorityType = when (val type = get().toInt()) {
        0 -> AuthorityType.MintTokens
        1 -> AuthorityType.FreezeAccount
        2 -> AuthorityType.AccountOwner
        3 -> AuthorityType.CloseAccount
        else -> throw IllegalArgumentException("Unknown AuthorityType, type: $type")
    }
    val newAuthority = readNullable { readPubkey() }

    return SetAuthority(authorityType, newAuthority)
}

internal fun ByteBuffer.parseMintTo(): MintTo {
    val amount = getLong().toULong()

    return MintTo(amount)
}

internal fun ByteBuffer.parseBurn(): Burn {
    val amount = getLong().toULong()

    return Burn(amount)
}

internal fun ByteBuffer.parseTransferChecked(): TransferChecked {
    val amount = getLong().toULong()
    val decimals = get().toUByte()

    return TransferChecked(amount, decimals)
}

internal fun ByteBuffer.parseApproveChecked(): ApproveChecked {
    val amount = getLong().toULong()
    val decimals = get().toUByte()

    return ApproveChecked(amount, decimals)
}

internal fun ByteBuffer.parseMintToChecked(): MintToChecked {
    val amount = getLong().toULong()
    val decimals = get().toUByte()

    return MintToChecked(amount, decimals)
}

internal fun ByteBuffer.parseBurnChecked(): BurnChecked {
    val amount = getLong().toULong()
    val decimals = get().toUByte()

    return BurnChecked(amount, decimals)
}

internal fun ByteBuffer.parseInitializeAccount2(): InitializeAccount2and3 {
    val owner = readPubkey()
    return InitializeAccount2and3(owner)
}

internal fun ByteBuffer.parseInitializeAccount3(): InitializeAccount2and3 {
    val owner = readPubkey()
    return InitializeAccount2and3(owner)
}

fun String.parseTokenInstruction(): TokenInstruction? {
    val bytes = Base58.decode(this)
    val buffer = ByteBuffer.wrap(bytes).apply { order(ByteOrder.LITTLE_ENDIAN) }

    val decoder = when (buffer.get().toInt()) {
        0 -> ByteBuffer::parseInitializeMint
        1 -> { _ -> InitializeAccount }
        2 -> ByteBuffer::parseInitializeMultisig
        3 -> ByteBuffer::parseTransfer
        4 -> ByteBuffer::parseApprove
        5 -> { _ -> Revoke }
        6 -> ByteBuffer::parseSetAuthority
        7 -> ByteBuffer::parseMintTo
        8 -> ByteBuffer::parseBurn
        9 -> { _ -> CloseAccount }
        10 -> { _ -> FreezeAccount }
        11 -> { _ -> ThawAccount }
        12 -> ByteBuffer::parseTransferChecked
        13 -> ByteBuffer::parseApproveChecked
        14 -> ByteBuffer::parseMintToChecked
        15 -> ByteBuffer::parseBurnChecked
        16 -> ByteBuffer::parseInitializeAccount2
        17 -> { _ -> SyncNative }
        18 -> ByteBuffer::parseInitializeAccount3
        19 -> return null // InitializeMultisig2
        20 -> ByteBuffer::parseInitializeMint2
        21 -> return null // GetAccountDataSize
        22 -> return null // InitializeImmutableOwner
        23 -> return null // AmountToUiAmount
        24 -> return null // UiAmountToAmount
        else -> return null
    }

    return decoder(buffer)
}

/*
pub enum TokenInstruction<'a> {
    // 0
    InitializeMint {
        decimals: u8,
        mint_authority: Pubkey,
        freeze_authority: COption<Pubkey>,
    },
    // 1
    InitializeAccount,
    // 2
    InitializeMultisig {
        m: u8,
    },
    // 3
    Transfer {
        amount: u64,
    },
    // 4
    Approve {
        amount: u64,
    },
    // 5
    Revoke,
    // 6
    SetAuthority {
        authority_type: AuthorityType,
        new_authority: COption<Pubkey>,
    },
    // 7
    MintTo {
        amount: u64,
    },
    // 8
    Burn {
        amount: u64,
    },
    // 9
    CloseAccount,
    // 10
    FreezeAccount,
    // 11
    ThawAccount,
    // 12
    TransferChecked {
        amount: u64,
        decimals: u8,
    },
    // 13
    ApproveChecked {
        amount: u64,
        decimals: u8,
    },
    // 14
    MintToChecked {
        amount: u64,
        decimals: u8,
    },
    // 15
    BurnChecked {
        amount: u64,
        decimals: u8,
    },
    // 16
    InitializeAccount2 {
        owner: Pubkey,
    },
    // 17
    SyncNative,
    // 18
    InitializeAccount3 {
        owner: Pubkey,
    },
    // 19
    InitializeMultisig2 {
        m: u8,
    },
    // 20
    InitializeMint2 {
        decimals: u8,
        mint_authority: Pubkey,
        freeze_authority: COption<Pubkey>,
    },
    // 21
    GetAccountDataSize,
    // 22
    InitializeImmutableOwner,
    // 23
    AmountToUiAmount {
        amount: u64,
    },
    // 24
    UiAmountToAmount {
        ui_amount: &'a str,
    },
}

 */
