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

interface ActivityRecord {
    val id: String
    val date: Instant
    val type: ActivityTypeDto
    val mint: String
    val owner: String
    val reverted: Boolean
    val blockchainInfo: ActivityBlockchainInfoDto

    fun toDto(): ActivityDto
}

fun ActivityDto.asRecord(): ActivityRecord? = when (this) {
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
    override val reverted: Boolean,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val value: BigInteger,
    override val type: ActivityTypeDto = ActivityTypeDto.MINT,
) : ActivityRecord {

    constructor(dto: MintActivityDto) : this(
        dto.id,
        dto.date,
        dto.tokenAddress,
        dto.owner,
        dto.reverted,
        dto.blockchainInfo,
        dto.value,
    )

    override fun toDto() = MintActivityDto(
        id = id,
        date = date,
        reverted = reverted,
        owner = owner,
        tokenAddress = mint,
        value = value,
        blockchainInfo = blockchainInfo
    )
}

data class BurnActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val reverted: Boolean,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val value: BigInteger,
    override val type: ActivityTypeDto = ActivityTypeDto.BURN,
) : ActivityRecord {

    constructor(dto: BurnActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.tokenAddress,
        owner = dto.owner,
        reverted = dto.reverted,
        blockchainInfo = dto.blockchainInfo,
        value = dto.value,
    )

    override fun toDto() = BurnActivityDto(
        id = id,
        date = date,
        reverted = reverted,
        owner = owner,
        tokenAddress = mint,
        value = value,
        blockchainInfo = blockchainInfo
    )
}

data class TransferActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val reverted: Boolean,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val value: BigInteger,
    val from: String,
    val purchase: Boolean,
    override val type: ActivityTypeDto = ActivityTypeDto.TRANSFER,
) : ActivityRecord {

    constructor(dto: TransferActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.tokenAddress,
        owner = dto.owner,
        reverted = dto.reverted,
        blockchainInfo = dto.blockchainInfo,
        value = dto.value,
        from = dto.from,
        purchase = dto.purchase,
    )

    override fun toDto() = TransferActivityDto(
        id = id,
        date = date,
        reverted = reverted,
        from = from,
        owner = owner,
        tokenAddress = mint,
        value = value,
        purchase = purchase,
        blockchainInfo = blockchainInfo
    )
}

data class ListActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val reverted: Boolean,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetDto,
    val take: AssetDto,
    val price: BigDecimal,
    override val type: ActivityTypeDto = ActivityTypeDto.LIST,
) : ActivityRecord {

    constructor(dto: OrderListActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.make.type.getNftMint().orEmpty(),
        owner = dto.maker,
        reverted = dto.reverted,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
        price = dto.price,
    )

    override fun toDto() = OrderListActivityDto(
        id = id,
        date = date,
        reverted = reverted,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        price = price,
        blockchainInfo = blockchainInfo
    )
}

data class BidActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val reverted: Boolean,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetDto,
    val take: AssetDto,
    val price: BigDecimal,
    override val type: ActivityTypeDto = ActivityTypeDto.BID,
) : ActivityRecord {

    constructor(dto: OrderBidActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.take.type.getNftMint().orEmpty(),
        owner = dto.maker,
        reverted = dto.reverted,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
        price = dto.price,
    )

    override fun toDto() = OrderBidActivityDto(
        id = id,
        date = date,
        reverted = reverted,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        price = price,
        blockchainInfo = blockchainInfo
    )
}

data class CancelListActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val reverted: Boolean,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetTypeDto,
    val take: AssetTypeDto,
    override val type: ActivityTypeDto = ActivityTypeDto.CANCEL_LIST,
) : ActivityRecord {

    constructor(dto: OrderCancelListActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.make.getNftMint().orEmpty(),
        owner = dto.maker,
        reverted = dto.reverted,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
    )

    override fun toDto() = OrderCancelListActivityDto(
        id = id,
        date = date,
        reverted = reverted,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        blockchainInfo = blockchainInfo
    )
}

data class CancelBidActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val reverted: Boolean,
    override val blockchainInfo: ActivityBlockchainInfoDto,
    val hash: String,
    val make: AssetTypeDto,
    val take: AssetTypeDto,
    override val type: ActivityTypeDto = ActivityTypeDto.CANCEL_BID,
) : ActivityRecord {

    constructor(dto: OrderCancelBidActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.take.getNftMint().orEmpty(),
        owner = dto.maker,
        reverted = dto.reverted,
        blockchainInfo = dto.blockchainInfo,
        hash = dto.hash,
        make = dto.make,
        take = dto.take,
    )

    override fun toDto() = OrderCancelBidActivityDto(
        id = id,
        date = date,
        reverted = reverted,
        hash = hash,
        maker = owner,
        make = make,
        take = take,
        blockchainInfo = blockchainInfo
    )
}

data class MatchActivityRecord(
    override val id: String,
    override val date: Instant,
    override val mint: String,
    override val owner: String,
    override val reverted: Boolean,
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
) : ActivityRecord {

    constructor(dto: OrderMatchActivityDto) : this(
        id = dto.id,
        date = dto.date,
        mint = dto.nft.type.getNftMint().orEmpty(),
        owner = dto.seller,
        reverted = dto.reverted,
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
        reverted = reverted,
        nft = nft,
        payment = payment,
        buyer = buyer,
        seller = seller,
        buyerOrderHash = buyerOrderHash,
        sellerOrderHash = sellerOrderHash,
        price = price,
        blockchainInfo = blockchainInfo,
        type = orderType,
    )
}
