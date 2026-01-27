package com.timekeeper

import java.time.LocalDateTime
import java.time.Duration
import java.io.*

data class TimeSession(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val autoStopped: Boolean = false
) {
    val duration: Duration
        get() = Duration.between(startTime, endTime)
}

class TimeTracker {
    private val sessions = mutableListOf<TimeSession>()
    private val dataFile = File(System.getProperty("user.home"), ".timekeeper_data.txt")

    init {
        loadSessions()
    }

    fun addSession(session: TimeSession) {
        sessions.add(session)
        saveSessions()
    }

    fun getTodaySessions() =
        LocalDateTime.now().toLocalDate().let { today ->
            sessions.filter { it.startTime.toLocalDate() == today }
        }

    fun getWeeklySessions() =
        LocalDateTime.now().let { now ->
            sessions.filter { it.startTime.isAfter(now.minusDays(7)) }
        }

    fun getTotalDuration(sessions: List<TimeSession>) =
        sessions.fold(Duration.ZERO) { acc, session -> acc.plus(session.duration) }

    private fun saveSessions() {
        runCatching {
            PrintWriter(FileWriter(dataFile)).use { writer ->
                sessions.forEach { session ->
                    writer.println("${session.startTime}|${session.endTime}|${session.autoStopped}")
                }
            }
        }
    }

    private fun loadSessions() {
        if (!dataFile.exists()) return

        runCatching {
            BufferedReader(FileReader(dataFile)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val parts = line.split("|")
                    if (parts.size >= 2) {
                        runCatching {
                            val start = LocalDateTime.parse(parts[0])
                            val end = LocalDateTime.parse(parts[1])
                            val autoStopped = if (parts.size >= 3) parts[2].toBoolean() else false
                            sessions.add(TimeSession(start, end, autoStopped))
                        }
                    }
                }
            }
        }
    }
}
