package com.rarible.protocol.solana.repository

import com.rarible.core.test.data.randomString
import com.rarible.protocol.solana.AbstractIntegrationTest
import com.rarible.protocol.solana.common.continuation.IdContinuation
import com.rarible.protocol.solana.common.repository.ActivityRepository
import com.rarible.protocol.solana.dto.ActivityTypeDto
import com.rarible.protocol.solana.dto.SolanaNftAssetTypeDto
import com.rarible.protocol.solana.test.randomAssetNft
import com.rarible.protocol.solana.test.randomBid
import com.rarible.protocol.solana.test.randomBurn
import com.rarible.protocol.solana.test.randomCancelBid
import com.rarible.protocol.solana.test.randomCancelList
import com.rarible.protocol.solana.test.randomList
import com.rarible.protocol.solana.test.randomMint
import com.rarible.protocol.solana.test.randomSell
import com.rarible.protocol.solana.test.randomTransfer
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ActivityRepositoryIt : AbstractIntegrationTest() {

    @Autowired
    lateinit var activityRepository: ActivityRepository

    @Test
    fun findByIds() = runBlocking<Unit> {
        val data = eachActivitiesByOne()
        data.values.forEach { activityRepository.save(it) }

        data.forEach { (type, dto) ->
            val found = activityRepository.findById(dto.id)
            val expected = data[type]!!
            assertNotNull(found)

            // assertEquals on full object doesn't work because internal AssetTypeDto compares by reference
            assertEquals(expected.id, found?.id)
            assertEquals(expected.date, found?.date)
            assertEquals(expected.reverted, found?.reverted)

            assertTrue(activityRepository.removeById(dto.id))
            assertNull(activityRepository.findById(dto.id))
        }
    }

    @Test
    fun findAllActivities() = runBlocking<Unit> {
        val data = eachActivitiesByOne()
        data.values.forEach { activityRepository.save(it) }

        data.forEach { (type, expected) ->
            val result = activityRepository.findAllActivities(listOf(type), null, 50, false).toList()
            assertThat(result).hasSize(1)
            assertThat(result.single()).isInstanceOf(expected::class.java)
        }

        val c1desc = activityRepository.findAllActivities(data.keys, null, 3, false)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        val c2desc = activityRepository.findAllActivities(data.keys, c1desc, 3, false)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        activityRepository.findAllActivities(data.keys, c2desc, 3, false)
            .toList().let { result ->
                assertThat(result).hasSize(2)
            }

        val c1asc = activityRepository.findAllActivities(data.keys, null, 3, true)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        val c2asc = activityRepository.findAllActivities(data.keys, c1asc, 3, true)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        activityRepository.findAllActivities(data.keys, c2asc, 3, true)
            .toList().let { result ->
                assertThat(result).hasSize(2)
            }
    }

    @Test
    fun findActivitiesByItem() = runBlocking<Unit> {
        val mint = randomString()
        val data = eachActivitiesByMint(mint)
        data.values.forEach { activityRepository.save(it) }

        data.forEach { (type, expected) ->
            val result = activityRepository.findActivitiesByMint(listOf(type), mint, null, 50, false).toList()
            assertThat(result).hasSize(1)
            assertThat(result.single()).isInstanceOf(expected::class.java)
        }

        val c1desc = activityRepository.findActivitiesByMint(data.keys, mint, null, 3, false)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        val c2desc = activityRepository.findActivitiesByMint(data.keys, mint, c1desc, 3, false)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        activityRepository.findActivitiesByMint(data.keys, mint, c2desc, 3, false)
            .toList().let { result ->
                assertThat(result).hasSize(2)
            }

        val c1asc = activityRepository.findActivitiesByMint(data.keys, mint, null, 3, true)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        val c2asc = activityRepository.findActivitiesByMint(data.keys, mint, c1asc, 3, true)
            .toList().let { result ->
                assertThat(result).hasSize(3)
                val last = result.last()
                IdContinuation(last.id)
            }
        activityRepository.findActivitiesByMint(data.keys, mint, c2asc, 3, true)
            .toList().let { result ->
                assertThat(result).hasSize(2)
            }

    }

    private fun eachActivitiesByOne() = mapOf(
        ActivityTypeDto.MINT to randomMint(),
        ActivityTypeDto.BURN to randomBurn(),
        ActivityTypeDto.TRANSFER to randomTransfer(),
        ActivityTypeDto.LIST to randomList(),
        ActivityTypeDto.CANCEL_LIST to randomCancelList(),
        ActivityTypeDto.BID to randomBid(),
        ActivityTypeDto.CANCEL_BID to randomCancelBid(),
        ActivityTypeDto.SELL to randomSell(),
    )

    private fun eachActivitiesByMint(mint: String) = mapOf(
        ActivityTypeDto.MINT to randomMint(tokenAddress = mint),
        ActivityTypeDto.BURN to randomBurn(tokenAddress = mint),
        ActivityTypeDto.TRANSFER to randomTransfer(tokenAddress = mint),
        ActivityTypeDto.LIST to randomList(make = randomAssetNft(type = SolanaNftAssetTypeDto(mint = mint))),
        ActivityTypeDto.CANCEL_LIST to randomCancelList(make = SolanaNftAssetTypeDto(mint = mint)),
        ActivityTypeDto.BID to randomBid(take = randomAssetNft(type = SolanaNftAssetTypeDto(mint = mint))),
        ActivityTypeDto.CANCEL_BID to randomCancelBid(take = SolanaNftAssetTypeDto(mint = mint)),
        ActivityTypeDto.SELL to randomSell(nft = randomAssetNft(type = SolanaNftAssetTypeDto(mint = mint))),
    )

}
