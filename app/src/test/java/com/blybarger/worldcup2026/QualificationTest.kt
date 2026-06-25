package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.MatchStatus
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team
import com.blybarger.worldcup2026.tournament.Qualification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QualificationTest {

    private var idSeq = 0L
    private fun team(id: String, rank: Int) = Team(id, id, rank)

    private fun played(group: String, home: Team, away: Team, hg: Int, ag: Int) = Match(
        id = idSeq++,
        stage = Stage.GROUP_STAGE,
        group = group,
        homeTeam = home,
        awayTeam = away,
        homeGoals = hg,
        awayGoals = ag,
        status = MatchStatus.FINISHED,
        winner = if (hg > ag) home else away,
    )

    /**
     * Builds a group with a clean 1>2>3>4 finish (9/6/3/0 pts). [thirdRank] sets the FIFA rank of
     * the third-placed team so third-place selection across groups is deterministic.
     */
    private fun group(letter: String, thirdRank: Int): List<Match> {
        val t1 = team("${letter}1", rank = 100 + thirdRank)
        val t2 = team("${letter}2", rank = 200 + thirdRank)
        val t3 = team("${letter}3", rank = thirdRank) // the third-placed team
        val t4 = team("${letter}4", rank = 300 + thirdRank)
        return listOf(
            played(letter, t1, t2, 1, 0),
            played(letter, t1, t3, 1, 0),
            played(letter, t1, t4, 1, 0),
            played(letter, t2, t3, 1, 0),
            played(letter, t2, t4, 1, 0),
            played(letter, t3, t4, 1, 0),
        )
    }

    @Test
    fun `selects the 8 best third-placed teams by FIFA rank when otherwise tied`() {
        // 12 groups A..L. Each third has identical pts/GD/GF, so the FIFA rank tiebreak decides.
        // Assign third-place FIFA ranks A=1 .. L=12; the 8 best are groups A..H.
        val letters = ('A'..'L').map { it.toString() }
        val matches = letters.flatMapIndexed { i, letter -> group(letter, thirdRank = i + 1) }

        val q = Qualification.compute(matches)

        assertEquals(12, q.groups.size)
        assertEquals(listOf("A", "B", "C", "D", "E", "F", "G", "H"), q.advancingThirdGroups)
        assertEquals("ABCDEFGH", q.thirdPlaceKey)
        // The advancing thirds are exactly each group's third-placed team.
        assertEquals(setOf("A3", "B3", "C3", "D3", "E3", "F3", "G3", "H3"),
            q.thirdByGroup.values.map { it.id }.toSet())
    }

    @Test
    fun `winners and runners-up are the top two of each group`() {
        val matches = ('A'..'L').flatMapIndexed { i, c -> group(c.toString(), i + 1) }
        val q = Qualification.compute(matches)
        for (letter in ('A'..'L').map { it.toString() }) {
            val res = q.groups.getValue(letter)
            assertEquals("${letter}1", res.winner.id)
            assertEquals("${letter}2", res.runnerUp.id)
            assertEquals("${letter}3", res.third.id)
        }
    }

    @Test
    fun `third-place key is always eight sorted letters`() {
        val matches = ('A'..'L').flatMapIndexed { i, c -> group(c.toString(), i + 1) }
        val key = Qualification.compute(matches).thirdPlaceKey
        assertEquals(8, key.length)
        assertTrue(key == key.toCharArray().sorted().joinToString(""))
    }
}
