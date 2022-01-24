package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.model.ItemId
import org.springframework.stereotype.Component

@Component
class ForwardChainItemReducer(
    itemConfirmEventApplyPolicy: ItemConfirmEventApplyPolicy,
    forwardValueItemReducer: ForwardValueItemReducer,
) : EntityChainReducer<ItemId, SolanaLogRecordEvent, Item>(
    itemConfirmEventApplyPolicy,
    forwardValueItemReducer
)
