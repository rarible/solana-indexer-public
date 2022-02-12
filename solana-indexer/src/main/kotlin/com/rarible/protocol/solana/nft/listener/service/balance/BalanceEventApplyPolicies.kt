package com.rarible.protocol.solana.nft.listener.service.balance

import com.rarible.protocol.solana.common.configuration.SolanaIndexerProperties
import com.rarible.protocol.solana.common.event.BalanceEvent
import com.rarible.protocol.solana.nft.listener.service.token.ConfirmEventApplyPolicy
import com.rarible.protocol.solana.nft.listener.service.token.RevertEventApplyPolicy
import org.springframework.stereotype.Component

@Component
class BalanceConfirmEventApplyPolicy(properties: SolanaIndexerProperties) :
    ConfirmEventApplyPolicy<BalanceEvent>(properties.confirmationBlocks)

@Component
class BalanceRevertEventApplyPolicy :
    RevertEventApplyPolicy<BalanceEvent>()
