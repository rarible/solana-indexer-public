package com.rarible.protocol.solana.util

import com.rarible.protocol.solana.common.util.RoyaltyDistributor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RoyaltyDistributorTest {

    @Test
    fun `distribute fully divided`() {
        val creators = mapOf(
            "a" to 10,
            "b" to 20,
            "c" to 70,
            "d" to 0
        )
        val sellerFeeBasisPoints = 1000
        val distributed = RoyaltyDistributor.distribute(sellerFeeBasisPoints, creators)

        assertThat(distributed["a"]).isEqualTo(100)
        assertThat(distributed["b"]).isEqualTo(200)
        assertThat(distributed["c"]).isEqualTo(700)
        assertThat(distributed["d"]).isNull()
    }

    @Test
    fun `distribute rounded`() {
        val creators = mapOf(
            "a" to 59,
            "b" to 20,
            "c" to 21
        )
        val sellerFeeBasisPoints = 420
        val distributed = RoyaltyDistributor.distribute(sellerFeeBasisPoints, creators)

        assertThat(distributed["a"]).isEqualTo(248)
        assertThat(distributed["b"]).isEqualTo(84)
        assertThat(distributed["c"]).isEqualTo(88)
    }

}