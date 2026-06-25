package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.data.remote.MatchDto
import com.blybarger.worldcup2026.data.remote.ScoreDto
import com.blybarger.worldcup2026.data.remote.ScoreLineDto
import com.blybarger.worldcup2026.data.remote.TeamDto
import com.blybarger.worldcup2026.data.remote.toDomain
import com.blybarger.worldcup2026.domain.MatchStatus
import com.blybarger.worldcup2026.domain.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MappersTest {

    private fun team(tla: String?, name: String? = null) =
        TeamDto(id = 1, name = name ?: tla, shortName = name ?: tla, tla = tla, crest = null)

    @Test
    fun `finished group match maps teams, goals, winner and FIFA ranks`() {
        val dto = MatchDto(
            id = 537327,
            status = "FINISHED",
            stage = "GROUP_STAGE",
            group = "GROUP_A",
            homeTeam = team("MEX", "Mexico"),
            awayTeam = team("RSA", "South Africa"),
            score = ScoreDto(winner = "HOME_TEAM", fullTime = ScoreLineDto(2, 0)),
        )
        val m = dto.toDomain()

        assertEquals(Stage.GROUP_STAGE, m.stage)
        assertEquals(MatchStatus.FINISHED, m.status)
        assertEquals("MEX", m.homeTeam?.id)
        assertEquals(11, m.homeTeam?.fifaRank) // Mexico
        assertEquals(61, m.awayTeam?.fifaRank) // South Africa
        assertEquals(2, m.homeGoals)
        assertEquals(m.homeTeam, m.winner)
        assertTrue(m.isFinished)
    }

    @Test
    fun `draw has no winner`() {
        val dto = MatchDto(
            id = 1,
            status = "FINISHED",
            stage = "GROUP_STAGE",
            homeTeam = team("BRA"),
            awayTeam = team("ESP"),
            score = ScoreDto(winner = "DRAW", fullTime = ScoreLineDto(1, 1)),
        )
        val m = dto.toDomain()
        assertNull(m.winner)
        assertTrue(m.isDraw)
    }

    @Test
    fun `unresolved knockout slot yields null teams and is not ready`() {
        val dto = MatchDto(
            id = 2,
            status = "TIMED",
            stage = "LAST_32",
            homeTeam = team(null),
            awayTeam = team(null),
            score = ScoreDto(winner = null),
        )
        val m = dto.toDomain()
        assertEquals(Stage.ROUND_OF_32, m.stage)
        assertEquals(MatchStatus.SCHEDULED, m.status)
        assertNull(m.homeTeam)
        assertNull(m.awayTeam)
        assertTrue(!m.isReady)
    }

    @Test
    fun `knockout stage strings map correctly`() {
        fun stageFor(s: String) = MatchDto(id = 0, stage = s).toDomain().stage
        assertEquals(Stage.ROUND_OF_16, stageFor("LAST_16"))
        assertEquals(Stage.QUARTERFINAL, stageFor("QUARTER_FINALS"))
        assertEquals(Stage.SEMIFINAL, stageFor("SEMI_FINALS"))
        assertEquals(Stage.FINAL, stageFor("FINAL"))
        assertEquals(Stage.THIRD_PLACE, stageFor("THIRD_PLACE"))
    }
}
