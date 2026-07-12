package com.example.smartalarm.feature.games.roman

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Тесты конвертера [RomanNumerals]. */
class RomanNumeralsTest {

    @Test
    fun `toRoman converts key values`() {
        val expected = mapOf(
            1 to "I",
            3 to "III",
            4 to "IV",
            5 to "V",
            9 to "IX",
            14 to "XIV",
            19 to "XIX",
            20 to "XX",
            40 to "XL",
            49 to "XLIX",
            58 to "LVIII",
            90 to "XC",
            99 to "XCIX",
            100 to "C",
            400 to "CD",
            444 to "CDXLIV",
            900 to "CM",
            999 to "CMXCIX",
            1000 to "M",
            1994 to "MCMXCIV",
            2024 to "MMXXIV",
            3888 to "MMMDCCCLXXXVIII",
            3999 to "MMMCMXCIX"
        )
        for ((number, roman) in expected) {
            assertEquals("toRoman($number)", roman, RomanNumerals.toRoman(number))
        }
    }

    @Test
    fun `toRoman rejects out of range values`() {
        assertThrows(IllegalArgumentException::class.java) { RomanNumerals.toRoman(0) }
        assertThrows(IllegalArgumentException::class.java) { RomanNumerals.toRoman(-1) }
        assertThrows(IllegalArgumentException::class.java) { RomanNumerals.toRoman(4000) }
    }

    @Test
    fun `fromRoman toRoman roundtrip holds for full range`() {
        for (number in 1..RomanNumerals.MAX_VALUE) {
            assertEquals(
                "roundtrip for $number",
                number,
                RomanNumerals.fromRoman(RomanNumerals.toRoman(number))
            )
        }
    }

    @Test
    fun `fromRoman parses subtractive notation`() {
        assertEquals(4, RomanNumerals.fromRoman("IV"))
        assertEquals(9, RomanNumerals.fromRoman("IX"))
        assertEquals(40, RomanNumerals.fromRoman("XL"))
        assertEquals(90, RomanNumerals.fromRoman("XC"))
        assertEquals(400, RomanNumerals.fromRoman("CD"))
        assertEquals(900, RomanNumerals.fromRoman("CM"))
        assertEquals(1994, RomanNumerals.fromRoman("MCMXCIV"))
    }

    @Test
    fun `fromRoman rejects invalid input`() {
        val invalid = listOf("", "ABC", "IIII", "VV", "IL", "IC", "XM", "MCMC", "IXIX", "iv")
        for (roman in invalid) {
            assertThrows("fromRoman(\"$roman\")", IllegalArgumentException::class.java) {
                RomanNumerals.fromRoman(roman)
            }
        }
    }
}
