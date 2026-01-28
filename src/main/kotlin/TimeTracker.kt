package com.timekeeper

import java.nio.file.Path
import java.time.*
import kotlin.io.path.*
import kotlin.time.Duration

data class TimeSession(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val inProgress: Boolean = false,
    val autoStopped: Boolean = false
) {
    val duration: Duration
        get() = start.until(end)
}

class TimeTracker(
    private val dataFile: Path = Path(System.getProperty("user.home"), ".timekeeper_data.txt")
) {
    private val sessions = mutableListOf<TimeSession>()

    init {
        loadSessions()
    }

    fun addSession(startTime: LocalDateTime, endTime: LocalDateTime?) {
        sessions.add(
            TimeSession(
                startTime,
                endTime ?: LocalDateTime.now(),
                autoStopped = endTime != null
            )
        )
        saveSessions()
    }

    fun getTodaySessions() =
        LocalDate.now().let { today ->
            sessions.filter { it.start.toLocalDate() == today }
        }

    fun getWeekSessions(): List<TimeSession> {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay()
        return sessions.filter { it.start >= monday }
    }

    fun getTotalDuration(sessions: List<TimeSession>): Duration =
        sessions.fold(Duration.ZERO) { acc, session -> acc + session.duration }

    private fun saveSessions() {
        runCatching {
            dataFile.writeLines(sessions.map { "${it.start}|${it.end}|${it.autoStopped}" })
        }.onFailure { showError("Failed to save sessions: ${it.message}") }
    }

    private fun loadSessions() {
        if (!dataFile.exists()) return

        runCatching {
            dataFile.forEachLine { line ->
                val parts = line.split("|")
                if (parts.size >= 2) {
                    runCatching {
                        val start = LocalDateTime.parse(parts[0])
                        val end = LocalDateTime.parse(parts[1])
                        val autoStopped = if (parts.size >= 3) parts[2].toBoolean() else false
                        sessions.add(TimeSession(start, end, autoStopped = autoStopped))
                    }
                }
            }
        }.onFailure { showError("Failed to load sessions: ${it.message}") }
    }
}
