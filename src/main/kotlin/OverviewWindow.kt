package com.timekeeper

import java.awt.*
import java.time.*
import javax.swing.*

class OverviewWindow private constructor(private val timeTracker: TimeTracker) {
    private val frame = JFrame("Time Keeper Overview").apply {
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        setSize(600, 400)
        setLocationRelativeTo(null)
    }
    private var currentState: TimerState = TimerState.Stopped

    companion object {
        private lateinit var instance: OverviewWindow

        fun show(timeTracker: TimeTracker, state: TimerState) {
            if (!::instance.isInitialized) instance = OverviewWindow(timeTracker)
            instance.apply {
                currentState = state
                refresh()
                frame.isVisible = true
                frame.extendedState = JFrame.NORMAL
                frame.isAlwaysOnTop = true
                frame.requestFocus()
            }
        }
    }

    private fun refresh() {
        frame.contentPane = JPanel(BorderLayout()).apply {
            add(JTabbedPane().apply {
                addTab("Daily", createDailyPanel())
                addTab("Weekly", createWeeklyPanel())
            }, BorderLayout.CENTER)
        }
    }

    private fun createDailyPanel() = createPanel(
        title = "TODAY",
        sessions = timeTracker.getTodaySessions()
    ) { sessions ->
        appendDay(LocalDate.now(), sessions)
    }

    private fun createWeeklyPanel() = createPanel(
        title = "THIS WEEK",
        sessions = timeTracker.getWeekSessions()
    ) { sessions ->
        val today = LocalDate.now()
        val sessionsByDate = sessions.groupBy { it.startTime.toLocalDate() }
        generateSequence(today.with(DayOfWeek.MONDAY)) { it.plusDays(1) }
            .takeWhile { it <= today }
            .forEach { appendDay(it, sessionsByDate[it] ?: emptyList()).also { append("\n") } }
        append("Week total: ${timeTracker.getTotalDuration(sessions).format()}\n")
    }

    private fun StringBuilder.appendDay(date: LocalDate, sessions: List<TimeSession>) {
        append(date.formatDate()).append("\n")
        sessions.forEach { append("  ").append(it.format()) }
        append("  Day total: ${timeTracker.getTotalDuration(sessions).format()}\n")
    }

    private fun createPanel(
        title: String,
        sessions: List<TimeSession>,
        renderSessions: StringBuilder.(List<TimeSession>) -> Unit
    ): JPanel {
        val allSessions = when (val s = currentState) {
            is TimerState.Running -> sessions + TimeSession(s.startTime, LocalDateTime.now(), running = true)
            is TimerState.Stopped -> sessions
        }
        return JPanel(BorderLayout()).apply {
            add(JScrollPane(JTextArea().apply {
                isEditable = false
                font = Font(Font.MONOSPACED, Font.PLAIN, 12)
                text = buildString {
                    append("$title\n")
                    append("=".repeat(50)).append("\n\n")
                    renderSessions(allSessions)
                }
            }), BorderLayout.CENTER)
        }
    }
}

private fun TimeSession.format(): String {
    val endStr = if (running) "...  " else endTime.formatTime()
    val suffix = when {
        running -> " [running]"
        autoStopped -> " [auto-stopped]"
        else -> ""
    }
    return "${startTime.formatTime()} - $endStr (${duration.format()})$suffix\n"
}
