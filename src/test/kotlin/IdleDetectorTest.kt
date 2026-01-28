package com.timekeeper

import java.time.LocalDateTime
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class IdleDetectorTest {

    @Test
    fun `lastActiveTime returns null when not idle`() {
        var now = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        val detector = IdleDetector(timeout = 30.seconds) { now }

        detector.reset()
        now = now.plusSeconds(10)

        assertNull(detector.lastActiveTime(now.minusMinutes(5)))
    }

    @Test
    fun `lastActiveTime returns null at exactly timeout`() {
        var now = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        val detector = IdleDetector(timeout = 30.seconds) { now }

        detector.reset()
        now = now.plusSeconds(30)

        assertNull(detector.lastActiveTime(now.minusMinutes(5)))
    }

    @Test
    fun `lastActiveTime returns previous check time when idle`() {
        var now = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        val detector = IdleDetector(timeout = 30.seconds) { now }

        detector.reset()
        val resetTime = now
        now = now.plusSeconds(31)

        val result = detector.lastActiveTime(now.minusMinutes(5))

        assertEquals(resetTime, result)
    }

    @Test
    fun `lastActiveTime returns startTime when previous check time is before startTime`() {
        var now = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        val detector = IdleDetector(timeout = 30.seconds) { now }

        detector.reset()
        now = now.plusHours(2)
        val startTime = now.minusMinutes(30)

        val result = detector.lastActiveTime(startTime)

        assertEquals(startTime, result)
    }

    @Test
    fun `lastActiveTime returns startTime before reset is called`() {
        var now = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        val detector = IdleDetector(timeout = 30.seconds) { now }
        val startTime = now.minusMinutes(5)

        val result = detector.lastActiveTime(startTime)

        assertEquals(startTime, result)
    }

    @Test
    fun `reset updates lastCheckTime`() {
        var now = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        val detector = IdleDetector(timeout = 30.seconds) { now }

        detector.reset()
        now = now.plusSeconds(10)
        detector.reset()
        now = now.plusSeconds(10)

        assertNull(detector.lastActiveTime(now.minusMinutes(5)))
    }

    @Test
    fun `consecutive calls without idle return null`() {
        var now = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        val detector = IdleDetector(timeout = 30.seconds) { now }

        detector.reset()

        repeat(5) {
            now = now.plusSeconds(10)
            assertNull(detector.lastActiveTime(now.minusMinutes(5)))
        }
    }
}
