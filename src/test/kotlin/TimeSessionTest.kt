package com.timekeeper

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TimeSessionTest {

    @Test
    fun `duration - calculates time between start and end`() {
        val session = TimeSession(
            start = LocalDateTime.of(2024, 1, 15, 9, 0, 0),
            end = LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        )
        assertEquals(1.hours + 30.minutes, session.duration)
    }

    @Test
    fun `duration - handles short sessions`() {
        val session = TimeSession(
            start = LocalDateTime.of(2024, 1, 15, 9, 0, 0),
            end = LocalDateTime.of(2024, 1, 15, 9, 5, 0)
        )
        assertEquals(5.minutes, session.duration)
    }

    @Test
    fun `duration - handles overnight sessions`() {
        val session = TimeSession(
            start = LocalDateTime.of(2024, 1, 15, 23, 0, 0),
            end = LocalDateTime.of(2024, 1, 16, 1, 0, 0)
        )
        assertEquals(2.hours, session.duration)
    }

    @Test
    fun `duration - zero duration when start equals end`() {
        val time = LocalDateTime.of(2024, 1, 15, 9, 0, 0)
        val session = TimeSession(start = time, end = time)
        assertEquals(0.minutes, session.duration)
    }

    @Test
    fun `default values - inProgress is false`() {
        val session = TimeSession(
            start = LocalDateTime.of(2024, 1, 15, 9, 0, 0),
            end = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        )
        assertEquals(false, session.inProgress)
    }

    @Test
    fun `default values - autoStopped is false`() {
        val session = TimeSession(
            start = LocalDateTime.of(2024, 1, 15, 9, 0, 0),
            end = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        )
        assertEquals(false, session.autoStopped)
    }
}
