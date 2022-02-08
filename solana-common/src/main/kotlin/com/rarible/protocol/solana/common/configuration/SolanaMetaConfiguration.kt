package com.rarible.protocol.solana.common.configuration

import com.rarible.protocol.solana.common.meta.MetaPackage
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [MetaPackage::class])
class SolanaMetaConfiguration
