package com.rarible.protocol.solana.nft.listener.service.meta

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.MetaplexMetaEvent
import com.rarible.protocol.solana.nft.listener.service.token.ConfirmEventApplyPolicy
import com.rarible.protocol.solana.nft.listener.service.token.RevertEventApplyPolicy
import org.springframework.stereotype.Component

@Component
class MetaplexMetaEventApplyPolicy(properties: SolanaIndexerProperties) :
    ConfirmEventApplyPolicy<MetaplexMetaEvent>(properties.confirmationBlocks)

@Component
class MetaplexMetaRevertEventApplyPolicy :
    RevertEventApplyPolicy<MetaplexMetaEvent>()