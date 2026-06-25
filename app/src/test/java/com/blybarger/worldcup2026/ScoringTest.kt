package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.MatchStatus
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team
import com.blybarger.worldcup2026.simulator.MatchSimulator
import com.blybarger.worldcup2026.tournament.BracketPrediction
import com.blybarger.worldcup2026.tournament.BracketScorer
import com.blybarger.worldcup2026.tournament.TournamentResult
import com.blybarger.worldcup2026.tournament.TournamentSimulator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class ScoringTest {

    private var idSeq = 0L
    private fun team(id: String, rank: Int) = Team(id, id, rank)
    private fun played(group: String, home: Team, away: Team) = Match(
        id = idSeq++, stage = Stage.GROUP_STAGE, group = group,
        homeTeam = home, awayTeam = away, homeGoals = 1, awayGoals = 0,
        status = MatchStatus.FINISHED, winner = home,
    )

    private fun group(letter: String, thirdRank: Int): List<Match> {
        val t1 = team("${letter}1", 100 + thirdRank); val t2 = team("${letter}2", 200 + thirdRank)
        val t3 = team("${letter}3", thirdRank); val t4 = team("${letter}4", 300 + thirdRank)
        return listOf(
            played(letter, t1, t2), played(letter, t1, t3), played(letter, t1, t4),
            played(letter, t2, t3), played(letter, t2, t4), played(letter, t3, t4),
        )
    }

    private fun result(seed: Int): TournamentResult {
        val groups = ('A'..'L').flatMapIndexed { i, c -> group(c.toString(), i + 1) }
        return TournamentSimulator(MatchSimulator(Random(seed))).simulate(groups)
    }

    /** Predict the losing team in every scored match — guaranteed all wrong. */
    private fun allWrong(r: TournamentResult) = BracketPrediction(
        r.knockoutMatches.filter { it.stage in BracketScorer.SCORED_STAGES }.associate { m ->
            val loser = if (m.homeTeam?.id == m.winner?.id) m.awayTeam!! else m.homeTeam!!
            m.id.toInt() to loser
        }
    )

    @Test
    fun `perfect bracket scores the maximum 80 points`() {
        val r = result(5)
        val card = BracketScorer.score(BracketScorer.perfectPrediction(r), r.knockoutMatches)
        // 16x1 + 8x2 + 4x4 + 2x8 + 1x16 = 80
        assertEquals(80, card.totalPoints)
        assertEquals(80, card.maxPoints)
        assertEquals(31, card.totalCorrect)
        card.rounds.forEach { assertEquals(16, it.points) } // each round contributes 16
    }

    @Test
    fun `predicting every loser scores zero`() {
        val r = result(5)
        val card = BracketScorer.score(allWrong(r), r.knockoutMatches)
        assertEquals(0, card.totalPoints)
        assertEquals(0, card.totalCorrect)
    }

    @Test
    fun `one wrong Round-of-32 pick costs exactly one point`() {
        val r = result(5)
        val perfect = BracketScorer.perfectPrediction(r).winners.toMutableMap()
        // Flip one R32 pick to the loser.
        val anR32 = r.knockoutMatches.first { it.stage == Stage.ROUND_OF_32 }
        val loser = if (anR32.homeTeam?.id == anR32.winner?.id) anR32.awayTeam!! else anR32.homeTeam!!
        perfect[anR32.id.toInt()] = loser

        val card = BracketScorer.score(BracketPrediction(perfect), r.knockoutMatches)
        assertEquals(79, card.totalPoints)
        assertEquals(30, card.totalCorrect)
        assertEquals(15, card.rounds.first { it.stage == Stage.ROUND_OF_32 }.correct)
    }

    @Test
    fun `points double each round`() {
        val r = result(5)
        val card = BracketScorer.score(BracketScorer.perfectPrediction(r), r.knockoutMatches)
        assertEquals(1, card.rounds.first { it.stage == Stage.ROUND_OF_32 }.pointsEach)
        assertEquals(2, card.rounds.first { it.stage == Stage.ROUND_OF_16 }.pointsEach)
        assertEquals(4, card.rounds.first { it.stage == Stage.QUARTERFINAL }.pointsEach)
        assertEquals(8, card.rounds.first { it.stage == Stage.SEMIFINAL }.pointsEach)
        assertEquals(16, card.rounds.first { it.stage == Stage.FINAL }.pointsEach)
    }
}
