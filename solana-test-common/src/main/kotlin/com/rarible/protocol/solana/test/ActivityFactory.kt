package com.rarible.protocol.solana.test

import com.rarible.core.test.data.randomBigDecimal
import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomBoolean
import com.rarible.core.test.data.randomInt
import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.dto.ActivityBlockchainInfoDto
import com.rarible.protocol.solana.dto.ActivityFilterAllTypeDto
import com.rarible.protocol.solana.dto.ActivityFilterByItemTypeDto
import com.rarible.protocol.solana.dto.AssetDto
import com.rarible.protocol.solana.dto.AssetTypeDto
import com.rarible.protocol.solana.dto.BurnActivityDto
import com.rarible.protocol.solana.dto.MintActivityDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.dto.SolanaSolAssetTypeDto
import com.rarible.protocol.solana.dto.TransferActivityDto
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.temporal.ChronoUnit

fun randomBlockchainInfo(
    blockHash: String = randomString(),
    blockNumber: Long = randomLong(),
    innerInstructionIndex: Int? = randomInt(),
    instructionIndex: Int = randomInt(),
    transactionHash: String = randomString(),
    transactionIndex: Int = randomInt(),
) = ActivityBlockchainInfoDto(
    blockNumber = blockNumber,
    blockHash = blockHash,
    transactionIndex = transactionIndex,
    transactionHash = transactionHash,
    instructionIndex = instructionIndex,
    innerInstructionIndex = innerInstructionIndex
)

fun randomActivityId() = "id:" + randomString()

fun randomMintActivity(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    owner: String = randomAccount(),
    tokenAddress: String = randomMint(),
    value: BigInteger = randomBigInt(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
) = MintActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    owner = owner,
    tokenAddress = tokenAddress,
    value = value,
    blockchainInfo = blockchainInfo
)

fun randomBurn(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    owner: String = randomAccount(),
    tokenAddress: String = randomMint(),
    value: BigInteger = randomBigInt(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
) = BurnActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    owner = owner,
    tokenAddress = tokenAddress,
    value = value,
    blockchainInfo = blockchainInfo
)

fun randomTransfer(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    from: String = randomAccount(),
    owner: String = randomAccount(),
    tokenAddress: String = randomMint(),
    value: BigInteger = randomBigInt(),
    purchase: Boolean = randomBoolean(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
) = TransferActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    from = from,
    owner = owner,
    tokenAddress = tokenAddress,
    value = value,
    purchase = purchase,
    blockchainInfo = blockchainInfo
)

fun randomAssetSol(
    type: AssetTypeDto = SolanaSolAssetTypeDto(),
    value: BigDecimal = randomBigDecimal(),
) = AssetDto(type, value)

fun randomAssetNft(
    type: AssetTypeDto = SolanaNftAssetTypeDto(mint = randomMint()),
    value: BigDecimal = BigDecimal.ONE,
) = AssetDto(type, value)

fun randomList(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    hash: String = randomString(),
    maker: String = randomAccount(),
    make: AssetDto = randomAssetNft(),
    take: AssetDto = randomAssetSol(),
    price: BigDecimal = randomBigDecimal(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
) = OrderListActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    hash = hash,
    maker = maker,
    make = make,
    take = take,
    price = price,
    blockchainInfo = blockchainInfo
)

fun randomCancelList(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    hash: String = randomString(),
    maker: String = randomAccount(),
    make: AssetTypeDto = SolanaNftAssetTypeDto(mint = randomMint()),
    take: AssetTypeDto = SolanaSolAssetTypeDto(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
) = OrderCancelListActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    hash = hash,
    maker = maker,
    make = make,
    take = take,
    blockchainInfo = blockchainInfo
)

fun randomBid(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    hash: String = randomString(),
    maker: String = randomAccount(),
    make: AssetDto = randomAssetSol(),
    take: AssetDto = randomAssetNft(),
    price: BigDecimal = randomBigDecimal(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
) = OrderBidActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    hash = hash,
    maker = maker,
    make = make,
    take = take,
    price = price,
    blockchainInfo = blockchainInfo
)

fun randomCancelBid(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    hash: String = randomString(),
    maker: String = randomAccount(),
    make: AssetTypeDto = SolanaNftAssetTypeDto(mint = randomMint()),
    take: AssetTypeDto = SolanaSolAssetTypeDto(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
) = OrderCancelBidActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    hash = hash,
    maker = maker,
    make = make,
    take = take,
    blockchainInfo = blockchainInfo
)

fun randomSell(
    id: String = randomActivityId(),
    date: Instant = randomTimestamp(),
    reverted: Boolean = false,
    nft: AssetDto = randomAssetNft(),
    payment: AssetDto = randomAssetSol(),
    buyer: String = randomAccount(),
    seller: String = randomAccount(),
    buyerOrderHash: String = randomString(),
    sellerOrderHash: String = randomString(),
    price: BigDecimal = randomBigDecimal(),
    blockchainInfo: ActivityBlockchainInfoDto = randomBlockchainInfo(),
    type: OrderMatchActivityDto.Type = OrderMatchActivityDto.Type.SELL,
) = OrderMatchActivityDto(
    id = id,
    date = date,
    reverted = reverted,
    nft = nft,
    payment = payment,
    buyer = buyer,
    seller = seller,
    buyerOrderHash = buyerOrderHash,
    sellerOrderHash = sellerOrderHash,
    price = price,
    blockchainInfo = blockchainInfo,
    type = type
)

fun randomTimestamp() = Instant
    .ofEpochMilli(randomLong(1648771200000, 1651363200000))
    .truncatedTo(ChronoUnit.MILLIS)!!

fun activityClassByType(type: ActivityFilterAllTypeDto) = when (type) {
    ActivityFilterAllTypeDto.TRANSFER -> TransferActivityDto::class.java
    ActivityFilterAllTypeDto.MINT -> MintActivityDto::class.java
    ActivityFilterAllTypeDto.BURN -> BurnActivityDto::class.java
    ActivityFilterAllTypeDto.BID -> OrderBidActivityDto::class.java
    ActivityFilterAllTypeDto.LIST -> OrderListActivityDto::class.java
    ActivityFilterAllTypeDto.SELL -> OrderMatchActivityDto::class.java
    ActivityFilterAllTypeDto.CANCEL_BID -> OrderCancelBidActivityDto::class.java
    ActivityFilterAllTypeDto.CANCEL_LIST -> OrderCancelListActivityDto::class.java
}

fun activityClassByType(type: ActivityFilterByItemTypeDto) = when (type) {
    ActivityFilterByItemTypeDto.TRANSFER -> TransferActivityDto::class.java
    ActivityFilterByItemTypeDto.MINT -> MintActivityDto::class.java
    ActivityFilterByItemTypeDto.BURN -> BurnActivityDto::class.java
    ActivityFilterByItemTypeDto.BID -> OrderBidActivityDto::class.java
    ActivityFilterByItemTypeDto.LIST -> OrderListActivityDto::class.java
    ActivityFilterByItemTypeDto.SELL -> OrderMatchActivityDto::class.java
    ActivityFilterByItemTypeDto.CANCEL_BID -> OrderCancelBidActivityDto::class.java
    ActivityFilterByItemTypeDto.CANCEL_LIST -> OrderCancelListActivityDto::class.java
}
