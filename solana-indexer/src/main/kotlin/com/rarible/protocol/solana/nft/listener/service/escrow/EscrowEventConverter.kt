package com.rarible.protocol.solana.nft.listener.service.escrow

import com.rarible.protocol.solana.common.event.EscrowBuyEvent
import com.rarible.protocol.solana.common.event.EscrowDepositEvent
import com.rarible.protocol.solana.common.event.EscrowEvent
import com.rarible.protocol.solana.common.event.EscrowExecuteSaleEvent
import com.rarible.protocol.solana.common.event.EscrowWithdrawEvent
import com.rarible.protocol.solana.common.records.SolanaEscrowRecord
import org.springframework.stereotype.Component

@Component
class EscrowEventConverter {
    suspend fun convert(
        record: SolanaEscrowRecord,
        reversed: Boolean
    ): List<EscrowEvent> = when (record) {
        is SolanaEscrowRecord.WithdrawRecord -> listOf(
            EscrowWithdrawEvent(
                account = record.escrow,
                auctionHouse = record.auctionHouse,
                wallet = record.wallet,
                amount = record.amount,
                timestamp = record.timestamp,
                log = record.log,
                reversed = reversed
            )
        )
        is SolanaEscrowRecord.DepositRecord -> listOf(
            EscrowDepositEvent(
                account = record.escrow,
                auctionHouse = record.auctionHouse,
                wallet = record.wallet,
                amount = record.amount,
                timestamp = record.timestamp,
                log = record.log,
                reversed = reversed
            )
        )
        is SolanaEscrowRecord.BuyRecord -> listOf(
            EscrowBuyEvent(
                account = record.escrow,
                auctionHouse = record.auctionHouse,
                wallet = record.wallet,
                amount = record.buyPrice,
                timestamp = record.timestamp,
                log = record.log,
                reversed = reversed
            )
        )
        is SolanaEscrowRecord.ExecuteSaleRecord -> listOf(
                EscrowExecuteSaleEvent(
                    mint = record.mint,
                    account = record.escrow,
                    auctionHouse = record.auctionHouse,
                    wallet = record.wallet,
                    amount = record.buyPrice,
                    timestamp = record.timestamp,
                    log = record.log,
                    reversed = reversed
                )
            )
    }
}
