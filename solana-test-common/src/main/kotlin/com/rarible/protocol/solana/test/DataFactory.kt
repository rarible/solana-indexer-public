package com.rarible.protocol.solana.test

import com.rarible.blockchain.scanner.solana.model.SolanaLog
import com.rarible.core.common.nowMillis
import com.rarible.core.test.data.randomBigInt
import com.rarible.core.test.data.randomBoolean
import com.rarible.core.test.data.randomInt
import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.common.meta.MetaplexOffChainCollectionHash
import com.rarible.protocol.solana.common.meta.TokenMeta
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.common.model.MetaplexMeta
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMeta
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.model.MetaplexTokenCreator
import com.rarible.protocol.solana.common.model.Token
import com.rarible.protocol.solana.common.model.TokenWithMeta

fun createRandomToken(): Token = Token(
    mint = randomString(),
    supply = randomBigInt(),
    revertableEvents = emptyList(),
    isDeleted = false,
    createdAt = nowMillis(),
    updatedAt = nowMillis(),
)

fun createRandomTokenWithMeta(): TokenWithMeta = TokenWithMeta(
    token = createRandomToken(),
    tokenMeta = createRandomTokenMeta()
)

fun createRandomMetaplexMeta(): MetaplexMeta = MetaplexMeta(
    metaAddress = randomString(),
    tokenAddress = randomString(),
    metaFields = createRandomMetaplexMetaFields(),
    isMutable = randomBoolean(),
    revertableEvents = emptyList(),
    updatedAt = nowMillis(),
)

fun createRandomMetaplexOffChainMeta(): MetaplexOffChainMeta = MetaplexOffChainMeta(
    tokenAddress = randomString(),
    metaFields = createRandomMetaplexOffChainMetaFields(),
    loadedAt = nowMillis()
)

fun createRandomMetaplexMetaFields() = MetaplexMetaFields(
    name = randomString(),
    symbol = randomString(),
    uri = randomUrl(),
    sellerFeeBasisPoints = 100,
    creators = listOf(createRandomTokenCreator()),
    collection = createRandomMetaplexMetaFieldsCollection()
)

fun createRandomMetaplexOffChainMetaFields(): MetaplexOffChainMetaFields {
    val collectionName = randomString()
    val collectionFamily = randomString()
    val creators = listOf(MetaplexTokenCreator(randomString(), 100))
    return MetaplexOffChainMetaFields(
        name = randomString(),
        symbol = randomString(),
        description = randomString(),
        collection = MetaplexOffChainMetaFields.Collection(
            name = collectionName,
            family = collectionFamily,
            hash = MetaplexOffChainCollectionHash.calculateCollectionHash(
                name = collectionName,
                family = collectionFamily,
                creators = creators.map { it.address }
            )
        ),
        sellerFeeBasisPoints = randomInt(),
        externalUrl = randomUrl(),
        edition = randomString(),
        backgroundColor = randomString(),
        attributes = listOf(
            MetaplexOffChainMetaFields.Attribute(
                traitType = randomString(),
                value = randomString()
            )
        ),
        properties = MetaplexOffChainMetaFields.Properties(
            category = randomString(),
            creators = creators,
            files = listOf(
                MetaplexOffChainMetaFields.Properties.File(
                    uri = randomString(),
                    type = randomString()
                )
            )
        ),
        image = randomString(),
        animationUrl = randomString()
    )
}

fun createRandomTokenMeta(): TokenMeta =
    TokenMeta(
        name = randomString(),
        description = randomString(),
        symbol = randomString(),
        url = randomUrl(),
        creators = listOf(createRandomTokenCreator()),
        collection = createRandomTokenMetadataCollection(),
        attributes = listOf(createRandomTokenMetaAttribute()),
        contents = listOf(createRandomTokenMetaContent()),
        externalUrl = randomUrl()
    )

fun createRandomTokenMetaContent(): TokenMeta.Content =
    if (randomBoolean()) {
        TokenMeta.Content.ImageContent(
            url = randomUrl(),
            mimeType = null
        )
    } else {
        TokenMeta.Content.VideoContent(
            url = randomUrl(),
            mimeType = null
        )
    }

fun createRandomTokenMetaAttribute(): TokenMeta.Attribute =
    TokenMeta.Attribute(
        key = randomString(),
        value = randomString(),
        type = null,
        format = null
    )

fun createRandomTokenMetadataCollection(): TokenMeta.Collection =
    if (randomBoolean()) {
        TokenMeta.Collection.OnChain(
            address = randomString(),
            verified = randomBoolean()
        )
    } else {
        TokenMeta.Collection.OffChain(
            name = randomString(),
            family = randomString(),
            hash = randomString()
        )
    }

fun createRandomMetaplexMetaFieldsCollection(): MetaplexMetaFields.Collection =
    MetaplexMetaFields.Collection(
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
    owner = randomString(),
    mint = randomString(),
    value = randomBigInt(),
    revertableEvents = emptyList(),
    createdAt = nowMillis(),
    updatedAt = nowMillis()
)

fun createRandomBalanceWithMeta(): BalanceWithMeta = BalanceWithMeta(
    balance = createRandomBalance(),
    tokenMeta = createRandomTokenMeta()
)

fun randomUrl(): String = "https://test.com/" + randomString()

/**
 * [SolanaLog] used in tests as a placeholder in places where the log is not necessary to compare.
 */
val ANY_SOLANA_LOG = SolanaLog(
    blockNumber = 0L,
    transactionHash = "",
    blockHash = "",
    instructionIndex = 0,
    innerInstructionIndex = null
)
