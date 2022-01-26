package com.rarible.protocol.solana.nft.listener.service.token

import com.rarible.protocol.solana.nft.listener.configuration.NftIndexerProperties
import com.rarible.protocol.solana.nft.listener.consumer.SolanaLogRecordEvent
import org.springframework.stereotype.Component

@Component
class TokenConfirmEventApplyPolicy(properties: NftIndexerProperties) :
    ConfirmEventApplyPolicy<SolanaLogRecordEvent>(properties.confirmationBlocks)

@Component
class TokenRevertEventApplyPolicy :
    RevertEventApplyPolicy<SolanaLogRecordEvent>()
