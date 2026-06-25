package com.blybarger.worldcup2026.tournament

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team

/** A user's predicted winner for each scored knockout match, keyed by match number (73–104). */
data class BracketPrediction(val winners: Map<Int, Team>)

/** A round's contribution to the scorecard. */
data class RoundScore(val stage: Stage, val correct: Int, val total: Int) {
    val pointsEach: Int get() = stage.points
    val points: Int get() = correct * stage.points
}

/** The full scorecard across the knockout rounds. */
data class ScoreCard(val rounds: List<RoundScore>) {
    val totalPoints: Int get() = rounds.sumOf { it.points }
    val totalCorrect: Int get() = rounds.sumOf { it.correct }
    /** Maximum points achievable for the rounds present (perfect bracket). */
    val maxPoints: Int get() = rounds.sumOf { it.total * it.stage.points }
}

/**
 * Scores a user's bracket prediction against actual (or simulated) results. Each correctly
 * predicted match winner earns its round's points, doubling per round:
 * Round of 32 = 1, Round of 16 = 2, Quarterfinal = 4, Semifinal = 8, Final = 16.
 * The group stage and third-place play-off are not part of the game.
 */
object BracketScorer {

    /** Scored knockout rounds, in order. */
    val SCORED_STAGES = listOf(
        Stage.ROUND_OF_32, Stage.ROUND_OF_16, Stage.QUARTERFINAL, Stage.SEMIFINAL, Stage.FINAL,
    )

    fun score(prediction: BracketPrediction, actualKnockout: List<Match>): ScoreCard {
        val byStage = actualKnockout.filter { it.stage in SCORED_STAGES }.groupBy { it.stage }
        val rounds = SCORED_STAGES.map { stage ->
            val matches = byStage[stage].orEmpty()
            val correct = matches.count { m ->
                val predicted = prediction.winners[m.id.toInt()]
                val actual = m.winner
                predicted != null && actual != null && predicted.id == actual.id
            }
            RoundScore(stage, correct = correct, total = matches.size)
        }
        return ScoreCard(rounds)
    }

    /** The prediction that exactly matches [result] — a perfect bracket. */
    fun perfectPrediction(result: TournamentResult): BracketPrediction =
        BracketPrediction(
            result.knockoutMatches
                .filter { it.stage in SCORED_STAGES && it.winner != null }
                .associate { it.id.toInt() to it.winner!! }
        )
}
