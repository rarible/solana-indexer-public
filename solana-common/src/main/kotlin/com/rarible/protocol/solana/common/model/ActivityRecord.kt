package com.rarible.protocol.solana.common.model

import com.rarible.protocol.solana.common.util.getNftMint
import com.rarible.protocol.solana.dto.ActivityBlockchainInfoDto
import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.ActivityTypeDto
import com.rarible.protocol.solana.dto.AssetDto
import com.rarible.protocol.solana.dto.AssetTypeDto
import com.rarible.protocol.solana.dto.BurnActivityDto
import com.rarible.protocol.solana.dto.MintActivityDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.dto.TransferActivityDto
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant

sealed class ActivityRecord {
    abstract val id: String
    abstract val date: Instant
    abstract val type: ActivityTypeDto
    abstract val mint: String
    abstract val owner: String
    abstract val blockchainInfo: ActivityBlockchainInfoDto
    abstract val dbUpdatedAt: Instant

    abstract fun toDto(): ActivityDto
    abstract fun withDbUpdatedAt(): ActivityRecord
}

fun ActivityDto.asRecord(): ActivityRecord = when (this) {
    is MintActivityDto -> MintActivityRecord(this)
    is BurnActivityDto -> BurnActivityRecord(this)
    is TransferActivityDto -> TransferActivityRecord(this)
    is OrderMatchActivityDto -> MatchActivityRecord(this)
    is OrderListActivityDto -> ListActivityRecord(this)
    is OrderCancelListActivityDto -> CancelListActivityRecord(this)
    is OrderBidActivityDto -> BidActivityRecord(this)
    is OrderCancelBidActivityDto -> CancelBidActivityRecord(this)
}

data class MintActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val value: BigInteger,
    override val type: ActivityTypeDto = ActivityTypeDto.MINT,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: MintActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.tokenAddress,
        owner = dto.owner,
        blockchainInfo = dto.blockchainInfo,
        value = dto.value,
    )

    override fun toDto() = MintActivityDto(
        id = id,
        date = date,
        reverted = false,
        owner = owner,
        tokenAddress = mint,
        value = value,
        blockchainInfo = blockchainInfo,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}

data class BurnActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val value: BigInteger,
    override val type: ActivityTypeDto = ActivityTypeDto.BURN,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: BurnActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.tokenAddress,
        owner = dto.owner,
        blockchainInfo = dto.blockchainInfo,
        value = dto.value,
    )

    override fun toDto() = BurnActivityDto(
        id = id,
        date = date,
        reverted = false,
        owner = owner,
        tokenAddress = mint,
        value = value,
        blockchainInfo = blockchainInfo,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}

data class TransferActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val value: BigInteger,
    val from: String,
    val purchase: Boolean,
    override val type: ActivityTypeDto = ActivityTypeDto.TRANSFER,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: TransferActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.tokenAddress,
        owner = dto.owner,
        blockchainInfo = dto.blockchainInfo,
        value = dto.value,
        from = dto.from,
        purchase = dto.purchase,
    )

    override fun toDto() = TransferActivityDto(
        id = id,
        date = date,
        reverted = false,
        from = from,
        owner = owner,
        tokenAddress = mint,
        value = value,
        purchase = purchase,
        blockchainInfo = blockchainInfo,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}

data class ListActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetDto,
    val take: AssetDto,
    val price: BigDecimal,
    override val type: ActivityTypeDto = ActivityTypeDto.LIST,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: OrderListActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.make.type.getNftMint().orEmpty(),
        owner = dto.maker,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
        price = dto.price,
    )

    override fun toDto() = OrderListActivityDto(
        id = id,
        date = date,
        reverted = false,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        price = price,
        blockchainInfo = blockchainInfo,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}

data class BidActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetDto,
    val take: AssetDto,
    val price: BigDecimal,
    override val type: ActivityTypeDto = ActivityTypeDto.BID,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: OrderBidActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.take.type.getNftMint().orEmpty(),
        owner = dto.maker,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
        price = dto.price,
    )

    override fun toDto() = OrderBidActivityDto(
        id = id,
        date = date,
        reverted = false,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        price = price,
        blockchainInfo = blockchainInfo,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}

data class CancelListActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetTypeDto,
    val take: AssetTypeDto,
    override val type: ActivityTypeDto = ActivityTypeDto.CANCEL_LIST,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: OrderCancelListActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.make.getNftMint().orEmpty(),
        owner = dto.maker,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
    )

    override fun toDto() = OrderCancelListActivityDto(
        id = id,
        date = date,
        reverted = false,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        blockchainInfo = blockchainInfo,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}

data class CancelBidActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetTypeDto,
    val take: AssetTypeDto,
    override val type: ActivityTypeDto = ActivityTypeDto.CANCEL_BID,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: OrderCancelBidActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.take.getNftMint().orEmpty(),
        owner = dto.maker,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
    )

    override fun toDto() = OrderCancelBidActivityDto(
        id = id,
        date = date,
        reverted = false,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        blockchainInfo = blockchainInfo,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}

data class MatchActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val nft: AssetDto,
    val payment: AssetDto,
    val buyer: String,
    val seller: String,
    val buyerOrderHash: String?,
    val sellerOrderHash: String?,
    val price: BigDecimal,
    val orderType: OrderMatchActivityDto.Type,
    override val type: ActivityTypeDto = ActivityTypeDto.SELL,
    override val dbUpdatedAt: Instant = Instant.now()
) : ActivityRecord() {

    constructor(dto: OrderMatchActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.nft.type.getNftMint().orEmpty(),
        owner = dto.seller,
        blockchainInfo = dto.blockchainInfo,
        nft = dto.nft,
        payment = dto.payment,
        buyer = dto.buyer,
        seller = dto.seller,
        buyerOrderHash = dto.buyerOrderHash,
        sellerOrderHash = dto.sellerOrderHash,
        price = dto.price,
        orderType = dto.type,
    )

    override fun toDto() = OrderMatchActivityDto(
        id = id,
        date = date,
        reverted = false,
        nft = nft,
        payment = payment,
        buyer = buyer,
        seller = seller,
        buyerOrderHash = buyerOrderHash,
        sellerOrderHash = sellerOrderHash,
        price = price,
        blockchainInfo = blockchainInfo,
        type = orderType,
        dbUpdatedAt = dbUpdatedAt
    )

    override fun withDbUpdatedAt() =
        copy(dbUpdatedAt = Instant.now())
}
