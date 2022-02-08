package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.converter.BalanceConverter
import com.rarible.protocol.solana.common.converter.TokenConverter
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.Token
import com.rarible.solana.protocol.dto.BalanceUpdateEventDto
import com.rarible.solana.protocol.dto.TokenDto
import com.rarible.solana.protocol.dto.TokenUpdateEventDto
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class TokenTest : EventAwareBlockScannerTest() {
    @Test
    fun `mint, burn, transfer token`() = runBlocking {
        val decimals = 3
        val aliceWallet = createWallet("${UUID.randomUUID()}")
        val tokenAddress = createToken(decimals)
        val account = createAccount(tokenAddress)
        val aliceAccount = createAccount(tokenAddress, aliceWallet)

        mintToken(tokenAddress, amount = 5UL)
        val token = Token(
            mint = tokenAddress,
            supply = 5.scaleSupply(decimals),
            isDeleted = false,
            revertableEvents = emptyList(),
            createdAt = Instant.EPOCH, // TODO[tests]: consider fetching from the blockchain.
            updatedAt = Instant.EPOCH,
            metaplexMeta = null,
            metaplexMetaHistory = emptyList()
        )
        val fromBalance = Balance(
            account = account,
            value = 5.scaleSupply(decimals),
            revertableEvents = emptyList()
        )
        val aliceBalance = Balance(
            account = aliceAccount,
            value = 0,
            revertableEvents = emptyList()
        )

        Wait.waitAssert {
            assertToken(token)
            assertUpdateTokenEvent(token)
            assertUpdateBalanceEvent(fromBalance)
            assertBalance(fromBalance)
        }

        burnToken(account, amount = 4UL)
        Wait.waitAssert {
            val partlyBurnedToken = token.copy(supply = 1.scaleSupply(decimals))
            assertToken(partlyBurnedToken)
            assertUpdateTokenEvent(partlyBurnedToken)

            val partlyBurnedBalance = fromBalance.copy(value = 1.scaleSupply(decimals))
            assertBalance(partlyBurnedBalance)
            assertUpdateBalanceEvent(partlyBurnedBalance)
        }

        transferToken(tokenAddress, amount = 1UL, aliceAccount)
        Wait.waitAssert {
            val finalToken = token.copy(supply = 1.scaleSupply(decimals))
            assertToken(finalToken)
            assertUpdateTokenEvent(finalToken)

            val finalFromBalance = fromBalance.copy(value = 0.scaleSupply(decimals))
            assertBalance(finalFromBalance)
            assertUpdateBalanceEvent(finalFromBalance)

            val finalAliceBalance = aliceBalance.copy(value = 1.scaleSupply(decimals))
            assertBalance(finalAliceBalance)
            assertUpdateBalanceEvent(finalAliceBalance)
        }
    }

    private fun assertUpdateTokenEvent(token: Token) {
        assertThat(tokenEvents).anySatisfy { event ->
            assertThat(event).isInstanceOfSatisfying(TokenUpdateEventDto::class.java) {
                assertThat(it.address).isEqualTo(token.mint)
                assertTokenDtoEqual(it.token, TokenConverter.convert(token))
            }
        }
    }

    private fun assertUpdateBalanceEvent(balance: Balance) {
        assertThat(balanceEvents).anySatisfy { event ->
            assertThat(event).isInstanceOfSatisfying(BalanceUpdateEventDto::class.java) {
                assertThat(it.account).isEqualTo(balance.account)
                assertThat(it.balance).isEqualTo(BalanceConverter.convert(balance))
            }
        }
    }

    private suspend fun assertToken(expectedToken: Token) {
        val actualToken = tokenRepository.findById(expectedToken.mint)
        assertTokensEqual(actualToken, expectedToken)
    }

    private fun assertTokenDtoEqual(actualToken: TokenDto?, expectedToken: TokenDto) {
        // Ignore some fields because they are minor and hard to get.
        fun TokenDto.ignore() = this
            .copy(createdAt = Instant.EPOCH)
            .copy(updatedAt = Instant.EPOCH)
        assertThat(actualToken?.ignore()).isEqualTo(expectedToken.ignore())
    }

    private fun assertTokensEqual(actualToken: Token?, expectedToken: Token) {
        // Ignore some fields because they are minor and hard to get.
        fun Token.ignore() = this
            .copy(createdAt = Instant.EPOCH)
            .copy(updatedAt = Instant.EPOCH)
            .copy(revertableEvents = emptyList())
        assertThat(actualToken?.ignore()).isEqualTo(expectedToken.ignore())
    }

    private suspend fun assertBalance(balance: Balance) {
        // Ignore [revertableEvents] because they are minor and hard to get.
        assertThat(balanceRepository.findById(balance.account)?.copy(revertableEvents = emptyList()))
            .isEqualTo(balance.copy(revertableEvents = emptyList()))
    }

}
