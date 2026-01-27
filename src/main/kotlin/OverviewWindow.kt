package com.timekeeper

import java.awt.*
import java.time.LocalDateTime
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
                frame.toFront()
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
        title = "TODAY'S SESSIONS",
        sessions = timeTracker.getTodaySessions(),
        emptyMessage = "No sessions recorded today."
    ) { sessions, runningSession ->
        sessions.forEach { append(it.format(it == runningSession)) }
        append("\nTotal: ${timeTracker.getTotalDuration(sessions).format()}\n")
    }

    private fun createWeeklyPanel() = createPanel(
        title = "LAST 7 DAYS",
        sessions = timeTracker.getWeeklySessions(),
        emptyMessage = "No sessions recorded in the last 7 days."
    ) { sessions, runningSession ->
        sessions.groupBy { it.startTime.toLocalDate() }.toSortedMap().forEach { (date, daySessions) ->
            append(date.formatDate()).append("\n")
            daySessions.forEach { append("  ").append(it.format(it == runningSession)) }
            append("  Day total: ${timeTracker.getTotalDuration(daySessions).format()}\n\n")
        }
        append("Week total: ${timeTracker.getTotalDuration(sessions).format()}\n")
    }

    private fun createPanel(
        title: String,
        sessions: List<TimeSession>,
        emptyMessage: String,
        renderSessions: StringBuilder.(List<TimeSession>, TimeSession?) -> Unit
    ): JPanel {
        val runningSession = if (currentState is TimerState.Running) TimeSession(currentState.startTime, LocalDateTime.now()) else null
        val allSessions = runningSession?.let { sessions + it } ?: sessions
        return JPanel(BorderLayout()).apply {
            add(JScrollPane(JTextArea().apply {
                isEditable = false
                font = Font(Font.MONOSPACED, Font.PLAIN, 12)
                text = buildString {
                    append("$title\n")
                    append("=".repeat(50)).append("\n\n")
                    if (allSessions.isEmpty()) append("$emptyMessage\n")
                    else renderSessions(allSessions, runningSession)
                }
            }), BorderLayout.CENTER)
        }
    }
}

private fun TimeSession.format(isRunning: Boolean = false): String {
    val endStr = if (isRunning) "...     " else endTime.formatTime()
    val suffix = when {
        isRunning -> " [running]"
        autoStopped -> " [auto-stopped]"
        else -> ""
    }
    return "${startTime.formatTime()} - $endStr (${duration.format()})$suffix\n"
}
