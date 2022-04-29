package com.rarible.protocol.solana.nft.api.controller

import com.rarible.core.common.nowMillis
import com.rarible.protocol.solana.common.converter.BalanceWithMetaConverter
import com.rarible.protocol.solana.common.model.BalanceWithMeta
import com.rarible.protocol.solana.common.model.MetaplexMetaFields
import com.rarible.protocol.solana.common.model.MetaplexOffChainMetaFields
import com.rarible.protocol.solana.common.pubkey.ProgramDerivedAddressCalc
import com.rarible.protocol.solana.common.pubkey.PublicKey
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.dto.BalancesDto
import com.rarible.protocol.solana.nft.api.test.AbstractControllerTest
import com.rarible.protocol.solana.test.createRandomBalance
import com.rarible.protocol.solana.test.randomAccount
import com.rarible.protocol.solana.test.randomMint
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

class BalanceControllerFt : AbstractControllerTest() {

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Test
    fun `find balance by mint and owner`() = runBlocking<Unit> {
        val balanceWithMeta = saveRandomBalanceWithMeta()

        val expected = BalanceWithMetaConverter.convert(balanceWithMeta)

        val result = balanceControllerApi.getBalanceByMintAndOwner(
            balanceWithMeta.balance.mint,
            balanceWithMeta.balance.owner
        ).awaitFirst()

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `find balances by mint and owner`() = runBlocking<Unit> {
        val owner = randomAccount()
        val mint = randomMint()
        val balanceWithMeta0 = saveRandomBalanceWithMeta(
            owner = owner,
            mint = mint,
            account = ProgramDerivedAddressCalc.getAssociatedTokenAccount(
                mint = PublicKey(mint),
                owner = PublicKey(owner)
            ).address.toBase58()
        )
        val balanceWithMeta1 = saveRandomBalanceWithMeta(owner = owner, mint = mint)
        val balanceWithMeta2 = saveRandomBalanceWithMeta(owner = owner, mint = mint)
        saveRandomBalanceWithMeta(owner = owner) // Different owner
        saveRandomBalanceWithMeta(mint = mint) // Different mint

        val result = balanceControllerApi.getBalancesByMintAndOwner(mint, owner).awaitFirst()
        val expected = BalancesDto(
            balances = listOf(balanceWithMeta0, balanceWithMeta1, balanceWithMeta2)
                .sortedBy { it.balance.account }
                .map { BalanceWithMetaConverter.convert(it) },
            continuation = null
        )
        assertThat(result).isEqualTo(expected)
        assertThat(result.balances).anyMatch { it.isAssociatedTokenAccount == true }
    }

    @Test
    fun `find balances by owner - first page`() = runBlocking<Unit> {
        val now = nowMillis()
        val owner = randomAccount()

        val balanceWithMeta1 = saveRandomBalanceWithMeta(owner = owner, updatedAt = now)
        val balanceWithMeta2 = saveRandomBalanceWithMeta(owner = owner, updatedAt = now.minusSeconds(1))
        // different owner
        saveRandomBalanceWithMeta()

        val expected = BalancesDto(
            balances = listOf(balanceWithMeta1, balanceWithMeta2).map { BalanceWithMetaConverter.convert(it) },
            continuation = null
        )

        val result = balanceControllerApi.getBalanceByOwner(owner, null, 50).awaitFirst()

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `find balances by owner - second page`() = runBlocking<Unit> {
        val owner = randomAccount()
        val updatedAt = nowMillis()
        val balanceWithMeta = saveRandomBalanceWithMeta(owner = owner, updatedAt = updatedAt)

        val pageBreakUpdatedAt = updatedAt.plusSeconds(1)
        // Same TS as in continuation, should be included by ID filter
        val pageBreakBalance = saveRandomBalanceWithMeta(owner = owner, updatedAt = pageBreakUpdatedAt)

        val continuation = "${pageBreakUpdatedAt.toEpochMilli()}_zzzzzzzz" // zzz... is 'greater' than any other str

        val expected = BalancesDto(
            balances = listOf(pageBreakBalance, balanceWithMeta).map { BalanceWithMetaConverter.convert(it) },
            continuation = "${updatedAt.toEpochMilli()}_${balanceWithMeta.balance.account}"
        )

        val result = balanceControllerApi.getBalanceByOwner(owner, continuation, 2).awaitFirst()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `find balances by mint`() = runBlocking<Unit> {
        val now = nowMillis()
        val mint = randomMint()

        val balanceWithMeta1 = saveRandomBalanceWithMeta(mint = mint, updatedAt = now)
        val balanceWithMeta2 = saveRandomBalanceWithMeta(mint = mint, updatedAt = now.minusSeconds(1))
        // different mint
        saveRandomBalanceWithMeta()

        val expected = BalancesDto(
            balances = listOf(balanceWithMeta1, balanceWithMeta2).map { BalanceWithMetaConverter.convert(it) },
            continuation = null
        )

        assertThat(balanceControllerApi.getBalanceByMint(mint, null, 50).awaitFirst())
            .isEqualTo(expected)
    }

    @Test
    fun `find balances by mint - second page`() = runBlocking<Unit> {
        val mint = randomMint()
        val updatedAt = nowMillis()
        val balanceWithMeta = saveRandomBalanceWithMeta(mint = mint, updatedAt = updatedAt)

        // From prev page, should be not included into result
        val prevUpdatedAt = updatedAt.plusSeconds(1)
        val prev = saveRandomBalanceWithMeta(mint = mint, updatedAt = prevUpdatedAt)

        val continuation = "${prevUpdatedAt.toEpochMilli()}_${prev.balance.account}"

        val expected = BalancesDto(
            balances = listOf(BalanceWithMetaConverter.convert(balanceWithMeta)),
            continuation = "${updatedAt.toEpochMilli()}_${balanceWithMeta.balance.account}"
        )

        assertThat(balanceControllerApi.getBalanceByMint(mint, continuation, 1).awaitFirst())
            .isEqualTo(expected)
    }

    private val defaultCollectionV1 = MetaplexOffChainMetaFields.Collection("name", "family", "123")
    private val defaultCollectionV2 = MetaplexMetaFields.Collection(randomAccount(), true)

    private suspend fun saveRandomBalanceWithMeta(
        owner: String = randomAccount(),
        mint: String = randomMint(),
        account: String = randomAccount(),
        updatedAt: Instant = nowMillis()
    ): BalanceWithMeta {
        val balance = createRandomBalance(
            account = account,
            owner = owner,
            mint = mint,
            updatedAt = updatedAt
        )

        balanceRepository.save(balance)
        val tokenMeta = saveRandomMetaplexOnChainAndOffChainMeta(
            balance.mint,
            metaplexOffChainMetaCustomizer = {
                this.copy(metaFields = this.metaFields.copy(collection = defaultCollectionV1))
            },
            metaplexMetaCustomizer = {
                this.copy(metaFields = this.metaFields.copy(collection = defaultCollectionV2))
            }
        )
        return BalanceWithMeta(balance, tokenMeta)
    }
}
