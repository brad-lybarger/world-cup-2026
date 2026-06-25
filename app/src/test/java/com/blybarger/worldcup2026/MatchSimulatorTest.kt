package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.domain.Team
import com.blybarger.worldcup2026.simulator.MatchSimulator
import com.blybarger.worldcup2026.simulator.winProbability
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchSimulatorTest {

    private val rank1 = Team("a", "Top Team", fifaRank = 1)
    private val rank2 = Team("b", "Second Team", fifaRank = 2)
    private val rank48 = Team("c", "Underdog", fifaRank = 48)

    @Test
    fun `win probability favors the better ranked team`() {
        // rank 1 vs rank 2 -> 2 / (1 + 2)
        assertEquals(0.667, winProbability(rank1, rank2), 0.001)
        // symmetric: probabilities sum to 1
        assertEquals(1.0, winProbability(rank1, rank2) + winProbability(rank2, rank1), 1e-9)
    }

    @Test
    fun `equal ranks are a coin flip`() {
        val t = Team("x", "X", fifaRank = 10)
        assertEquals(0.5, winProbability(t, t.copy(id = "y")), 1e-9)
    }

    @Test
    fun `higher upset factor makes the favorite more dominant`() {
        val low = winProbability(rank1, rank48, upsetFactor = 0.5)
        val high = winProbability(rank1, rank48, upsetFactor = 2.0)
        assertTrue(high > low)
    }

    @Test
    fun `simulation is reproducible with a seeded RNG and favors the favorite over many runs`() {
        val sim = MatchSimulator(Random(42))
        var topWins = 0
        repeat(1000) { if (sim.simulateWinner(rank1, rank48) == rank1) topWins++ }
        // rank 1 vs rank 48 -> ~0.98; allow generous slack but require clear dominance
        assertTrue("favorite should win the vast majority, got $topWins/1000", topWins > 900)

        // Same seed reproduces the same sequence.
        val a = MatchSimulator(Random(7)).simulateWinner(rank1, rank2)
        val b = MatchSimulator(Random(7)).simulateWinner(rank1, rank2)
        assertEquals(a, b)
    }
}
