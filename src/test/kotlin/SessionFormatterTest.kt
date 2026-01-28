package com.timekeeper

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.*
import kotlin.io.path.writeText
import kotlin.test.*

class SessionFormatterTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var dataFile: Path

    @BeforeEach
    fun setUp() {
        dataFile = tempDir.resolve(".timekeeper_data.txt")
    }

    @Test
    fun `buildDailyText - contains TODAY header`() {
        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildDailyText(TimerState.Stopped)

        assertContains(text, "TODAY")
        assertContains(text, "=".repeat(50))
    }

    @Test
    fun `buildDailyText - shows session times`() {
        val today = LocalDate.now()
        val start = today.atTime(9, 0)
        val end = today.atTime(10, 30)
        dataFile.writeText("$start|$end|false\n")

        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildDailyText(TimerState.Stopped)

        assertContains(text, "09:00")
        assertContains(text, "10:30")
        assertContains(text, "(01:30)")
    }

    @Test
    fun `buildDailyText - shows in progress session when started`() {
        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)
        val startTime = LocalDateTime.now().minusMinutes(30)

        val text = formatter.buildDailyText(TimerState.Started(startTime))

        assertContains(text, "[in progress]")
        assertContains(text, "...")
    }

    @Test
    fun `buildDailyText - shows auto-stopped suffix`() {
        val today = LocalDate.now()
        val start = today.atTime(9, 0)
        val end = today.atTime(10, 0)
        dataFile.writeText("$start|$end|true\n")

        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildDailyText(TimerState.Stopped)

        assertContains(text, "[auto-stopped]")
    }

    @Test
    fun `buildDailyText - shows day total`() {
        val today = LocalDate.now()
        val start = today.atTime(9, 0)
        val end = today.atTime(10, 0)
        dataFile.writeText("$start|$end|false\n")

        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildDailyText(TimerState.Stopped)

        assertContains(text, "Day")
        assertContains(text, "(01:00)")
    }

    @Test
    fun `buildWeeklyText - contains THIS WEEK header`() {
        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildWeeklyText(TimerState.Stopped)

        assertContains(text, "THIS WEEK")
        assertContains(text, "=".repeat(50))
    }

    @Test
    fun `buildWeeklyText - shows week total`() {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val start = monday.atTime(9, 0)
        val end = monday.atTime(11, 0)
        dataFile.writeText("$start|$end|false\n")

        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildWeeklyText(TimerState.Stopped)

        assertContains(text, "Week")
    }

    @Test
    fun `buildWeeklyText - shows days from Monday to today`() {
        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildWeeklyText(TimerState.Stopped)

        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        assertContains(text, monday.formatDate())

        val today = LocalDate.now()
        assertContains(text, today.formatDate())
    }

    @Test
    fun `buildWeeklyText - groups sessions by date`() {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        val tuesday = monday.plusDays(1)

        val sessions = buildString {
            appendLine("${monday.atTime(9, 0)}|${monday.atTime(10, 0)}|false")
            if (tuesday <= LocalDate.now()) {
                appendLine("${tuesday.atTime(14, 0)}|${tuesday.atTime(15, 0)}|false")
            }
        }
        dataFile.writeText(sessions)

        val tracker = TimeTracker(dataFile)
        val formatter = SessionFormatter(tracker)

        val text = formatter.buildWeeklyText(TimerState.Stopped)

        assertContains(text, monday.formatDate())
        assertTrue(text.indexOf("09:00") < text.indexOf(monday.formatDate()).let { idx ->
            text.indexOf("Day", idx)
        })
    }
}
