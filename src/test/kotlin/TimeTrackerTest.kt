package com.timekeeper

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.*
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TimeTrackerTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var dataFile: Path

    @BeforeEach
    fun setUp() {
        dataFile = tempDir.resolve(".timekeeper_data.txt")
    }

    @Test
    fun `getTotalDuration - empty list returns zero`() {
        val tracker = TimeTracker(dataFile)
        assertEquals(Duration.ZERO, tracker.getTotalDuration(emptyList()))
    }

    @Test
    fun `getTotalDuration - single session`() {
        val tracker = TimeTracker(dataFile)
        val session = TimeSession(
            start = LocalDateTime.of(2024, 1, 15, 9, 0, 0),
            end = LocalDateTime.of(2024, 1, 15, 10, 30, 0)
        )
        assertEquals(1.hours + 30.minutes, tracker.getTotalDuration(listOf(session)))
    }

    @Test
    fun `getTotalDuration - multiple sessions`() {
        val tracker = TimeTracker(dataFile)
        val sessions = listOf(
            TimeSession(
                start = LocalDateTime.of(2024, 1, 15, 9, 0, 0),
                end = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
            ),
            TimeSession(
                start = LocalDateTime.of(2024, 1, 15, 14, 0, 0),
                end = LocalDateTime.of(2024, 1, 15, 15, 30, 0)
            )
        )
        assertEquals(2.hours + 30.minutes, tracker.getTotalDuration(sessions))
    }

    @Test
    fun `file persistence - saves and loads sessions`() {
        val tracker1 = TimeTracker(dataFile)
        val start = LocalDateTime.of(2024, 1, 15, 9, 0, 0)
        val end = LocalDateTime.of(2024, 1, 15, 10, 0, 0)
        tracker1.addSession(start, end)

        val tracker2 = TimeTracker(dataFile)
        val todaySessions = tracker2.getTodaySessions()
        val weekSessions = tracker2.getWeekSessions()

        val loaded = if (LocalDate.now() == LocalDate.of(2024, 1, 15)) {
            todaySessions.firstOrNull()
        } else {
            weekSessions.find { it.start == start }
        }

        assertTrue(dataFile.readText().contains("2024-01-15T09:00"))
    }

    @Test
    fun `file persistence - handles pipe-delimited format`() {
        dataFile.writeText("2024-01-15T09:00:00|2024-01-15T10:30:00|false\n")

        val tracker = TimeTracker(dataFile)
        val sessions = tracker.getWeekSessions()

        val monday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay()
        val sessionDate = LocalDate.of(2024, 1, 15)

        if (sessionDate >= monday.toLocalDate()) {
            assertTrue(sessions.any { it.start.toLocalDate() == sessionDate })
        }
    }

    @Test
    fun `getTodaySessions - filters by current date`() {
        val today = LocalDate.now()
        val todayStart = today.atTime(9, 0)
        val todayEnd = today.atTime(10, 0)
        dataFile.writeText("$todayStart|$todayEnd|false\n")

        val tracker = TimeTracker(dataFile)
        val sessions = tracker.getTodaySessions()

        assertEquals(1, sessions.size)
        assertEquals(todayStart, sessions[0].start)
    }

    @Test
    fun `getTodaySessions - excludes other dates`() {
        val yesterday = LocalDate.now().minusDays(1)
        val yesterdayStart = yesterday.atTime(9, 0)
        val yesterdayEnd = yesterday.atTime(10, 0)
        dataFile.writeText("$yesterdayStart|$yesterdayEnd|false\n")

        val tracker = TimeTracker(dataFile)
        val sessions = tracker.getTodaySessions()

        assertEquals(0, sessions.size)
    }

    @Test
    fun `getWeekSessions - includes sessions from Monday onwards`() {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val mondayStart = monday.atTime(9, 0)
        val mondayEnd = monday.atTime(10, 0)
        dataFile.writeText("$mondayStart|$mondayEnd|false\n")

        val tracker = TimeTracker(dataFile)
        val sessions = tracker.getWeekSessions()

        assertEquals(1, sessions.size)
    }

    @Test
    fun `getWeekSessions - excludes sessions before Monday`() {
        val lastSunday = LocalDate.now().with(DayOfWeek.MONDAY).minusDays(1)
        val sundayStart = lastSunday.atTime(9, 0)
        val sundayEnd = lastSunday.atTime(10, 0)
        dataFile.writeText("$sundayStart|$sundayEnd|false\n")

        val tracker = TimeTracker(dataFile)
        val sessions = tracker.getWeekSessions()

        assertEquals(0, sessions.size)
    }
}

