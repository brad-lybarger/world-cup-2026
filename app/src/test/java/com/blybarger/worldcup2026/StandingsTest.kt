package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.MatchStatus
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team
import com.blybarger.worldcup2026.tournament.GroupStandings
import org.junit.Assert.assertEquals
import org.junit.Test

class StandingsTest {

    private var idSeq = 0L
    private fun team(id: String, rank: Int = 50) = Team(id, id, rank)

    /** A finished match with explicit goals. */
    private fun played(home: Team, away: Team, hg: Int, ag: Int) = Match(
        id = idSeq++,
        stage = Stage.GROUP_STAGE,
        group = "X",
        homeTeam = home,
        awayTeam = away,
        homeGoals = hg,
        awayGoals = ag,
        status = MatchStatus.FINISHED,
        winner = when { hg > ag -> home; hg < ag -> away; else -> null },
    )

    /** A simulated match: a winner but no goals. */
    private fun simulated(home: Team, away: Team, winner: Team) = Match(
        id = idSeq++,
        stage = Stage.GROUP_STAGE,
        group = "X",
        homeTeam = home,
        awayTeam = away,
        status = MatchStatus.FINISHED,
        winner = winner,
    )

    private fun order(standings: List<com.blybarger.worldcup2026.tournament.TeamRecord>) =
        standings.map { it.team.id }

    @Test
    fun `orders by points then goal difference then goals for`() {
        val a = team("A"); val b = team("B"); val c = team("C"); val d = team("D")
        val matches = listOf(
            played(a, b, 1, 0), played(a, c, 1, 0), played(a, d, 1, 0), // A: 9
            played(b, c, 1, 0), played(b, d, 1, 0),                      // B: 6
            played(c, d, 1, 0),                                          // C: 3, D: 0
        )
        assertEquals(listOf("A", "B", "C", "D"), order(GroupStandings.compute(matches)))
    }

    @Test
    fun `head-to-head breaks a tie and outranks FIFA ranking`() {
        // A and B finish equal on points(3)/GD(-1)/GF(1); A won the head-to-head.
        // B has the better FIFA rank, so if H2H were ignored B would wrongly rank above A.
        val a = team("A", rank = 10)
        val b = team("B", rank = 5)
        val c = team("C", rank = 99)
        val d = team("D", rank = 1)
        val matches = listOf(
            played(a, b, 1, 0),  // A beats B (head-to-head)
            played(c, a, 1, 0),  // A loses to C
            played(d, a, 1, 0),  // A loses to D
            played(b, c, 1, 0),  // B beats C
            played(d, b, 1, 0),  // B loses to D
            played(d, c, 2, 0),  // D dominates; C trails on GD
        )
        // D first (9 pts). Then A and B tie on primary; H2H puts A above B. C last (GD -2).
        assertEquals(listOf("D", "A", "B", "C"), order(GroupStandings.compute(matches)))
    }

    @Test
    fun `a perfect cycle falls back to FIFA ranking`() {
        val x = team("X", rank = 3)
        val y = team("Y", rank = 1)
        val z = team("Z", rank = 2)
        val matches = listOf(
            played(x, y, 1, 0),
            played(y, z, 1, 0),
            played(z, x, 1, 0),
        )
        // All equal on points/GD/GF and head-to-head is a cycle -> FIFA rank decides (Y,Z,X).
        assertEquals(listOf("Y", "Z", "X"), order(GroupStandings.compute(matches)))
    }

    @Test
    fun `simulated win counts as one-nil`() {
        val a = team("A"); val b = team("B")
        val standings = GroupStandings.compute(listOf(simulated(a, b, winner = a)))
        val recA = standings.first { it.team.id == "A" }
        assertEquals(3, recA.points)
        assertEquals(1, recA.goalsFor)
        assertEquals(0, recA.goalsAgainst)
        assertEquals("A", standings.first().team.id)
    }
}
