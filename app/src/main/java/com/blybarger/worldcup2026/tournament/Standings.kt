package com.blybarger.worldcup2026.tournament

import com.blybarger.worldcup2026.domain.Match
import com.blybarger.worldcup2026.domain.Team

/** A team's group-stage record. */
data class TeamRecord(
    val team: Team,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
) {
    val points: Int get() = won * 3 + drawn
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

/**
 * Computes group standings from match results, applying FIFA's tiebreakers in order:
 * points → goal difference → goals scored → head-to-head (among the tied teams) → FIFA ranking.
 *
 * Fair-play points (which sit between head-to-head and FIFA ranking in the real rules) are omitted
 * because the data source doesn't expose disciplinary records. A *simulated* win (winner set, no
 * goals) is scored as 1-0; real results — including draws — use their actual goals.
 */
object GroupStandings {

    private class Acc(val team: Team) {
        var played = 0; var won = 0; var drawn = 0; var lost = 0
        var gf = 0; var ga = 0
        fun toRecord() = TeamRecord(team, played, won, drawn, lost, gf, ga)
    }

    /** Effective goals for a match that has a result, or null if it hasn't been decided yet. */
    private fun resultGoals(m: Match): Pair<Int, Int>? {
        val home = m.homeTeam ?: return null
        val away = m.awayTeam ?: return null
        return when {
            m.homeGoals != null && m.awayGoals != null -> m.homeGoals to m.awayGoals
            m.winner == home -> 1 to 0
            m.winner == away -> 0 to 1
            else -> null
        }
    }

    private fun accumulate(matches: List<Match>, teams: List<Team>): Map<String, TeamRecord> {
        val acc = LinkedHashMap<String, Acc>()
        teams.forEach { acc.getOrPut(it.id) { Acc(it) } }
        for (m in matches) {
            val (hg, ag) = resultGoals(m) ?: continue
            val h = acc.getOrPut(m.homeTeam!!.id) { Acc(m.homeTeam) }
            val a = acc.getOrPut(m.awayTeam!!.id) { Acc(m.awayTeam) }
            h.played++; a.played++
            h.gf += hg; h.ga += ag; a.gf += ag; a.ga += hg
            when {
                hg > ag -> { h.won++; a.lost++ }
                hg < ag -> { a.won++; h.lost++ }
                else -> { h.drawn++; a.drawn++ }
            }
        }
        return acc.mapValues { it.value.toRecord() }
    }

    private fun collectTeams(matches: List<Match>): List<Team> {
        val seen = LinkedHashMap<String, Team>()
        for (m in matches) {
            m.homeTeam?.let { seen[it.id] = it }
            m.awayTeam?.let { seen[it.id] = it }
        }
        return seen.values.toList()
    }

    fun compute(groupMatches: List<Match>): List<TeamRecord> {
        val records = accumulate(groupMatches, collectTeams(groupMatches)).values.toList()
        val byPrimary = records.sortedWith(
            compareByDescending<TeamRecord> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
        )
        // Break ties among teams equal on points/GD/GF using head-to-head, then FIFA ranking.
        val result = ArrayList<TeamRecord>(byPrimary.size)
        var i = 0
        while (i < byPrimary.size) {
            var j = i + 1
            while (j < byPrimary.size && samePrimary(byPrimary[i], byPrimary[j])) j++
            if (j - i == 1) result.add(byPrimary[i])
            else result.addAll(breakTie(byPrimary.subList(i, j), groupMatches))
            i = j
        }
        return result
    }

    private fun samePrimary(a: TeamRecord, b: TeamRecord) =
        a.points == b.points && a.goalDifference == b.goalDifference && a.goalsFor == b.goalsFor

    /** Head-to-head mini-table among [cluster]; remaining ties broken by FIFA ranking (no recursion). */
    private fun breakTie(cluster: List<TeamRecord>, allMatches: List<Match>): List<TeamRecord> {
        val ids = cluster.mapTo(HashSet()) { it.team.id }
        val h2h = allMatches.filter { it.homeTeam?.id in ids && it.awayTeam?.id in ids }
        val mini = accumulate(h2h, cluster.map { it.team })
        fun r(t: TeamRecord) = mini.getValue(t.team.id)
        return cluster.sortedWith(
            compareByDescending<TeamRecord> { r(it).points }
                .thenByDescending { r(it).goalDifference }
                .thenByDescending { r(it).goalsFor }
                .thenBy { it.team.fifaRank }
        )
    }
}
