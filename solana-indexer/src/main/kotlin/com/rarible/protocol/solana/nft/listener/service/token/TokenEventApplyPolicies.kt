package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import org.springframework.stereotype.Component

@Component
class TokenConfirmEventApplyPolicy(properties: NftIndexerProperties) :
    ConfirmEventApplyPolicy<TokenEvent>(properties.confirmationBlocks)

@Component
class TokenRevertEventApplyPolicy :
    RevertEventApplyPolicy<TokenEvent>()
