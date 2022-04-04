package com.rarible.protocol.solana.common.util

import kotlin.math.roundToInt

object RoyaltyDistributor {

    /**
     * SellerFeeBasisPoints - value 0..10000 (from 0 to 100%)
     * Creators - map of creator addresses with their part which is present as %.
     * Sum of all creators parts == 100
     */
    fun distribute(sellerFeeBasisPoints: Int, creators: Map<String, Int>): Map<String, Int> {
        val multiplier = sellerFeeBasisPoints.toDouble()
        return creators.mapValues {
            val part = (multiplier * it.value) / 100
            part.roundToInt()
        }.filter { it.value > 0 }
    }

}