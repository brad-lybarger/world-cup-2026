package com.blybarger.worldcup2026.tournament

import com.blybarger.worldcup2026.domain.Stage

/** Where a knockout participant comes from. */
sealed interface Feeder {
    data class Winner(val group: String) : Feeder
    data class RunnerUp(val group: String) : Feeder
    /** The third-placed team that faces the winner of [winnerGroup] (resolved via the allocation table). */
    data class ThirdVsWinner(val winnerGroup: String) : Feeder
    data class WinnerOf(val match: Int) : Feeder
    data class LoserOf(val match: Int) : Feeder
}

/** One knockout fixture: its match number, stage, and the two feeders that supply its teams. */
data class KnockoutFixture(
    val matchNo: Int,
    val stage: Stage,
    val home: Feeder,
    val away: Feeder,
)

/**
 * The official 2026 World Cup knockout bracket (matches 73–104), source: FIFA via the Wikipedia
 * knockout-stage bracket. Fixtures are ordered so each one depends only on earlier match numbers,
 * allowing a single forward pass to resolve and simulate the whole tree.
 */
object BracketStructure {

    private fun w(g: String) = Feeder.Winner(g)
    private fun r(g: String) = Feeder.RunnerUp(g)
    private fun third(winnerGroup: String) = Feeder.ThirdVsWinner(winnerGroup)
    private fun win(m: Int) = Feeder.WinnerOf(m)
    private fun lose(m: Int) = Feeder.LoserOf(m)

    val fixtures: List<KnockoutFixture> = listOf(
        // Round of 32
        KnockoutFixture(73, Stage.ROUND_OF_32, r("A"), r("B")),
        KnockoutFixture(74, Stage.ROUND_OF_32, w("E"), third("E")),
        KnockoutFixture(75, Stage.ROUND_OF_32, w("F"), r("C")),
        KnockoutFixture(76, Stage.ROUND_OF_32, w("C"), r("F")),
        KnockoutFixture(77, Stage.ROUND_OF_32, w("I"), third("I")),
        KnockoutFixture(78, Stage.ROUND_OF_32, r("E"), r("I")),
        KnockoutFixture(79, Stage.ROUND_OF_32, w("A"), third("A")),
        KnockoutFixture(80, Stage.ROUND_OF_32, w("L"), third("L")),
        KnockoutFixture(81, Stage.ROUND_OF_32, w("D"), third("D")),
        KnockoutFixture(82, Stage.ROUND_OF_32, w("G"), third("G")),
        KnockoutFixture(83, Stage.ROUND_OF_32, r("K"), r("L")),
        KnockoutFixture(84, Stage.ROUND_OF_32, w("H"), r("J")),
        KnockoutFixture(85, Stage.ROUND_OF_32, w("B"), third("B")),
        KnockoutFixture(86, Stage.ROUND_OF_32, w("J"), r("H")),
        KnockoutFixture(87, Stage.ROUND_OF_32, w("K"), third("K")),
        KnockoutFixture(88, Stage.ROUND_OF_32, r("D"), r("G")),
        // Round of 16
        KnockoutFixture(89, Stage.ROUND_OF_16, win(74), win(77)),
        KnockoutFixture(90, Stage.ROUND_OF_16, win(73), win(75)),
        KnockoutFixture(91, Stage.ROUND_OF_16, win(76), win(78)),
        KnockoutFixture(92, Stage.ROUND_OF_16, win(79), win(80)),
        KnockoutFixture(93, Stage.ROUND_OF_16, win(83), win(84)),
        KnockoutFixture(94, Stage.ROUND_OF_16, win(81), win(82)),
        KnockoutFixture(95, Stage.ROUND_OF_16, win(86), win(88)),
        KnockoutFixture(96, Stage.ROUND_OF_16, win(85), win(87)),
        // Quarterfinals
        KnockoutFixture(97, Stage.QUARTERFINAL, win(89), win(90)),
        KnockoutFixture(98, Stage.QUARTERFINAL, win(93), win(94)),
        KnockoutFixture(99, Stage.QUARTERFINAL, win(91), win(92)),
        KnockoutFixture(100, Stage.QUARTERFINAL, win(95), win(96)),
        // Semifinals
        KnockoutFixture(101, Stage.SEMIFINAL, win(97), win(98)),
        KnockoutFixture(102, Stage.SEMIFINAL, win(99), win(100)),
        // Third-place play-off and final
        KnockoutFixture(103, Stage.THIRD_PLACE, lose(101), lose(102)),
        KnockoutFixture(104, Stage.FINAL, win(101), win(102)),
    )

    const val FINAL_MATCH = 104
}
