package com.rarible.protocol.solana.nft.api.converter

import com.rarible.protocol.solana.dto.ActivityDto
import kotlinx.coroutines.flow.Flow

interface ActivityConverter<T> {
    fun convert(flow: Flow<T>): Flow<ActivityDto>
}
