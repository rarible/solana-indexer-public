package com.rarible.protocol.solana.common.configuration

import com.rarible.protocol.solana.common.service.PackageService
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackageClasses = [PackageService::class])
class SolanaServiceConfiguration
