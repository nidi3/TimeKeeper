 package com.timekeeper

import java.awt.*
import java.awt.image.BufferedImage
import java.time.LocalDateTime
import javax.swing.*
import kotlin.system.exitProcess
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun main() {
    System.setProperty("apple.awt.UIElement", "true")
    SwingUtilities.invokeLater {
        TimeKeeperApp()
    }
}

class TimeKeeperApp {
    companion object {
        private val IDLE_TIMEOUT = 30.seconds
        private val TIMER_INTERVAL = 1.seconds
        private const val ICON_SIZE = 22
    }

    private val timeTracker = TimeTracker()
    private val stoppedIcon = createStoppedIcon()
    private val runningIcon = createRunningIcon()
    private val trayIcon: TrayIcon
    private val timer: Timer
    private var startTime: LocalDateTime? = null
    private var elapsed = Duration.ZERO
    private var isRunning = false
    private var lastCheckTime = System.currentTimeMillis()

    init {
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null, "System tray is not supported", "Error", JOptionPane.ERROR_MESSAGE)
            exitProcess(1)
        }

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

        trayIcon = TrayIcon(stoppedIcon, Duration.ZERO.format(), popup).apply {
            isImageAutoSize = true
        }

        runCatching {
            SystemTray.getSystemTray().add(trayIcon)
        }.onFailure {
            JOptionPane.showMessageDialog(null, "Failed to add tray icon", "Error", JOptionPane.ERROR_MESSAGE)
            exitProcess(1)
        }

        timer = Timer(TIMER_INTERVAL.inWholeMilliseconds.toInt()) {
            val currentTime = System.currentTimeMillis()
            val timeDiff = currentTime - lastCheckTime

            if (timeDiff > IDLE_TIMEOUT.inWholeMilliseconds && isRunning) {
                stopTimer(autoStopped = true, endTime = LocalDateTime.now().minusSeconds(timeDiff / 1000))
                startStopItem.label = "Start"
            }

            if (isRunning) {
                elapsed += 1.seconds
                updateDisplay()
            }

            lastCheckTime = currentTime
        }
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
        elapsed = Duration.ZERO
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
            elapsed = Duration.ZERO
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        trayIcon.toolTip = elapsed.format()
    }

    private fun showOverview() = OverviewWindow(timeTracker)
}
