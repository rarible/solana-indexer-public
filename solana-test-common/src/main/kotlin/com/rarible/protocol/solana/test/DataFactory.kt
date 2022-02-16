package com.rarible.protocol.solana.test

import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomBoolean
import com.rarible.core.test.data.randomInt
import com.rarible.core.test.data.randomLong
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.meta.TokenMetadata
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.MetaplexMetaData
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator

fun createRandomToken(): Token = Token(
    mint = randomString(),
    supply = randomLong(),
    revertableEvents = emptyList(),
    isDeleted = false,
    createdAt = nowMillis(),
    updatedAt = nowMillis(),
)

fun createRandomTokenMetadata(): TokenMetadata =
    TokenMetadata(
        name = randomString(),
        description = randomString(),
        symbol = randomString(),
        url = randomUrl(),
        creators = listOf(createRandomTokenCreator()),
        collection = createRandomTokenMetadataCollection()
    )

fun createRandomMetaplexTokenMeta(): MetaplexMetaData =
    MetaplexMetaData(
        name = randomString(),
        symbol = randomString(),
        uri = randomUrl(),
        sellerFeeBasisPoints = randomInt(100),
        creators = listOf(createRandomTokenCreator()),
        collection = createRandomMetaplexTokenCollection(),
        mutable = randomBoolean()
    )

fun createRandomTokenMetadataCollection(): TokenMetadata.Collection =
    if (randomBoolean()) {
        TokenMetadata.Collection.OnChain(
            address = randomString(),
            verified = randomBoolean()
        )
    } else {
        TokenMetadata.Collection.OffChain(
            name = randomString(),
            family = randomString(),
            hash = randomString()
        )
    }

fun createRandomMetaplexTokenCollection(): MetaplexMetaData.Collection =
    MetaplexMetaData.Collection(
        address = randomString(),
        verified = randomBoolean()
    )

fun createRandomTokenCreator(): MetaplexTokenCreator =
    MetaplexTokenCreator(
        address = randomString(),
        share = randomInt(101)
    )

fun createRandomBalance(): Balance = Balance(
    account = randomString(),
    value = randomLong(),
    revertableEvents = emptyList(),
    createdAt = nowMillis(),
    updatedAt = nowMillis()
)

fun randomUrl(): String = "https://test.com/" + randomString()
