package com.rarible.protocol.solana.nft.listener.test.data

import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainBlock
import com.rarible.blockchain.scanner.solana.client.SolanaBlockchainLog
import com.rarible.blockchain.scanner.solana.client.SolanaInstruction
import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.borsh.Base58
import com.rarible.protocol.solana.common.pubkey.SolanaProgramId
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun randomSolanaBlockchainBlock(
    logs: List<SolanaBlockchainLog> = emptyList()
): SolanaBlockchainBlock {
    val slot = randomLong()
    return SolanaBlockchainBlock(
        slot = slot,
        parentSlot = slot - 1,
        logs = logs,
        hash = randomString(),
        parentHash = randomString(),
        timestamp = nowMillis().toEpochMilli() / 1000
    )
}

fun randomSaleInstruction(
    maker: String = randomString(),
    tokenAccount: String = randomString(),
    auctionHouse: String = randomString(),
    price: Long = randomLong(),
    size: Long = randomLong()
): SolanaInstruction {

    val data = ByteBuffer.allocate(27)
        .apply { order(ByteOrder.LITTLE_ENDIAN) }
        .putLong(-5943767438166989261L)
        .put(ByteArray(3))
        .putLong(price)
        .putLong(size)

    return SolanaInstruction(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        data = Base58.encode(data.array()),
        accounts = listOf(
            maker, // 0
            tokenAccount, // 1
            "",
            "",
            auctionHouse // 4
        )
    )
}

fun randomBuyInstruction(
    maker: String = randomString(),
    tokenAccount: String = randomString(),
    treasuryMint: String = randomString(),
    auctionHouse: String = randomString(),
    price: Long = randomLong(),
    size: Long = randomLong()
): SolanaInstruction {

    val data = ByteBuffer.allocate(26)
        .apply { order(ByteOrder.LITTLE_ENDIAN) }
        .putLong(-1518880751171598746L)
        .put(ByteArray(2))
        .putLong(price)
        .putLong(size)

    return SolanaInstruction(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        data = Base58.encode(data.array()),
        accounts = listOf(
            maker, // 0
            "",
            "",
            treasuryMint, // 3
            tokenAccount, // 4
            "",
            "",
            "",
            auctionHouse // 8
        )
    )
}

fun randomExecuteSaleInstruction(
    seller: String = randomString(),
    buyer: String = randomString(),
    mint: String = randomString(),
    treasuryMint: String = randomString(),
    auctionHouse: String = randomString(),
    buyerPrice: Long = randomLong(),
    size: Long = randomLong()
): SolanaInstruction {

    val data = ByteBuffer.allocate(27)
        .apply { order(ByteOrder.LITTLE_ENDIAN) }
        .putLong(442251406432881189L)
        .put(ByteArray(3))
        .putLong(buyerPrice)
        .putLong(size)

    return SolanaInstruction(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        data = Base58.encode(data.array()),
        accounts = listOf(
            buyer, // 0
            seller, // 1
            "",
            mint, // 3
            "",
            treasuryMint, // 5
            "",
            "",
            "",
            "",
            auctionHouse // 10
        )
    )
}

fun randomCancelInstruction(
    maker: String = randomString(),
    mint: String = randomString(),
    auctionHouse: String = randomString(),
    price: Long = randomLong(),
    size: Long = randomLong()
): SolanaInstruction {

    val data = ByteBuffer.allocate(24)
        .apply { order(ByteOrder.LITTLE_ENDIAN) }
        .putLong(-4693616285582369816L)
        .putLong(price)
        .putLong(size)

    return SolanaInstruction(
        programId = SolanaProgramId.AUCTION_HOUSE_PROGRAM,
        data = Base58.encode(data.array()),
        accounts = listOf(
            maker, // 0
            "",
            mint, // 2
            "",
            auctionHouse // 4
        )
    )
}