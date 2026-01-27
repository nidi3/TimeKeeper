package com.timekeeper

import javax.swing.*
import java.awt.*
import java.time.format.DateTimeFormatter
import java.time.Duration

class OverviewWindow(private val timeTracker: TimeTracker) {
    private val frame: JFrame

    init {
        frame = JFrame("Time Keeper Overview")
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.setSize(600, 400)
        frame.setLocationRelativeTo(null)

        val panel = JPanel(BorderLayout())
        
        // Create tabs for daily and weekly views
        val tabbedPane = JTabbedPane()
        tabbedPane.addTab("Daily", createDailyPanel())
        tabbedPane.addTab("Weekly", createWeeklyPanel())
        
        panel.add(tabbedPane, BorderLayout.CENTER)
        
        frame.contentPane = panel
        frame.isVisible = true
    }

    private fun createDailyPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val sessions = timeTracker.getTodaySessions()
        val totalDuration = timeTracker.getTotalDuration(sessions)
        
        val textArea = JTextArea()
        textArea.isEditable = false
        textArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        
        val sb = StringBuilder()
        sb.append("TODAY'S SESSIONS\n")
        sb.append("=".repeat(50)).append("\n\n")
        
        if (sessions.isEmpty()) {
            sb.append("No sessions recorded today.\n")
        } else {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            sessions.forEach { session ->
                val start = session.startTime.format(formatter)
                val end = session.endTime.format(formatter)
                val duration = formatDuration(session.duration)
                val suffix = if (session.autoStopped) " [auto-stopped]" else ""
                sb.append("$start - $end  ($duration)$suffix\n")
            }
            sb.append("\n")
            sb.append("Total: ${formatDuration(totalDuration)}\n")
        }
        
        textArea.text = sb.toString()
        
        val scrollPane = JScrollPane(textArea)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        return panel
    }

    private fun createWeeklyPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val sessions = timeTracker.getWeeklySessions()
        val totalDuration = timeTracker.getTotalDuration(sessions)
        
        val textArea = JTextArea()
        textArea.isEditable = false
        textArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        
        val sb = StringBuilder()
        sb.append("LAST 7 DAYS\n")
        sb.append("=".repeat(50)).append("\n\n")
        
        if (sessions.isEmpty()) {
            sb.append("No sessions recorded in the last 7 days.\n")
        } else {
            // Group by date
            val sessionsByDate = sessions.groupBy { it.startTime.toLocalDate() }
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            
            sessionsByDate.toSortedMap().forEach { (date, daySessions) ->
                sb.append(date.format(dateFormatter)).append("\n")
                val dayTotal = timeTracker.getTotalDuration(daySessions)
                
                daySessions.forEach { session ->
                    val start = session.startTime.format(timeFormatter)
                    val end = session.endTime.format(timeFormatter)
                    val duration = formatDuration(session.duration)
                    val suffix = if (session.autoStopped) " [auto-stopped]" else ""
                    sb.append("  $start - $end  ($duration)$suffix\n")
                }
                sb.append("  Day total: ${formatDuration(dayTotal)}\n")
                sb.append("\n")
            }
            sb.append("Week total: ${formatDuration(totalDuration)}\n")
        }
        
        textArea.text = sb.toString()
        
        val scrollPane = JScrollPane(textArea)
        panel.add(scrollPane, BorderLayout.CENTER)
        
        return panel
    }

    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
