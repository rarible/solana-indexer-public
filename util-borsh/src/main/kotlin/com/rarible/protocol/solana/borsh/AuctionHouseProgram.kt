@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class AuctionHouseInstruction

data class CreateAuctionHouse(
    val bump: UByte,
    val feePayerBump: UByte,
    val sellerFeeBasisPoints: UShort,
    val requiresSignOff: Boolean,
    val canChangeSalePrice: Boolean
) : AuctionHouseInstruction()

data class UpdateAuctionHouse(
    val sellerFeeBasisPoints: UShort,
    val requiresSignOff: Boolean,
    val canChangeSalePrice: Boolean
) : AuctionHouseInstruction()

internal fun ByteBuffer.parseCreateAuctionHouse(): CreateAuctionHouse {
    val bump = get().toUByte()
    val feePayerBump = get().toUByte()
    val sellerFeeBasisPoints = getShort().toUShort()
    val requiresSignOff = readBoolean()
    val canChangeSalePrice = readBoolean()

    return CreateAuctionHouse(bump, feePayerBump, sellerFeeBasisPoints, requiresSignOff, canChangeSalePrice)
}

internal fun ByteBuffer.parseUpdateAuctionHouse(): UpdateAuctionHouse {
    val sellerFeeBasisPoints = getShort()
    val requiresSignOff = readBoolean()
    val canChangeSalePrice = readBoolean()

    return UpdateAuctionHouse(sellerFeeBasisPoints.toUShort(), requiresSignOff, canChangeSalePrice)
}

fun String.parseAuctionHouseInstruction(): AuctionHouseInstruction? {
    val bytes = Base58.decode(this)
    val buffer = ByteBuffer.wrap(bytes).apply { order(ByteOrder.LITTLE_ENDIAN) }

    val decoder = when (buffer.get().toInt()) {
        64 -> ByteBuffer::parseCreateAuctionHouse
        84 -> ByteBuffer::parseUpdateAuctionHouse
        else -> return null
    }

    return decoder(buffer)
}