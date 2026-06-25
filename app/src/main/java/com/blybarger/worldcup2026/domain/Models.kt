package com.blybarger.worldcup2026.domain

/**
 * A national team.
 *
 * @param id stable identifier — we use the team's TLA (three-letter code) from the API.
 * @param fifaRank FIFA/Coca-Cola World Ranking position (1 = best). Weights simulated outcomes.
 */
data class Team(
    val id: String,
    val name: String,
    val fifaRank: Int,
)

/**
 * Tournament stage. [points] is the score a correct bracket prediction earns for a match in that
 * stage, doubling each knockout round per the game rules. Group stage and the third-place play-off
 * are not part of the prediction game, so they score 0.
 */
enum class Stage(val displayName: String, val points: Int) {
    GROUP_STAGE("Group Stage", 0),
    ROUND_OF_32("Round of 32", 1),
    ROUND_OF_16("Round of 16", 2),
    QUARTERFINAL("Quarterfinal", 4),
    SEMIFINAL("Semifinal", 8),
    THIRD_PLACE("Third-place Play-off", 0),
    FINAL("Final", 16);

    val isKnockout: Boolean get() = this != GROUP_STAGE
}

/** Lifecycle of a match as reported by the data source. */
enum class MatchStatus { SCHEDULED, LIVE, FINISHED }

/**
 * A single match. Teams may be undecided for knockout slots whose feeder matches haven't resolved
 * (nullable). [winner] is null for an undecided match *or* a drawn (finished) group match.
 */
data class Match(
    val id: Long,
    val stage: Stage,
    val group: String? = null,
    val homeTeam: Team? = null,
    val awayTeam: Team? = null,
    val homeGoals: Int? = null,
    val awayGoals: Int? = null,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val winner: Team? = null,
) {
    /** Both participants are known. */
    val isReady: Boolean get() = homeTeam != null && awayTeam != null

    /** The result is official. */
    val isFinished: Boolean get() = status == MatchStatus.FINISHED

    /** A finished match with no winner (only possible in the group stage). */
    val isDraw: Boolean get() = isFinished && isReady && winner == null
}
