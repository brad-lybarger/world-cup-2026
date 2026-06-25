package com.blybarger.worldcup2026.simulator

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.Team
import kotlin.math.pow
import kotlin.random.Random

/**
 * Computes the probability that [a] beats [b], based on FIFA ranking.
 *
 * Default model: p(a beats b) = rankB^k / (rankA^k + rankB^k).
 * A better (lower) rank number yields a higher win probability, but upsets are always possible.
 * [upsetFactor] (k) tunes how strongly ranking dominates: k = 0 makes every match a coin flip,
 * larger k makes the favorite more dominant. k = 1.0 is a sensible default.
 *
 * Example (k = 1.0): rank 1 vs rank 2 -> 2 / (1 + 2) = 0.667 for the #1 team.
 */
fun winProbability(a: Team, b: Team, upsetFactor: Double = 1.0): Double {
    val wa = b.fifaRank.toDouble().pow(upsetFactor)
    val wb = a.fifaRank.toDouble().pow(upsetFactor)
    return wa / (wa + wb)
}

/**
 * Simulates knockout matches with FIFA-ranking-weighted random outcomes.
 *
 * @param random injectable RNG; pass a seeded [Random] for reproducible simulations and tests.
 */
class MatchSimulator(
    private val random: Random = Random.Default,
    private val upsetFactor: Double = 1.0,
) {
    /** Returns the winning team for a ready match, weighted by FIFA ranking. */
    fun simulateWinner(home: Team, away: Team): Team {
        val pHome = winProbability(home, away, upsetFactor)
        return if (random.nextDouble() < pHome) home else away
    }

    /** Returns a copy of [match] with a simulated [Match.winner]; unchanged if not ready. */
    fun simulate(match: Match): Match {
        val home = match.homeTeam ?: return match
        val away = match.awayTeam ?: return match
        return match.copy(winner = simulateWinner(home, away))
    }
}
