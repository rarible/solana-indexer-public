package com.rarible.protocol.solana

import com.rarible.core.test.data.randomBoolean
import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.Token

fun createRandomToken(): Token = Token(
    mint = randomString(),
    collection = if (randomBoolean()) randomString() else null,
    supply = randomLong(),
    revertableEvents = emptyList(),
    isDeleted = false,
    metadataUrl = randomString()
)

fun createRandomBalance(): Balance = Balance(
    account = randomString(),
    value = randomLong(),
    revertableEvents = emptyList()
)
