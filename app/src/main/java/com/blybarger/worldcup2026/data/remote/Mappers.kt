package com.blybarger.worldcup2026.data.remote

import com.blybarger.worldcup2026.data.FifaRankings
import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.MatchStatus
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team

/** Maps football-data.org's stage strings to our [Stage]. Unknown stages fall back to GROUP_STAGE. */
internal fun stageOf(apiStage: String?): Stage = when (apiStage) {
    "LAST_32" -> Stage.ROUND_OF_32
    "LAST_16" -> Stage.ROUND_OF_16
    "QUARTER_FINALS" -> Stage.QUARTERFINAL
    "SEMI_FINALS" -> Stage.SEMIFINAL
    "THIRD_PLACE" -> Stage.THIRD_PLACE
    "FINAL" -> Stage.FINAL
    else -> Stage.GROUP_STAGE
}

/** Maps the API status to our [MatchStatus]. */
internal fun statusOf(apiStatus: String?): MatchStatus = when (apiStatus) {
    "FINISHED", "AWARDED" -> MatchStatus.FINISHED
    "IN_PLAY", "PAUSED", "LIVE" -> MatchStatus.LIVE
    else -> MatchStatus.SCHEDULED // TIMED, SCHEDULED, POSTPONED, …
}

/** A team DTO becomes a domain [Team] only if it's resolved (has a TLA); otherwise null. */
internal fun TeamDto.toDomainOrNull(): Team? {
    val code = tla ?: return null
    return Team(
        id = code,
        name = name ?: shortName ?: code,
        fifaRank = FifaRankings.rankFor(code),
    )
}

/** Maps a match DTO to the domain model, resolving teams, status, score, and winner. */
fun MatchDto.toDomain(): Match {
    val home = homeTeam?.toDomainOrNull()
    val away = awayTeam?.toDomainOrNull()
    val winner = when (score?.winner) {
        "HOME_TEAM" -> home
        "AWAY_TEAM" -> away
        else -> null // DRAW or not yet decided
    }
    return Match(
        id = id,
        stage = stageOf(stage),
        group = group?.removePrefix("GROUP_"), // API uses "GROUP_A"; we key brackets by "A"
        homeTeam = home,
        awayTeam = away,
        homeGoals = score?.fullTime?.home,
        awayGoals = score?.fullTime?.away,
        status = statusOf(status),
        winner = winner,
    )
}
