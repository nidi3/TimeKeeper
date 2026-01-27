 package com.timekeeper

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*
import java.time.LocalDateTime

fun main() {
    SwingUtilities.invokeLater {
        TimeKeeperApp()
    }
}

class TimeKeeperApp {
    private val trayIcon: TrayIcon
    private val timer: Timer
    private val timeTracker: TimeTracker
    private var startTime: LocalDateTime? = null
    private var elapsedSeconds: Long = 0
    private var isRunning = false
    private val stoppedIcon: Image
    private val runningIcon: Image

    init {
        if (!SystemTray.isSupported()) {
            println("System tray is not supported")
            System.exit(1)
        }

        timeTracker = TimeTracker()

        // Create menu bar icons
        stoppedIcon = createStoppedIcon()
        runningIcon = createRunningIcon()

        val popup = PopupMenu()
        trayIcon = TrayIcon(stoppedIcon, "00:00:00", popup)
        trayIcon.isImageAutoSize = true

        // Create menu items
        val startStopItem = MenuItem("Start")
        val overviewItem = MenuItem("Show Overview")
        val exitItem = MenuItem("Exit")

        // Start/Stop action
        startStopItem.addActionListener {
            if (isRunning) {
                stopTimer(autoStopped = false)
                startStopItem.label = "Start"
            } else {
                startTimer()
                startStopItem.label = "Stop"
            }
        }

        // Overview action
        overviewItem.addActionListener {
            showOverview()
        }

        // Exit action
        exitItem.addActionListener {
            SystemTray.getSystemTray().remove(trayIcon)
            System.exit(0)
        }

        popup.add(startStopItem)
        popup.add(overviewItem)
        popup.addSeparator()
        popup.add(exitItem)

        // Add to system tray
        val tray = SystemTray.getSystemTray()
        try {
            tray.add(trayIcon)
        } catch (e: AWTException) {
            println("TrayIcon could not be added.")
            System.exit(1)
        }

        // Timer to update display and check sleep state
        timer = Timer(1000, object : ActionListener {
            private var lastCheckTime = System.currentTimeMillis()

            override fun actionPerformed(e: ActionEvent) {
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - lastCheckTime

                // If more than 30 seconds passed, system likely woke from sleep
                if (timeDiff > 30000 && isRunning) {
                    // Auto-stop timer when waking from sleep, using the last check time as end time
                    stopTimer(autoStopped = true, endTime = LocalDateTime.now().minusSeconds(timeDiff / 1000))
                    val menuItem = (popup.getItem(0) as MenuItem)
                    menuItem.label = "Start"
                }

                if (isRunning) {
                    elapsedSeconds++
                    updateDisplay()
                }

                lastCheckTime = currentTime
            }
        })
    }

    private fun createStoppedIcon(): Image {
        val size = 22
        val image = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw pause icon - two vertical bars
        g.color = Color.WHITE
        val barWidth = 3
        val barHeight = 12
        val spacing = 3
        val startX = (size - barWidth * 2 - spacing) / 2
        val startY = (size - barHeight) / 2

        g.fillRect(startX, startY, barWidth, barHeight)
        g.fillRect(startX + barWidth + spacing, startY, barWidth, barHeight)

        g.dispose()
        return image
    }

    private fun createRunningIcon(): Image {
        val size = 22
        val image = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw play icon - triangle pointing right
        g.color = Color.WHITE
        val triangleSize = 10
        val startX = (size - triangleSize) / 2 + 2
        val startY = (size - triangleSize) / 2

        val xPoints = intArrayOf(startX, startX + triangleSize, startX)
        val yPoints = intArrayOf(startY, size / 2, startY + triangleSize)
        g.fillPolygon(xPoints, yPoints, 3)

        g.dispose()
        return image
    }

    private fun startTimer() {
        startTime = LocalDateTime.now()
        elapsedSeconds = 0
        isRunning = true
        trayIcon.image = runningIcon
        timer.start()
        updateDisplay()
    }

    private fun stopTimer(autoStopped: Boolean = false, endTime: LocalDateTime? = null) {
        if (isRunning && startTime != null) {
            val actualEndTime = endTime ?: LocalDateTime.now()
            timeTracker.addSession(TimeSession(startTime!!, actualEndTime, autoStopped))
            isRunning = false
            trayIcon.image = stoppedIcon
            timer.stop()
            elapsedSeconds = 0
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60
        val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        trayIcon.toolTip = timeString
    }

    private fun showOverview() {
        OverviewWindow(timeTracker)
    }
}
