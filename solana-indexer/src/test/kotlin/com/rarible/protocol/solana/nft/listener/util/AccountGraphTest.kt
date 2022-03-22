package com.rarible.protocol.solana.nft.listener.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AccountGraphTest {

    @Test
    fun `empty graph`() {
        val graph = AccountGraph()

        val groups = graph.findGroups()

        assertThat(groups).isEmpty()
    }

    @Test
    fun `single rib`() {
        val graph = AccountGraph()
        graph.addRib("a", "b")

        val expectedGroup = listOf("a", "b")

        val groups = graph.findGroups()

        assertThat(groups).hasSize(expectedGroup.size)
        expectedGroup.forEach {
            assertThat(groups[it]!!.group).containsExactlyElementsOf(expectedGroup)
        }
    }

    @Test
    fun `group with multiple vertices`() {
        val graph = AccountGraph()
        graph.addRib("a", "b")
        graph.addRib("a", "c")
        graph.addRib("b", "d")

        val expectedGroup = listOf("a", "b", "c", "d")

        val groups = graph.findGroups()
        groups["a"]?.mint = "1" // let's check we set mint for all vertices

        assertThat(groups).hasSize(expectedGroup.size)
        expectedGroup.forEach {
            assertThat(groups[it]!!.group).containsExactlyElementsOf(expectedGroup)
            assertThat(groups[it]!!.mint).isEqualTo("1")
        }
    }

    @Test
    fun `several groups`() {
        val graph = AccountGraph()
        graph.addRib("a", "b")
        graph.addRib("b", "c")

        // Just for case
        graph.addRib("e", "e")

        graph.addRib("1", "2")

        val expectedGroup1 = listOf("a", "b", "c")
        val expectedGroup2 = listOf("e")
        val expectedGroup3 = listOf("1", "2")

        val groups = graph.findGroups()

        groups["a"]?.mint = "TEST" // let's check we set mint for all vertices

        assertThat(groups).hasSize(expectedGroup1.size + expectedGroup2.size + expectedGroup3.size)
        expectedGroup1.forEach {
            assertThat(groups[it]!!.group).containsExactlyElementsOf(expectedGroup1)
            assertThat(groups[it]!!.mint).isEqualTo("TEST")
        }
        expectedGroup2.forEach {
            assertThat(groups[it]!!.group).containsExactlyElementsOf(expectedGroup2)
            assertThat(groups[it]!!.mint).isNull()
        }
        expectedGroup3.forEach {
            assertThat(groups[it]!!.group).containsExactlyElementsOf(expectedGroup3)
            assertThat(groups[it]!!.mint).isNull()
        }
    }
}