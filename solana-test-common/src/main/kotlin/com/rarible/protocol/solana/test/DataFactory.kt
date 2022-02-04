package com.rarible.protocol.solana.test

import com.rarible.core.test.data.randomBoolean
import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.meta.SolanaMeta
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

fun createRandomTokenMeta(): SolanaMeta = SolanaMeta(
    name = randomString(),
    description = randomString(),
    url = randomUrl()
)

fun randomUrl(): String = "https://test.com/" + randomString()
