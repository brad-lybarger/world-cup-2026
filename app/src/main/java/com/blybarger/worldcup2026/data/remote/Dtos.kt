package com.blybarger.worldcup2026.data.remote

import kotlinx.serialization.Serializable

/**
 * DTOs for football-data.org v4 `GET /competitions/WC/matches`.
 *
 * Only the fields we use are declared; the Json parser is configured to ignore unknown keys, so
 * the many other fields (area, season, odds, referees, …) are simply skipped. Most fields are
 * nullable because knockout slots arrive with null team data until their feeder matches resolve.
 */
@Serializable
data class MatchesResponseDto(
    val resultSet: ResultSetDto? = null,
    val matches: List<MatchDto> = emptyList(),
)

@Serializable
data class ResultSetDto(
    val count: Int? = null,
    val played: Int? = null,
)

@Serializable
data class MatchDto(
    val id: Long,
    val utcDate: String? = null,
    val status: String? = null,
    val stage: String? = null,
    val group: String? = null,
    val homeTeam: TeamDto? = null,
    val awayTeam: TeamDto? = null,
    val score: ScoreDto? = null,
)

@Serializable
data class TeamDto(
    val id: Long? = null,
    val name: String? = null,
    val shortName: String? = null,
    val tla: String? = null,
    val crest: String? = null,
)

@Serializable
data class ScoreDto(
    val winner: String? = null,
    val duration: String? = null,
    val fullTime: ScoreLineDto? = null,
)

@Serializable
data class ScoreLineDto(
    val home: Int? = null,
    val away: Int? = null,
)
