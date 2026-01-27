 package com.timekeeper

import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.time.LocalDateTime
import javax.swing.*
import kotlin.system.exitProcess

fun main() {
    System.setProperty("apple.awt.UIElement", "true")
    SwingUtilities.invokeLater {
        TimeKeeperApp()
    }
}

class TimeKeeperApp {
    companion object {
        private const val IDLE_TIMEOUT_MS = 30000L
        private const val TIMER_INTERVAL_MS = 1000
        private const val ICON_SIZE = 22
    }

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
            JOptionPane.showMessageDialog(null, "System tray is not supported", "Error", JOptionPane.ERROR_MESSAGE)
            exitProcess(1)
        }

        timeTracker = TimeTracker()

        stoppedIcon = createStoppedIcon()
        runningIcon = createRunningIcon()

        val startStopItem = MenuItem("Start").apply {
            addActionListener {
                if (isRunning) {
                    stopTimer(autoStopped = false)
                    label = "Start"
                } else {
                    startTimer()
                    label = "Stop"
                }
            }
        }

        val popup = PopupMenu().apply {
            add(startStopItem)
            add(MenuItem("Show Overview").apply {
                addActionListener { showOverview() }
            })
            addSeparator()
            add(MenuItem("Exit").apply {
                addActionListener {
                    SystemTray.getSystemTray().remove(trayIcon)
                    exitProcess(0)
                }
            })
        }

        trayIcon = TrayIcon(stoppedIcon, "00:00:00", popup).apply {
            isImageAutoSize = true
        }

        runCatching {
            SystemTray.getSystemTray().add(trayIcon)
        }.onFailure {
            JOptionPane.showMessageDialog(null, "Failed to add tray icon", "Error", JOptionPane.ERROR_MESSAGE)
            exitProcess(1)
        }

        timer = Timer(TIMER_INTERVAL_MS, object : ActionListener {
            private var lastCheckTime = System.currentTimeMillis()

            override fun actionPerformed(e: ActionEvent) {
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - lastCheckTime

                if (timeDiff > IDLE_TIMEOUT_MS && isRunning) {
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

    private fun createStoppedIcon() = createIcon(ICON_SIZE) { g, size ->
        val triangleSize = 10
        val startX = (size - triangleSize) / 2 + 2
        val startY = (size - triangleSize) / 2
        g.fillPolygon(
            intArrayOf(startX, startX + triangleSize, startX),
            intArrayOf(startY, size / 2, startY + triangleSize),
            3
        )
    }

    private fun createRunningIcon() = createIcon(ICON_SIZE) { g, size ->
        val barWidth = 3
        val barHeight = 12
        val spacing = 3
        val startX = (size - barWidth * 2 - spacing) / 2
        val startY = (size - barHeight) / 2
        g.fillRect(startX, startY, barWidth, barHeight)
        g.fillRect(startX + barWidth + spacing, startY, barWidth, barHeight)
    }

    private fun createIcon(size: Int, draw: (Graphics2D, Int) -> Unit) =
        BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB).also { image ->
            image.createGraphics().apply {
                setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                color = Color.WHITE
                draw(this, size)
                dispose()
            }
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
            timeTracker.addSession(TimeSession(startTime!!, endTime ?: LocalDateTime.now(), autoStopped))
            isRunning = false
            trayIcon.image = stoppedIcon
            timer.stop()
            elapsedSeconds = 0
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        trayIcon.toolTip = String.format(
            "%02d:%02d:%02d",
            elapsedSeconds / 3600,
            (elapsedSeconds % 3600) / 60,
            elapsedSeconds % 60
        )
    }

    private fun showOverview() = OverviewWindow(timeTracker)
}
