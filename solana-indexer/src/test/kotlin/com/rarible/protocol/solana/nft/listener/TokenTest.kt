package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.nft.listener.service.balance.BalanceRepository
import com.rarible.protocol.solana.nft.listener.service.token.ItemRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.UUID
import kotlin.math.pow

class TokenTest : AbstractBlockScannerTest() {
    private val timeout = Duration.ofSeconds(5)

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @Autowired
    private lateinit var balanceRepository: BalanceRepository

    @Test
    fun `Item supply should be changed`() = runBlocking {
        val decimals = 3
        val aliceWallet = createWallet("${UUID.randomUUID()}")
        val token = createToken(decimals)
        val account = createAccount(token)
        val aliceAccount = createAccount(token, aliceWallet)

        mintToken(token, amount = 5UL)
        checkItem(token, supply = 5, decimals)
        checkBalance(account, value = 5, decimals)

        burnToken(account, amount = 4UL)
        checkItem(token, supply = 1, decimals)
        checkBalance(account, value = 1, decimals)

        transferToken(token, amount = 1UL, aliceAccount)
        checkItem(token, supply = 1, decimals)
        checkBalance(account, value = 0, decimals)
        checkBalance(aliceAccount, value = 1, decimals)
    }

    private suspend fun checkItem(token: String, supply: Long, decimals: Int = 0) {
        Wait.waitAssert(timeout) {
            val item = itemRepository.findById(token).block()!!

            assertEquals(token, item.token)
            assertEquals(supply * 10.0.pow(decimals.toDouble()).toLong(), item.supply)
        }
    }

    private suspend fun checkBalance(account: String, value: Long, decimals: Int = 0) {
        Wait.waitAssert(timeout) {
            val balance = balanceRepository.findById(account).block()!!

            assertEquals(value * 10.0.pow(decimals.toDouble()).toLong(), balance.value)
        }
    }
}