package com.blybarger.worldcup2026.tournament

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.Team

/** Final standings for one group. Positions 0/1/2 are winner / runner-up / third. */
data class GroupResult(val group: String, val standings: List<TeamRecord>) {
    val winner: Team get() = standings[0].team
    val runnerUp: Team get() = standings[1].team
    val third: Team get() = standings[2].team
    val thirdRecord: TeamRecord get() = standings[2]
}

/**
 * Who advances to the Round of 32: the 12 group winners, 12 runners-up, and the 8 best
 * third-placed teams.
 *
 * @param thirdPlaceKey the 8 advancing-third groups as sorted letters (e.g. "EFGHIJKL") — the key
 *   into the official allocation table.
 */
data class TournamentQualification(
    val groups: Map<String, GroupResult>,
    val advancingThirdGroups: List<String>,
    val thirdByGroup: Map<String, Team>,
) {
    val thirdPlaceKey: String get() = advancingThirdGroups.joinToString("")
}

object Qualification {

    /** Ranks third-placed teams: points → goal difference → goals scored → FIFA ranking. */
    private val thirdRanking: Comparator<Pair<String, TeamRecord>> =
        compareByDescending<Pair<String, TeamRecord>> { it.second.points }
            .thenByDescending { it.second.goalDifference }
            .thenByDescending { it.second.goalsFor }
            .thenBy { it.second.team.fifaRank }

    /** Computes qualification from all group-stage matches (group field must be set). */
    fun compute(groupStageMatches: List<Match>): TournamentQualification {
        val groups = groupStageMatches
            .filter { it.group != null }
            .groupBy { it.group!! }
            .mapValues { (g, ms) -> GroupResult(g, GroupStandings.compute(ms)) }
            .toSortedMap()

        val best8 = groups
            .map { (g, res) -> g to res.thirdRecord }
            .sortedWith(thirdRanking)
            .take(8)

        return TournamentQualification(
            groups = groups,
            advancingThirdGroups = best8.map { it.first }.sorted(),
            thirdByGroup = best8.associate { it.first to it.second.team },
        )
    }
}
