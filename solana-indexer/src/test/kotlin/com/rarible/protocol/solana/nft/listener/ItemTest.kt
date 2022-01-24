package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.nft.listener.service.ItemRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import kotlin.math.pow

@Disabled
class ItemTest : AbstractBlockScannerTest() {
    private val timeout = Duration.ofSeconds(5)

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @Test
    fun `Item supply should be changed`() = runBlocking {
        val decimals = 3
        val aliceWallet = createWallet("alice")
        val token = createToken(decimals)
        val account = createAccount(token)
        val aliceAccount = createAccount(token, aliceWallet)

        mintToken(token, 5UL)
        checkItem(token, 5 * 10.0.pow(decimals).toLong())

        burnToken(account, 4UL)
        checkItem(token, 1 * 10.0.pow(decimals).toLong())

        transferToken(token, 1UL, aliceAccount)
        checkItem(token, 1 * 10.0.pow(decimals).toLong())
    }

    private suspend fun checkItem(token: String, supply: Long) {
        Wait.waitAssert(timeout) {
            val item = itemRepository.findById(token).block()

            assertNotNull(item)
            assertEquals(token, item.token)
            assertEquals(supply, item.supply)
        }
    }
}