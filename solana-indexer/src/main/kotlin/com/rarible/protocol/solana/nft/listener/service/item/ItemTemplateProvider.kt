package com.rarible.protocol.solana.nft.listener.service.item

import com.rarible.core.entity.reducer.service.EntityTemplateProvider
import com.rarible.protocol.solana.nft.listener.model.Item
import com.rarible.protocol.solana.nft.listener.model.ItemId
import org.springframework.stereotype.Component

@Component
class ItemTemplateProvider : EntityTemplateProvider<ItemId, Item> {
    override fun getEntityTemplate(id: ItemId): Item {
        return Item.empty(id)
    }
}
