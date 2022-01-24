package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.model.ItemEvent
import com.rarible.protocol.solana.nft.listener.model.ItemId
import org.springframework.stereotype.Component

@Component
class ReversedChainItemReducer(
    itemRevertEventApplyPolicy: ItemRevertEventApplyPolicy,
    reversedValueItemReducer: ReversedValueItemReducer,
) : RevertedEntityChainReducer<ItemId, LogRecordEvent<ItemEvent>, Item>(
    itemRevertEventApplyPolicy,
    reversedValueItemReducer
)