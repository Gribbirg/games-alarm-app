package com.example.smartalarm.feature.games.binary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Тесты чистого конвертера [BinaryNumbers]. */
class BinaryNumbersTest {

    @Test
    fun `toBinary produces known values`() {
        assertEquals("1", BinaryNumbers.toBinary(1))
        assertEquals("10", BinaryNumbers.toBinary(2))
        assertEquals("101", BinaryNumbers.toBinary(5))
        assertEquals("1010", BinaryNumbers.toBinary(10))
        assertEquals("1101", BinaryNumbers.toBinary(13))
        assertEquals("1111", BinaryNumbers.toBinary(15))
        assertEquals("111111", BinaryNumbers.toBinary(63))
        assertEquals("1000000", BinaryNumbers.toBinary(64))
        assertEquals("10000000", BinaryNumbers.toBinary(128))
        assertEquals("11111111", BinaryNumbers.toBinary(255))
    }

    @Test
    fun `fromBinary parses known values`() {
        assertEquals(1, BinaryNumbers.fromBinary("1"))
        assertEquals(5, BinaryNumbers.fromBinary("101"))
        assertEquals(13, BinaryNumbers.fromBinary("1101"))
        assertEquals(255, BinaryNumbers.fromBinary("11111111"))
    }

    @Test
    fun `roundtrip holds for the whole game range`() {
        for (number in 1..255) {
            val binary = BinaryNumbers.toBinary(number)
            assertTrue(
                "запись \"$binary\" числа $number не каноническая",
                binary.matches(Regex("^1[01]*$"))
            )
            assertEquals(number, BinaryNumbers.fromBinary(binary))
        }
    }

    @Test
    fun `toBinary rejects non-positive numbers`() {
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.toBinary(0) }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.toBinary(-5) }
    }

    @Test
    fun `fromBinary rejects invalid strings`() {
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary("") }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary("0") }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary("011") }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary("102") }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary("13") }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary(" 101") }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary("-1") }
        assertThrows(IllegalArgumentException::class.java) { BinaryNumbers.fromBinary("abc") }
    }
}
