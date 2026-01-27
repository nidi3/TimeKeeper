package com.timekeeper

import javax.swing.*
import java.awt.*
import java.time.format.DateTimeFormatter
import java.time.Duration

class OverviewWindow(private val timeTracker: TimeTracker) {
    private val frame: JFrame

    init {
        frame = JFrame("Time Keeper Overview").apply {
            defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            setSize(600, 400)
            setLocationRelativeTo(null)
            contentPane = JPanel(BorderLayout()).apply {
                add(JTabbedPane().apply {
                    addTab("Daily", createDailyPanel())
                    addTab("Weekly", createWeeklyPanel())
                }, BorderLayout.CENTER)
            }
            isVisible = true
        }
    }

    private fun createDailyPanel() = JPanel(BorderLayout()).apply {
        val sessions = timeTracker.getTodaySessions()
        add(JScrollPane(JTextArea().apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            text = buildString {
                append("TODAY'S SESSIONS\n")
                append("=".repeat(50)).append("\n\n")
                if (sessions.isEmpty()) {
                    append("No sessions recorded today.\n")
                } else {
                    DateTimeFormatter.ofPattern("HH:mm:ss").let { formatter ->
                        sessions.forEach { session ->
                            append("${session.startTime.format(formatter)} - ${session.endTime.format(formatter)}  ")
                            append("(${formatDuration(session.duration)})")
                            if (session.autoStopped) append(" [auto-stopped]")
                            append("\n")
                        }
                    }
                    append("\nTotal: ${formatDuration(timeTracker.getTotalDuration(sessions))}\n")
                }
            }
        }), BorderLayout.CENTER)
    }

    private fun createWeeklyPanel() = JPanel(BorderLayout()).apply {
        val sessions = timeTracker.getWeeklySessions()
        add(JScrollPane(JTextArea().apply {
            isEditable = false
            font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            text = buildString {
                append("LAST 7 DAYS\n")
                append("=".repeat(50)).append("\n\n")
                if (sessions.isEmpty()) {
                    append("No sessions recorded in the last 7 days.\n")
                } else {
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                    sessions.groupBy { it.startTime.toLocalDate() }.toSortedMap().forEach { (date, daySessions) ->
                        append(date.format(dateFormatter)).append("\n")
                        daySessions.forEach { session ->
                            append("  ${session.startTime.format(timeFormatter)} - ${session.endTime.format(timeFormatter)}  ")
                            append("(${formatDuration(session.duration)})")
                            if (session.autoStopped) append(" [auto-stopped]")
                            append("\n")
                        }
                        append("  Day total: ${formatDuration(timeTracker.getTotalDuration(daySessions))}\n\n")
                    }
                    append("Week total: ${formatDuration(timeTracker.getTotalDuration(sessions))}\n")
                }
            }
        }), BorderLayout.CENTER)
    }

    private fun formatDuration(duration: Duration) = String.format(
        "%02d:%02d:%02d",
        duration.toHours(),
        duration.toMinutes() % 60,
        duration.seconds % 60
    )
}
