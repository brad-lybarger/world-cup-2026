package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.data.FifaRankings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FifaRankingsTest {

    @Test
    fun `seed covers all 48 World Cup nations`() {
        assertEquals(48, FifaRankings.byTla.size)
    }

    @Test
    fun `no two teams share a ranking`() {
        val ranks = FifaRankings.byTla.values
        assertEquals(ranks.size, ranks.toSet().size)
    }

    @Test
    fun `lookup resolves known teams and falls back for unknown`() {
        assertEquals(1, FifaRankings.rankFor("ARG"))
        assertEquals(11, FifaRankings.rankFor("MEX"))
        assertEquals(FifaRankings.UNRANKED, FifaRankings.rankFor("XYZ"))
        assertEquals(FifaRankings.UNRANKED, FifaRankings.rankFor(null))
    }

    @Test
    fun `every rank is positive and hosts are present`() {
        assertTrue(FifaRankings.byTla.values.all { it > 0 })
        // 2026 hosts: USA, Mexico, Canada
        for (tla in listOf("USA", "MEX", "CAN")) {
            assertTrue("missing host $tla", FifaRankings.byTla.containsKey(tla))
        }
    }
}
