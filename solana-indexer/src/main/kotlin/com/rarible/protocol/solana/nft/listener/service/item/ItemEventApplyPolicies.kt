package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import org.springframework.stereotype.Component

@Component
class ItemConfirmEventApplyPolicy(properties: NftIndexerProperties) :
    ConfirmEventApplyPolicy(properties.confirmationBlocks)

@Component
class ItemRevertEventApplyPolicy :
    RevertEventApplyPolicy()
