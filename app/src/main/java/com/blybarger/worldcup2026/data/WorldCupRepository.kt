package com.blybarger.worldcup2026.data

import com.blybarger.worldcup2026.data.remote.FootballDataApi
import com.blybarger.worldcup2026.data.remote.NetworkModule
import com.blybarger.worldcup2026.data.remote.toDomain
import com.blybarger.worldcup2026.domain.Match

/** Fetches World Cup data and maps it into domain models. */
class WorldCupRepository(
    private val api: FootballDataApi = NetworkModule.createApi(),
) {
    /** All World Cup matches (group stage through final), as domain [Match]es. */
    suspend fun getMatches(): List<Match> =
        api.getWorldCupMatches().matches.map { it.toDomain() }
}
