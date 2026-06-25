package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.MatchStatus
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team
import com.blybarger.worldcup2026.simulator.MatchSimulator
import com.blybarger.worldcup2026.tournament.BracketStructure
import com.blybarger.worldcup2026.tournament.TournamentSimulator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentSimulatorTest {

    private var idSeq = 0L
    private fun team(id: String, rank: Int) = Team(id, id, rank)

    private fun played(group: String, home: Team, away: Team) = Match(
        id = idSeq++,
        stage = Stage.GROUP_STAGE,
        group = group,
        homeTeam = home,
        awayTeam = away,
        homeGoals = 1,
        awayGoals = 0,
        status = MatchStatus.FINISHED,
        winner = home,
    )

    /** Group with a clean 1>2>3>4 finish; thirdRank controls the third's FIFA rank. */
    private fun group(letter: String, thirdRank: Int): List<Match> {
        val t1 = team("${letter}1", 100 + thirdRank)
        val t2 = team("${letter}2", 200 + thirdRank)
        val t3 = team("${letter}3", thirdRank)
        val t4 = team("${letter}4", 300 + thirdRank)
        return listOf(
            played(letter, t1, t2), played(letter, t1, t3), played(letter, t1, t4),
            played(letter, t2, t3), played(letter, t2, t4),
            played(letter, t3, t4),
        )
    }

    private fun fullGroupStage(): List<Match> =
        ('A'..'L').flatMapIndexed { i, c -> group(c.toString(), i + 1) }

    @Test
    fun `produces a complete 32-match knockout bracket`() {
        val result = TournamentSimulator(MatchSimulator(Random(1))).simulate(fullGroupStage())

        assertEquals(32, result.knockoutMatches.size)
        val byStage = result.knockoutMatches.groupingBy { it.stage }.eachCount()
        assertEquals(16, byStage[Stage.ROUND_OF_32])
        assertEquals(8, byStage[Stage.ROUND_OF_16])
        assertEquals(4, byStage[Stage.QUARTERFINAL])
        assertEquals(2, byStage[Stage.SEMIFINAL])
        assertEquals(1, byStage[Stage.THIRD_PLACE])
        assertEquals(1, byStage[Stage.FINAL])
        // Every knockout match resolved to two real teams and a winner.
        assertTrue(result.knockoutMatches.all { it.isReady && it.winner != null })
    }

    @Test
    fun `champion is one of the qualified teams`() {
        val result = TournamentSimulator(MatchSimulator(Random(7))).simulate(fullGroupStage())

        val qualified = buildSet {
            result.qualification.groups.values.forEach { add(it.winner.id); add(it.runnerUp.id) }
            result.qualification.thirdByGroup.values.forEach { add(it.id) }
        }
        assertEquals(32, qualified.size)
        assertTrue(result.champion.id in qualified)
    }

    @Test
    fun `simulation is reproducible with the same seed`() {
        val a = TournamentSimulator(MatchSimulator(Random(42))).simulate(fullGroupStage())
        val b = TournamentSimulator(MatchSimulator(Random(42))).simulate(fullGroupStage())
        assertEquals(a.champion.id, b.champion.id)
        assertEquals(
            a.knockout(BracketStructure.FINAL_MATCH).homeTeam?.id,
            b.knockout(BracketStructure.FINAL_MATCH).homeTeam?.id,
        )
    }

    @Test
    fun `round of 32 winner-vs-third matches use the allocation table`() {
        val result = TournamentSimulator(MatchSimulator(Random(3))).simulate(fullGroupStage())
        // With thirds ranked A..L = 1..12, the 8 best are groups A..H -> key ABCDEFGH.
        assertEquals("ABCDEFGH", result.qualification.thirdPlaceKey)
        // Match 79 = Winner A vs third facing A. For key ABCDEFGH, slot A -> 3H, i.e. team "H3".
        val m79 = result.knockout(79)
        val ids = setOfNotNull(m79.homeTeam?.id, m79.awayTeam?.id)
        assertTrue("expected A1 and H3 in match 79, got $ids", "A1" in ids && "H3" in ids)
    }
}
