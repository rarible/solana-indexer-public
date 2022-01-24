package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.blockchain.scanner.framework.data.LogRecordEvent
import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import com.rarible.protocol.solana.nft.listener.model.ItemEvent
import org.springframework.stereotype.Component

@Component
class ItemConfirmEventApplyPolicy(properties: NftIndexerProperties) :
    ConfirmEventApplyPolicy<LogRecordEvent<ItemEvent>>(properties.confirmationBlocks)

@Component
class ItemRevertEventApplyPolicy :
    RevertEventApplyPolicy<LogRecordEvent<ItemEvent>>()
