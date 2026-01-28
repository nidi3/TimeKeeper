package com.timekeeper

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ExtensionsTest {

    @Test
    fun `Duration format - zero duration`() {
        assertEquals("00:00", 0.seconds.format())
    }

    @Test
    fun `Duration format - exact minutes`() {
        assertEquals("00:05", 5.minutes.format())
        assertEquals("01:00", 60.minutes.format())
        assertEquals("02:30", 150.minutes.format())
    }

    @Test
    fun `Duration format - rounds up at 30 seconds`() {
        assertEquals("00:01", 30.seconds.format())
        assertEquals("00:02", 90.seconds.format())
    }

    @Test
    fun `Duration format - rounds down below 30 seconds`() {
        assertEquals("00:00", 29.seconds.format())
        assertEquals("00:01", 89.seconds.format())
    }

    @Test
    fun `Duration format - hours and minutes`() {
        assertEquals("01:30", (1.hours + 30.minutes).format())
        assertEquals("10:05", (10.hours + 5.minutes).format())
    }

    @Test
    fun `Duration format - pads single digits`() {
        assertEquals("01:05", (1.hours + 5.minutes).format())
        assertEquals("00:09", 9.minutes.format())
    }

    @Test
    fun `LocalDateTime formatTime - formats as HH colon mm`() {
        val time = LocalDateTime.of(2024, 1, 15, 9, 5, 30)
        assertEquals("09:05", time.formatTime())
    }

    @Test
    fun `LocalDateTime formatTime - handles afternoon time`() {
        val time = LocalDateTime.of(2024, 1, 15, 14, 30, 0)
        assertEquals("14:30", time.formatTime())
    }

    @Test
    fun `LocalDate formatDate - includes day of week`() {
        val date = LocalDate.of(2024, 1, 15) // Monday
        val formatted = date.formatDate()
        assertTrue(formatted.startsWith("2024-01-15 ("))
        assertTrue(formatted.endsWith(")"))
    }

    @Test
    fun `LocalDate formatDate - different dates have different day names`() {
        val monday = LocalDate.of(2024, 1, 15).formatDate()
        val tuesday = LocalDate.of(2024, 1, 16).formatDate()
        val wednesday = LocalDate.of(2024, 1, 17).formatDate()

        assertTrue(monday != tuesday)
        assertTrue(tuesday != wednesday)
        assertTrue(monday.contains("2024-01-15"))
        assertTrue(tuesday.contains("2024-01-16"))
    }

    @Test
    fun `LocalDateTime until - calculates duration between times`() {
        val start = LocalDateTime.of(2024, 1, 15, 9, 0, 0)
        val end = LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        assertEquals(1.hours + 30.minutes, start.until(end))
    }

    @Test
    fun `LocalDateTime until - handles same time`() {
        val time = LocalDateTime.of(2024, 1, 15, 9, 0, 0)
        assertEquals(0.seconds, time.until(time))
    }

    @Test
    fun `LocalDateTime until - handles overnight duration`() {
        val start = LocalDateTime.of(2024, 1, 15, 23, 0, 0)
        val end = LocalDateTime.of(2024, 1, 16, 1, 0, 0)
        assertEquals(2.hours, start.until(end))
    }
}
