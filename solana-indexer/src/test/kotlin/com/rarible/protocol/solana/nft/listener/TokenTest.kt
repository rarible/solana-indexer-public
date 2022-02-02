package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.repository.BalanceRepository
import com.rarible.protocol.solana.common.repository.TokenRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.UUID
import kotlin.math.pow

@Disabled
class TokenTest : AbstractBlockScannerTest() {
    private val timeout = Duration.ofSeconds(5)

    @Autowired
    private lateinit var tokenRepository: TokenRepository

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Test
    fun `Token supply should be changed`() = runBlocking {
        val decimals = 3
        val aliceWallet = createWallet("${UUID.randomUUID()}")
        val token = createToken(decimals)
        val account = createAccount(token)
        val aliceAccount = createAccount(token, aliceWallet)

        mintToken(token, amount = 5UL)
        checkToken(token, supply = 5, decimals)
        checkBalance(account, value = 5, decimals)

        burnToken(account, amount = 4UL)
        checkToken(token, supply = 1, decimals)
        checkBalance(account, value = 1, decimals)

        transferToken(token, amount = 1UL, aliceAccount)
        checkToken(token, supply = 1, decimals)
        checkBalance(account, value = 0, decimals)
        checkBalance(aliceAccount, value = 1, decimals)
    }

    private suspend fun checkToken(mint: String, supply: Long, decimals: Int = 0) {
        Wait.waitAssert(timeout) {
            val token = tokenRepository.findById(mint)!!

            assertEquals(mint, token.mint)
            assertEquals(supply * 10.0.pow(decimals.toDouble()).toLong(), token.supply)
        }
    }

    private suspend fun checkBalance(account: String, value: Long, decimals: Int = 0) {
        Wait.waitAssert(timeout) {
            val balance = balanceRepository.findById(account)!!

            assertEquals(value * 10.0.pow(decimals.toDouble()).toLong(), balance.value)
        }
    }
}
