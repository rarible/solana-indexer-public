package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.common.model.MetaId
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.nft.listener.service.token.RevertedEntityChainReducer
import org.springframework.stereotype.Component

@Component
class ReversedChainMetaplexMetaReducer(
    eventApplyPolicy: MetaplexMetaRevertEventApplyPolicy,
    reversedMetaReducer: ReversedMetaplexMetaReducer,
) : RevertedEntityChainReducer<MetaId, MetaplexMetaEvent, MetaplexMeta>(
    eventApplyPolicy,
    reversedMetaReducer
)
