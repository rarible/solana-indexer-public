@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import org.bitcoinj.core.Base58
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

internal inline fun <reified T> ByteBuffer.readNullable(block: (ByteBuffer) -> T) : T? =
    if (get().toInt() == 0) null else block(this)

internal fun ByteBuffer.readBoolean(): Boolean =
    get().toInt() == 1

internal fun ByteBuffer.readString(): String {
    val length = getInt()
    val bytes = ByteArray(length).apply { get(this) }

    return String(bytes, StandardCharsets.UTF_8)
}

internal fun ByteBuffer.readPubkey(): Pubkey {
    val bytes = ByteArray(32).apply { get(this) }

    return Base58.encode(bytes)
}