package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import com.rarible.protocol.solana.nft.listener.service.token.ConfirmEventApplyPolicy
import com.rarible.protocol.solana.nft.listener.service.token.RevertEventApplyPolicy
import org.springframework.stereotype.Component

@Component
class BalanceConfirmEventApplyPolicy(properties: NftIndexerProperties) :
    ConfirmEventApplyPolicy<BalanceEvent>(properties.confirmationBlocks)

@Component
class BalanceRevertEventApplyPolicy :
    RevertEventApplyPolicy<BalanceEvent>()
