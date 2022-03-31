@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import java.nio.ByteBuffer
import java.nio.ByteOrder

private fun ByteBuffer.parseUpdateMetadataAccountInstruction(
    version: MetaplexMetadata.DataVersion
): MetaplexUpdateMetadataAccount {
    val data = readNullable { readData(version) }
    val updateAuthority = readNullable { readPubkey() }
    val primarySaleHappened = readNullable { readBoolean() }
    val mutable = if (version == MetaplexMetadata.DataVersion.V2) {
        readNullable { readBoolean() }
    } else {
        null
    }
    val updateArgs = MetaplexMetadata.UpdateAccountArgs(
        metadata = data,
        updateAuthority = updateAuthority,
        primarySaleHappened = primarySaleHappened,
        mutable = mutable
    )
    return MetaplexUpdateMetadataAccount(updateArgs)
}

internal fun ByteBuffer.parseMetaplexCreateMetadataAccountInstruction(
    version: MetaplexMetadata.DataVersion
): MetaplexMetadataInstruction {
    val data = readData(version)
    val mutable = readBoolean()
    val createArgs = MetaplexMetadata.CreateAccountArgs(metadata = data, mutable = mutable)
    return MetaplexCreateMetadataAccount(createArgs)
}

private fun ByteBuffer.readData(
    version: MetaplexMetadata.DataVersion
): MetaplexMetadata.Data {
    val name = readString()
    val symbol = readString()
    val uri = readString()
    val sellerFeeBasisPoints = getShort()
    val creators = readNullable { List(getInt()) { readCreator() } }
    val collection = if (version == MetaplexMetadata.DataVersion.V2) {
        readNullable { readCollection() }
    } else {
        null
    }

    // If necessary, add Uses to the model here.
    if (version == MetaplexMetadata.DataVersion.V2) {
        // Skip the Uses struct (17 bytes).
        readNullable { repeat(17) { get() } }
    }

    return MetaplexMetadata.Data(
        name = name,
        symbol = symbol,
        uri = uri,
        sellerFeeBasisPoints = sellerFeeBasisPoints,
        creators = creators,
        collection = collection
    )
}

private fun ByteBuffer.readCollection(): MetaplexMetadata.Collection {
    val verified = readBoolean()
    val key = readPubkey()

    return MetaplexMetadata.Collection(key = key, verified = verified)
}

private fun ByteBuffer.readCreator(): MetaplexMetadata.Creator {
    val address = readPubkey()
    val verified = readBoolean()
    val share = get()

    return MetaplexMetadata.Creator(address, verified, share)
}

fun String.parseMetaplexMetadataInstruction(): MetaplexMetadataInstruction? {
    val bytes = Base58.decode(this)
    val buffer = ByteBuffer.wrap(bytes).apply { order(ByteOrder.LITTLE_ENDIAN) }
    return when (buffer.get().toInt()) {
        0 -> buffer.parseMetaplexCreateMetadataAccountInstruction(MetaplexMetadata.DataVersion.V1)
        1 -> buffer.parseUpdateMetadataAccountInstruction(MetaplexMetadata.DataVersion.V1)
        2 -> null // DeprecatedCreateMasterEdition(CreateMasterEditionArgs),
        3 -> null // DeprecatedMintNewEditionFromMasterEditionViaPrintingToken,
        4 -> null // UpdatePrimarySaleHappenedViaToken,
        5 -> null // DeprecatedSetReservationList(SetReservationListArgs),
        6 -> null // DeprecatedCreateReservationList,
        7 -> SignMetadata
        8 -> null // DeprecatedMintPrintingTokensViaToken(MintPrintingTokensViaTokenArgs),
        9 -> null // DeprecatedMintPrintingTokens(MintPrintingTokensViaTokenArgs),
        10 -> null // CreateMasterEdition(CreateMasterEditionArgs),
        11 -> null // MintNewEditionFromMasterEditionViaToken(MintNewEditionFromMasterEditionViaTokenArgs),
        12 -> null // ConvertMasterEditionV1ToV2,
        13 -> null // MintNewEditionFromMasterEditionViaVaultProxy(MintNewEditionFromMasterEditionViaTokenArgs),
        14 -> null // PuffMetadata,
        15 -> buffer.parseUpdateMetadataAccountInstruction(MetaplexMetadata.DataVersion.V2)
        16 -> buffer.parseMetaplexCreateMetadataAccountInstruction(MetaplexMetadata.DataVersion.V2)
        17 -> null // CreateMasterEditionV3(CreateMasterEditionArgs),
        18 -> VerifyCollection
        19 -> null // Utilize(UtilizeArgs),
        20 -> null // ApproveUseAuthority(ApproveUseAuthorityArgs),
        21 -> null // RevokeUseAuthority,
        22 -> UnVerifyCollection
        23 -> null // ApproveCollectionAuthority,
        24 -> null // RevokeCollectionAuthority,
        25 -> SetAndVerifyCollection
        26 -> null // FreezeDelegatedAccount,
        27 -> null // ThawDelegatedAccount
        else -> null
    }
}

sealed class MetaplexMetadataInstruction

data class MetaplexCreateMetadataAccount(
    val createArgs: MetaplexMetadata.CreateAccountArgs
) : MetaplexMetadataInstruction()

data class MetaplexUpdateMetadataAccount(
    val updateArgs: MetaplexMetadata.UpdateAccountArgs
) : MetaplexMetadataInstruction()

object SignMetadata : MetaplexMetadataInstruction()

object SetAndVerifyCollection : MetaplexMetadataInstruction()

object VerifyCollection : MetaplexMetadataInstruction()

object UnVerifyCollection : MetaplexMetadataInstruction()
