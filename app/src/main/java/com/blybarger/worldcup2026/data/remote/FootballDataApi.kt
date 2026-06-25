package com.blybarger.worldcup2026.data.remote

import retrofit2.http.GET

/** Retrofit interface for football-data.org. */
interface FootballDataApi {

    /** All 2026 World Cup matches (group stage through final). */
    @GET("v4/competitions/WC/matches")
    suspend fun getWorldCupMatches(): MatchesResponseDto
}
