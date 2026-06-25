package com.blybarger.worldcup2026.data

/**
 * Static FIFA/Coca-Cola Men's World Ranking seed for the 48 nations in the 2026 World Cup.
 *
 * The football-data.org API does not expose FIFA rankings, so we seed them here. Keyed by each
 * team's TLA (three-letter code) as returned by the API — a stable identifier that avoids
 * name-matching issues (e.g. API "Korea Republic"/"South Korea", "Türkiye"/"Turkey").
 *
 * Source: FIFA Men's World Ranking, June 2026 (the 48 qualified nations).
 * Update these numbers when FIFA publishes a new ranking.
 */
object FifaRankings {

    /** TLA -> FIFA world ranking position (1 = best). */
    val byTla: Map<String, Int> = mapOf(
        "ARG" to 1,   // Argentina
        "FRA" to 2,   // France
        "ESP" to 3,   // Spain
        "ENG" to 4,   // England
        "BRA" to 5,   // Brazil
        "MAR" to 6,   // Morocco
        "NED" to 7,   // Netherlands
        "GER" to 8,   // Germany
        "POR" to 9,   // Portugal
        "BEL" to 10,  // Belgium
        "MEX" to 11,  // Mexico
        "COL" to 12,  // Colombia
        "USA" to 13,  // United States
        "CRO" to 15,  // Croatia
        "JPN" to 16,  // Japan
        "SEN" to 17,  // Senegal
        "SUI" to 18,  // Switzerland
        "URY" to 19,  // Uruguay
        "AUT" to 21,  // Austria
        "IRN" to 22,  // Iran
        "KOR" to 23,  // South Korea
        "AUS" to 25,  // Australia
        "EGY" to 26,  // Egypt
        "NOR" to 27,  // Norway
        "CAN" to 28,  // Canada
        "ALG" to 29,  // Algeria
        "ECU" to 30,  // Ecuador
        "CIV" to 31,  // Ivory Coast
        "TUR" to 32,  // Turkey
        "SWE" to 36,  // Sweden
        "PAR" to 37,  // Paraguay
        "PAN" to 40,  // Panama
        "SCO" to 41,  // Scotland
        "COD" to 43,  // Congo DR
        "CZE" to 44,  // Czechia
        "UZB" to 54,  // Uzbekistan
        "QAT" to 57,  // Qatar
        "TUN" to 58,  // Tunisia
        "KSA" to 59,  // Saudi Arabia
        "IRQ" to 60,  // Iraq
        "RSA" to 61,  // South Africa
        "CPV" to 63,  // Cape Verde Islands
        "BIH" to 64,  // Bosnia-Herzegovina
        "GHA" to 65,  // Ghana
        "JOR" to 68,  // Jordan
        "CUW" to 81,  // Curaçao
        "NZL" to 84,  // New Zealand
        "HAI" to 87,  // Haiti
    )

    /** Worst-case rank used when a team's TLA is unknown, so simulations still run. */
    const val UNRANKED = 999

    /** FIFA rank for the given [tla], or [UNRANKED] if not seeded. */
    fun rankFor(tla: String?): Int = byTla[tla] ?: UNRANKED
}
