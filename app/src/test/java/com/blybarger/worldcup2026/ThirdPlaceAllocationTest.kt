package com.blybarger.worldcup2026

import com.blybarger.worldcup2026.tournament.ThirdPlaceAllocation
import org.junit.Assert.assertEquals
import org.junit.Test

class ThirdPlaceAllocationTest {

    @Test
    fun `loads all 495 combinations`() {
        assertEquals(495, ThirdPlaceAllocation.size)
    }

    @Test
    fun `resolves a known combination (EFGHIJKL)`() {
        // CSV row: EFGHIJKL,E,J,I,F,H,G,L,K  (slots A,B,D,E,G,I,K,L)
        val key = "EFGHIJKL"
        assertEquals("E", ThirdPlaceAllocation.thirdGroupFor(key, "A"))
        assertEquals("J", ThirdPlaceAllocation.thirdGroupFor(key, "B"))
        assertEquals("I", ThirdPlaceAllocation.thirdGroupFor(key, "D"))
        assertEquals("F", ThirdPlaceAllocation.thirdGroupFor(key, "E"))
        assertEquals("H", ThirdPlaceAllocation.thirdGroupFor(key, "G"))
        assertEquals("G", ThirdPlaceAllocation.thirdGroupFor(key, "I"))
        assertEquals("L", ThirdPlaceAllocation.thirdGroupFor(key, "K"))
        assertEquals("K", ThirdPlaceAllocation.thirdGroupFor(key, "L"))
    }

    @Test
    fun `resolves another known combination (ABCDEFGH)`() {
        // CSV row: ABCDEFGH,H,G,B,C,A,F,D,E
        val key = "ABCDEFGH"
        assertEquals("H", ThirdPlaceAllocation.thirdGroupFor(key, "A"))
        assertEquals("G", ThirdPlaceAllocation.thirdGroupFor(key, "B"))
        assertEquals("E", ThirdPlaceAllocation.thirdGroupFor(key, "L"))
    }

    @Test
    fun `every assignment is one of the advancing groups`() {
        val key = "ABCDEFGH"
        val assigned = ThirdPlaceAllocation.SLOT_GROUPS.map { ThirdPlaceAllocation.thirdGroupFor(key, it) }
        assertEquals(key.toCharArray().map { it.toString() }.toSet(), assigned.toSet())
    }
}
