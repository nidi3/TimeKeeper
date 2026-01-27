package com.timekeeper

import java.io.*
import java.time.*
import kotlin.time.Duration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration

data class TimeSession(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val inProgress: Boolean = false,
    val autoStopped: Boolean = false
) {
    val duration: Duration
        get() = JavaDuration.between(startTime, endTime).toKotlinDuration()
}

class TimeTracker {
    companion object {
        private val DATA_FILE = File(System.getProperty("user.home"), ".timekeeper_data.txt")
    }

    private val sessions = mutableListOf<TimeSession>()

    init {
        loadSessions()
    }

    fun addSession(session: TimeSession) {
        sessions.add(session)
        saveSessions()
    }

    fun getTodaySessions() =
        LocalDate.now().let { today ->
            sessions.filter { it.startTime.toLocalDate() == today }
        }

    fun getWeekSessions(): List<TimeSession> {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay()
        return sessions.filter { it.startTime >= monday }
    }

    fun getTotalDuration(sessions: List<TimeSession>): Duration =
        sessions.fold(Duration.ZERO) { acc, session -> acc + session.duration }

    private fun saveSessions() {
        runCatching {
            PrintWriter(FileWriter(DATA_FILE)).use { writer ->
                sessions.forEach { session ->
                    writer.println("${session.startTime}|${session.endTime}|${session.autoStopped}")
                }
            }
        }.onFailure { showError("Failed to save sessions: ${it.message}") }
    }

    private fun loadSessions() {
        if (!DATA_FILE.exists()) return

        runCatching {
            BufferedReader(FileReader(DATA_FILE)).use { reader ->
                reader.lineSequence().forEach { line ->
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
            }
        }.onFailure { showError("Failed to load sessions: ${it.message}") }
    }
}
