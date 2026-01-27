package com.timekeeper

import java.time.*

class SessionFormatter(private val timeTracker: TimeTracker) {
    fun buildDailyText(state: TimerState) = buildSessionsText(
        state = state,
        title = "TODAY",
        sessions = timeTracker.getTodaySessions()
    ) { sessions ->
        appendDay(LocalDate.now(), sessions)
    }

    fun buildWeeklyText(state: TimerState) = buildSessionsText(
        state = state,
        title = "THIS WEEK",
        sessions = timeTracker.getWeekSessions()
    ) { sessions ->
        val today = LocalDate.now()
        val sessionsByDate = sessions.groupBy { it.startTime.toLocalDate() }
        generateSequence(today.with(DayOfWeek.MONDAY)) { it.plusDays(1) }
            .takeWhile { it <= today }
            .forEach { appendDay(it, sessionsByDate[it] ?: emptyList()).also { append("\n") } }
        append("${"Week".padStart(15)} (${timeTracker.getTotalDuration(sessions).format()})\n")
    }

    private fun buildSessionsText(
        state: TimerState,
        title: String,
        sessions: List<TimeSession>,
        renderSessions: StringBuilder.(List<TimeSession>) -> Unit
    ) = buildString {
        val allSessions = when (state) {
            is TimerState.Started -> sessions + TimeSession(state.startTime, LocalDateTime.now(), inProgress = true)
            is TimerState.Stopped -> sessions
        }
        append("$title\n")
        append("=".repeat(50)).append("\n\n")
        renderSessions(allSessions)
    }

    private fun StringBuilder.appendDay(date: LocalDate, sessions: List<TimeSession>) {
        append(date.formatDate()).append("\n")
        sessions.forEach { append("  ").append(it.format()) }
        append("  ${"Day".padStart(13)} (${timeTracker.getTotalDuration(sessions).format()})\n")
    }
}

private fun TimeSession.format(): String {
    val endStr = if (inProgress) "...  " else endTime.formatTime()
    val suffix = when {
        inProgress -> " [in progress]"
        autoStopped -> " [auto-stopped]"
        else -> ""
    }
    return "${startTime.formatTime()} - $endStr (${duration.format()})$suffix\n"
}
