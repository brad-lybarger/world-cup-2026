package com.blybarger.worldcup2026.tournament

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.MatchStatus
import com.blybarger.worldcup2026.domain.Stage
import com.blybarger.worldcup2026.domain.Team
import com.blybarger.worldcup2026.simulator.MatchSimulator

/** A fully simulated tournament: completed group stage, qualification, knockout bracket, champion. */
data class TournamentResult(
    val groupMatches: List<Match>,
    val qualification: TournamentQualification,
    val knockoutMatches: List<Match>,
    val champion: Team,
) {
    fun knockout(matchNo: Int): Match = knockoutMatches.first { it.id == matchNo.toLong() }
}

/**
 * Runs a full FIFA-ranking-weighted simulation from the current state to a champion:
 *  1. simulate any undecided group matches (decided ones keep their real result),
 *  2. compute standings & qualification (top 2 + 8 best thirds),
 *  3. seed the Round of 32 from the official matchups + third-place allocation table,
 *  4. play out the knockout tree.
 *
 * Because step 2 runs on the real results once the group stage is complete, the seeded bracket
 * then equals the official bracket — so this same path serves both the "projected" phase (now) and
 * the live phase. (A future refinement can additionally prefer API-provided knockout teams for
 * exact reconciliation in edge cases.)
 */
class TournamentSimulator(
    private val simulator: MatchSimulator = MatchSimulator(),
) {
    fun simulate(allMatches: List<Match>): TournamentResult {
        val completedGroups = allMatches
            .filter { it.stage == Stage.GROUP_STAGE }
            .map { if (it.status == MatchStatus.FINISHED) it else simulator.simulate(it) }

        val qualification = Qualification.compute(completedGroups)
        val knockout = simulateKnockout(qualification)

        return TournamentResult(
            groupMatches = completedGroups,
            qualification = qualification,
            knockoutMatches = knockout,
            champion = knockout.first { it.id == BracketStructure.FINAL_MATCH.toLong() }.winner!!,
        )
    }

    private fun simulateKnockout(q: TournamentQualification): List<Match> {
        val winners = HashMap<Int, Team>()
        val losers = HashMap<Int, Team>()
        val played = ArrayList<Match>(BracketStructure.fixtures.size)

        fun resolve(f: Feeder): Team = when (f) {
            is Feeder.Winner -> q.groups.getValue(f.group).winner
            is Feeder.RunnerUp -> q.groups.getValue(f.group).runnerUp
            is Feeder.ThirdVsWinner -> {
                val thirdGroup = ThirdPlaceAllocation.thirdGroupFor(q.thirdPlaceKey, f.winnerGroup)
                q.thirdByGroup.getValue(thirdGroup)
            }
            is Feeder.WinnerOf -> winners.getValue(f.match)
            is Feeder.LoserOf -> losers.getValue(f.match)
        }

        for (fx in BracketStructure.fixtures) {
            val home = resolve(fx.home)
            val away = resolve(fx.away)
            val result = simulator.simulate(
                Match(id = fx.matchNo.toLong(), stage = fx.stage, homeTeam = home, awayTeam = away)
            )
            val winner = result.winner!!
            winners[fx.matchNo] = winner
            losers[fx.matchNo] = if (winner == home) away else home
            played += result
        }
        return played
    }
}
