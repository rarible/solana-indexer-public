@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import com.rarible.protocol.solana.borsh.MetaplexMetadataProgram.parseMetaplexCreateMetadataAccountInstruction
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

    internal fun ByteBuffer.parseMetaplexCreateMetadataAccountInstruction(
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
        0 -> buffer.parseMetaplexCreateMetadataAccountInstruction(MetaplexMetadataProgram.DataVersion.V1)
        1 -> buffer.parseUpdateMetadataAccountArgs(MetaplexMetadataProgram.DataVersion.V1)
        2 -> null // DeprecatedCreateMasterEdition(CreateMasterEditionArgs),
        3 -> null // DeprecatedMintNewEditionFromMasterEditionViaPrintingToken,
        4 -> null // UpdatePrimarySaleHappenedViaToken,
        5 -> null // DeprecatedSetReservationList(SetReservationListArgs),
        6 -> null // DeprecatedCreateReservationList,
        7 -> null // SignMetadata,
        8 -> null // DeprecatedMintPrintingTokensViaToken(MintPrintingTokensViaTokenArgs),
        9 -> null // DeprecatedMintPrintingTokens(MintPrintingTokensViaTokenArgs),
        10 -> null // CreateMasterEdition(CreateMasterEditionArgs),
        11 -> null // MintNewEditionFromMasterEditionViaToken(MintNewEditionFromMasterEditionViaTokenArgs),
        12 -> null // ConvertMasterEditionV1ToV2,
        13 -> null // MintNewEditionFromMasterEditionViaVaultProxy(MintNewEditionFromMasterEditionViaTokenArgs),
        14 -> null // PuffMetadata,
        15 -> buffer.parseUpdateMetadataAccountArgs(MetaplexMetadataProgram.DataVersion.V2)
        16 -> buffer.parseMetaplexCreateMetadataAccountInstruction(MetaplexMetadataProgram.DataVersion.V2)
        17 -> null // CreateMasterEditionV3(CreateMasterEditionArgs),
        18 -> VerifyCollection
        19 -> null // Utilize(UtilizeArgs),
        20 -> null // ApproveUseAuthority(ApproveUseAuthorityArgs),
        21 -> null // RevokeUseAuthority,
        22 -> UnVerifyCollection
        23 -> null // ApproveCollectionAuthority,
        24 -> null // RevokeCollectionAuthority,
        25 -> null // SetAndVerifyCollection,
        26 -> null // FreezeDelegatedAccount,
        27 -> null // ThawDelegatedAccount
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

object UnVerifyCollection : MetaplexMetadataInstruction()
