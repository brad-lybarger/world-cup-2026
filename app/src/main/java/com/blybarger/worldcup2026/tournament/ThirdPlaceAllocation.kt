package com.blybarger.worldcup2026.tournament

/**
 * Official FIFA allocation of the 8 best third-placed teams to Round-of-32 bracket slots.
 *
 * Loads `third_place_allocation.csv` (a classpath resource) once, lazily. Each row maps a
 * combination key (the 8 advancing-third groups, sorted, e.g. "EFGHIJKL") to the third-placed
 * group that faces the winner of each of the 8 "winner-vs-third" slots: 1A, 1B, 1D, 1E, 1G, 1I,
 * 1K, 1L.
 */
object ThirdPlaceAllocation {

    /** The 8 winner-slot groups, in the CSV's column order. */
    val SLOT_GROUPS = listOf("A", "B", "D", "E", "G", "I", "K", "L")

    private val table: Map<String, Map<String, String>> by lazy { load() }

    private fun load(): Map<String, Map<String, String>> {
        val stream = javaClass.getResourceAsStream("/third_place_allocation.csv")
            ?: error("third_place_allocation.csv not found on classpath")
        return stream.bufferedReader().useLines { lines ->
            lines.filter { it.isNotBlank() }.associate { line ->
                val parts = line.split(",")
                require(parts.size == 9) { "Malformed allocation row: $line" }
                val key = parts[0]
                key to SLOT_GROUPS.mapIndexed { i, slot -> slot to parts[i + 1] }.toMap()
            }
        }
    }

    /** Number of combinations loaded (should be 495). */
    val size: Int get() = table.size

    /**
     * The group whose third-placed team faces the winner of [winnerGroup], given the advancing
     * third-place combination [key].
     */
    fun thirdGroupFor(key: String, winnerGroup: String): String {
        val row = table[key] ?: error("No allocation row for combination '$key'")
        return row[winnerGroup] ?: error("Slot '$winnerGroup' is not a winner-vs-third slot")
    }
}
