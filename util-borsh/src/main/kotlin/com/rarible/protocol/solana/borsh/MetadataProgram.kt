@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram.parseMetaplexCreateMetadataInstruction
import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram.parseUpdateMetadataAccountArgs
import java.nio.ByteBuffer
import java.nio.ByteOrder

object MetaplexMetadataProgram {
    enum class DataVersion {
        V1, V2
    }

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

    internal fun ByteBuffer.parseUpdateMetadataAccountArgs(
        version: DataVersion
    ): MetaplexUpdateMetadataAccountArgs {
        val data = readNullable { readData(version) }
        val updateAuthority = readNullable { readPubkey() }
        val primarySaleHappened = readNullable { readBoolean() }
        val mutable = if (version == DataVersion.V2) {
            readNullable { readBoolean() }
        } else {
            null
        }

        return MetaplexUpdateMetadataAccountArgs(
            data,
            updateAuthority,
            primarySaleHappened,
            mutable
        )
    }

    internal fun ByteBuffer.parseMetaplexCreateMetadataInstruction(
        version: DataVersion
    ): MetaplexMetadataInstruction {
        val data = readData(version)
        val mutable = readBoolean()

        return MetaplexCreateMetadataAccountArgs(
            data,
            mutable
        )
    }

    // TODO add Uses under version flag
    private fun ByteBuffer.readData(
        version: DataVersion
    ): Data {
        val name = readString()
        val symbol = readString()
        val uri = readString()
        val sellerFeeBasisPoints = getShort()
        val creators = readNullable { List(getInt()) { readCreator() } }
        val collection = if (version == DataVersion.V2) {
            readNullable { readCollection() }
        } else {
            null
        }

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
        0 -> buffer.parseMetaplexCreateMetadataInstruction(MetaplexMetadataProgram.DataVersion.V1)
        16 -> buffer.parseMetaplexCreateMetadataInstruction(MetaplexMetadataProgram.DataVersion.V2)
        1 -> buffer.parseUpdateMetadataAccountArgs(MetaplexMetadataProgram.DataVersion.V1)
        15 -> buffer.parseUpdateMetadataAccountArgs(MetaplexMetadataProgram.DataVersion.V2)
        18 -> VerifyCollection
        else -> null
    }
}

sealed class MetaplexMetadataInstruction

data class MetaplexCreateMetadataAccountArgs(
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
