@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram.parseMetaplexCreateMetadataInstruction
import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram.parseUpdateMetadataAccountArgs
import org.bitcoinj.core.Base58
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MetaplexMetadataProgram {
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

    internal fun ByteBuffer.parseUpdateMetadataAccountArgs(): MetaplexUpdateMetadataAccountArgs {
        val data = readNullable { readData() }
        val updateAuthority = readNullable { readPubkey() }
        val primarySaleHappened = readNullable { readBoolean() }
        val mutable = readOptional { readNullable { readBoolean() } }

        return MetaplexUpdateMetadataAccountArgs(
            data,
            updateAuthority,
            primarySaleHappened,
            mutable
        )
    }

    internal fun ByteBuffer.parseMetaplexCreateMetadataInstruction(): MetaplexMetadataInstruction {
        val data = readData()
        val mutable = readBoolean()

        return MetaplexCreateMetadataAccount(
            data,
            mutable
        )
    }

    private fun ByteBuffer.readData(): Data {
        val name = readString()
        val symbol = readString()
        val uri = readString()
        val sellerFeeBasisPoints = getShort()
        val creators = readNullable { List(getInt()) { readCreator() } }
        val collection = readOptional { readNullable { readCollection() } }

        return Data(
            name = name,
            symbol = symbol,
            uri = uri,
            sellerFeeBasisPoints = sellerFeeBasisPoints,
            creators = creators,
            collection = collection
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
}

fun String.parseMetaplexMetadataInstruction(): MetaplexMetadataInstruction? {
    val bytes = Base58.decode(this)
    val buffer = ByteBuffer.wrap(bytes).apply { order(ByteOrder.LITTLE_ENDIAN) }
    return when (buffer.get().toInt()) {
        0, 16 -> buffer.parseMetaplexCreateMetadataInstruction()
        1, 15 -> buffer.parseUpdateMetadataAccountArgs()
        18 -> VerifyCollection
        else -> null
    }
}

sealed class MetaplexMetadataInstruction

data class MetaplexCreateMetadataAccount(
    val metadata: MetaplexMetadataProgram.Data,
    val mutable: Boolean
) : MetaplexMetadataInstruction()

data class MetaplexUpdateMetadataAccountArgs(
    val metadata: MetaplexMetadataProgram.Data?,
    val updateAuthority: Pubkey?,
    val primarySaleHappened: Boolean?,
    val mutable: Boolean?
) : MetaplexMetadataInstruction()

object VerifyCollection : MetaplexMetadataInstruction()