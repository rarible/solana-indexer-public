package com.rarible.protocol.solana.common.util

import com.rarible.protocol.solana.dto.ActivityDto
import com.rarible.protocol.solana.dto.BurnActivityDto
import com.rarible.protocol.solana.dto.MintActivityDto
import com.rarible.protocol.solana.dto.OrderBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelBidActivityDto
import com.rarible.protocol.solana.dto.OrderCancelListActivityDto
import com.rarible.protocol.solana.dto.OrderListActivityDto
import com.rarible.protocol.solana.dto.OrderMatchActivityDto
import com.rarible.protocol.solana.dto.TransferActivityDto

fun ActivityDto.getNftMint(): String? {
    return when (this) {
        is MintActivityDto -> this.tokenAddress
        is BurnActivityDto -> this.tokenAddress
        is TransferActivityDto -> this.tokenAddress
        is OrderMatchActivityDto -> this.nft.type.getNftMint()
        is OrderListActivityDto -> this.make.type.getNftMint()
        is OrderBidActivityDto -> this.take.type.getNftMint()
        is OrderCancelListActivityDto -> this.make.getNftMint()
        is OrderCancelBidActivityDto -> this.take.getNftMint()
    }
}