package com.timekeeper

import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*

class OverviewWindow(timeTracker: TimeTracker) {
    private val formatter = SessionFormatter(timeTracker)
    private val dailyText = createTextArea()
    private val weeklyText = createTextArea()
    private val frame = JFrame("Time Keeper Overview").apply {
        defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        setSize(600, 400)
        setLocationRelativeTo(null)
        contentPane = JPanel(BorderLayout()).apply {
            add(JTabbedPane().apply {
                addTab("Daily", JScrollPane(dailyText))
                addTab("Weekly", JScrollPane(weeklyText))
            }, BorderLayout.CENTER)
        }
    }

    fun show(state: TimerState) {
        update(state)
        frame.apply {
            isVisible = true
            isAlwaysOnTop = true
            requestFocus()
        }
    }

    fun update(state: TimerState) {
        dailyText.text = formatter.buildDailyText(state)
        weeklyText.text = formatter.buildWeeklyText(state)
    }

    private fun createTextArea() = JTextArea().apply {
        isEditable = false
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
    }
}
