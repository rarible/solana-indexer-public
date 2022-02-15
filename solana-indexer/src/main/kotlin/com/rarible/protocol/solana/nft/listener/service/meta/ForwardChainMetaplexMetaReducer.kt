package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.nft.listener.service.token.EntityChainReducer
import org.springframework.stereotype.Component

@Component
class ForwardChainMetaplexMetaReducer(
    eventApplyPolicy: MetaplexMetaEventApplyPolicy,
    forwardBalanceValueReducer: ForwardMetaplexMetaReducer
) : EntityChainReducer<MetaId, MetaplexMetaEvent, MetaplexMeta>(
    eventApplyPolicy,
    forwardBalanceValueReducer
)
