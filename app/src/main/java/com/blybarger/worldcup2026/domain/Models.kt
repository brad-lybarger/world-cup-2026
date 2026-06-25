package com.blybarger.worldcup2026.domain

/**
 * A national team in the tournament.
 *
 * @param fifaRank the team's FIFA/Coca-Cola World Ranking position (1 = best). Used as the
 *   weight for simulated match outcomes.
 */
data class Team(
    val id: String,
    val name: String,
    val fifaRank: Int,
)

/**
 * Knockout rounds, in order. [points] is the score a correct bracket prediction earns for a
 * match in that round, doubling each round per the game rules.
 */
enum class Round(val displayName: String, val points: Int) {
    ROUND_OF_32("Round of 32", 1),
    ROUND_OF_16("Round of 16", 2),
    QUARTERFINAL("Quarterfinal", 4),
    SEMIFINAL("Semifinal", 8),
    FINAL("Final", 16),
}

/**
 * A single knockout match between two teams. A match may not yet have both teams decided
 * (they come from earlier matches), hence the nullable slots.
 *
 * @param winner the actual/decided winner, if known.
 */
data class Match(
    val id: String,
    val round: Round,
    val homeTeam: Team?,
    val awayTeam: Team?,
    val winner: Team? = null,
) {
    /** Both participants are known and a winner can be produced. */
    val isReady: Boolean get() = homeTeam != null && awayTeam != null

    /** The result is final. */
    val isDecided: Boolean get() = winner != null
}
