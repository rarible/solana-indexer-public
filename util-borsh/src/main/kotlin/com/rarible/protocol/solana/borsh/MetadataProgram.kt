@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import org.bitcoinj.core.Base58
import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class MetadataInstruction

data class Creator(
    val address: Pubkey,
    val verified: Boolean,
    // In percentages, NOT basis points ;) Watch out!
    val share: Byte,
)

data class Collection(
    val key: Pubkey,
    val verified: Boolean
)

data class Data(
    val name: String,
    /// The symbol for the asset
    val symbol: String,
    /// URI pointing to JSON representing the asset
    val uri: String,
    /// Royalty basis points that goes to creators in secondary sales (0-10000)
    val sellerFeeBasisPoints: Short,
    /// Array of creators, optional
    val creators: List<Creator>?,
    val collection: Collection?
)

data class CreateMetadataAccountArgs(
    val data: Data,
    val mutable: Boolean
) : MetadataInstruction()

internal fun ByteBuffer.parseCreateMetadataAccountArgs(): CreateMetadataAccountArgs {
    val name = readString()
    val symbol = readString()
    val uri = readString()
    val sellerFeeBasisPoints = getShort()
    val creators = readNullable { List(getInt()) { readCreator() } }
    val mutable = readBoolean()
    val collection = readOptional { readNullable { readCollection() } }

    return CreateMetadataAccountArgs(
        Data(
            name = name,
            symbol = symbol,
            uri = uri,
            sellerFeeBasisPoints = sellerFeeBasisPoints,
            creators = creators,
            collection = collection
        ),
        mutable
    )
}

private fun ByteBuffer.readCollection(): Collection {
    val verified = readBoolean()
    val key = readPubkey()

    return Collection(key = key, verified = verified)
}

private fun ByteBuffer.readCreator(): Creator {
    val address = readPubkey()
    val verified = readBoolean()
    val share = get()

    return Creator(address, verified, share)
}

fun String.parseMetadataInstruction(): MetadataInstruction? {
    val bytes = Base58.decode(this)
    val buffer = ByteBuffer.wrap(bytes).apply { order(ByteOrder.LITTLE_ENDIAN) }

    val decoder = when (buffer.get().toInt()) {
        0, 16 -> ByteBuffer::parseCreateMetadataAccountArgs
        else -> return null
    }

    return decoder(buffer)
}