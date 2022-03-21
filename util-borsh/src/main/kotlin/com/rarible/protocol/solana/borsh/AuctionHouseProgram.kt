@file:Suppress("UsePropertyAccessSyntax")

package com.rarible.protocol.solana.borsh

import java.nio.ByteBuffer
import java.nio.ByteOrder

sealed class AuctionHouseInstruction

data class CreateAuctionHouse(
    val bump: UByte,
    val feePayerBump: UByte,
    val treasuryBump: UByte,
    val sellerFeeBasisPoints: UShort,
    val requiresSignOff: Boolean,
    val canChangeSalePrice: Boolean
) : AuctionHouseInstruction()

data class UpdateAuctionHouse(
    val sellerFeeBasisPoints: UShort,
    val requiresSignOff: Boolean,
    val canChangeSalePrice: Boolean
) : AuctionHouseInstruction()

data class Buy(
    val price: ULong,
    val size: ULong
) : AuctionHouseInstruction()

data class Sell(
    val price: ULong,
    val size: ULong
) : AuctionHouseInstruction()

data class ExecuteSale(
    val buyerPrice: ULong,
    val size: ULong
) : AuctionHouseInstruction()

internal fun ByteBuffer.parseCreateAuctionHouse(): CreateAuctionHouse {
    val bump = get().toUByte()
    val feePayerBump = get().toUByte()
    val treasuryBump = get().toUByte()
    val sellerFeeBasisPoints = getShort().toUShort()
    val requiresSignOff = readBoolean()
    val canChangeSalePrice = readBoolean()

    return CreateAuctionHouse(bump, feePayerBump, treasuryBump, sellerFeeBasisPoints, requiresSignOff, canChangeSalePrice)
}

internal fun ByteBuffer.parseUpdateAuctionHouse(): UpdateAuctionHouse {
    val sellerFeeBasisPoints = getShort()
    val requiresSignOff = readBoolean()
    val canChangeSalePrice = readBoolean()

    return UpdateAuctionHouse(sellerFeeBasisPoints.toUShort(), requiresSignOff, canChangeSalePrice)
}

internal fun ByteBuffer.parseBuy(): Buy {
    repeat(2) { get() } // skipped
    val price = getLong().toULong()
    val size = getLong().toULong()

    return Buy(price, size)
}

internal fun ByteBuffer.parseSell(): Sell {
    repeat(3) { get() } // skipped
    val price = getLong().toULong()
    val size = getLong().toULong()

    return Sell(price, size)
}

internal fun ByteBuffer.parseExecuteSell(): ExecuteSale {
    repeat(3) { get() } // skipped
    val buyerPrice = getLong().toULong()
    val size = getLong().toULong()

    return ExecuteSale(buyerPrice, size)
}

fun String.parseAuctionHouseInstruction(): AuctionHouseInstruction? {
    val bytes = Base58.decode(this)
    val buffer = ByteBuffer.wrap(bytes).apply { order(ByteOrder.LITTLE_ENDIAN) }

    val decoder = when (buffer.getLong()) {
        -5943767438166989261L -> ByteBuffer::parseSell
        -1518880751171598746L -> ByteBuffer::parseBuy
        -1042918692164058403L -> ByteBuffer::parseCreateAuctionHouse
        -2597168572136237228L -> ByteBuffer::parseUpdateAuctionHouse
        442251406432881189L -> ByteBuffer::parseExecuteSell
        else -> return null
    }

    return decoder(buffer)
}