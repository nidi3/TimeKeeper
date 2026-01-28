package com.timekeeper

import java.awt.*
import java.time.LocalDateTime
import javax.swing.SwingUtilities
import javax.swing.Timer
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

sealed class TimerState {
    data object Stopped : TimerState()
    data class Started(val startTime: LocalDateTime) : TimerState()
}

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
    }

    private var state: TimerState = TimerState.Stopped

    private val idleDetector = IdleDetector(IDLE_TIMEOUT)
    private val timeTracker = TimeTracker()
    private val overviewWindow = OverviewWindow(timeTracker)
    private val startStopItem = createStartStopItem()
    private val trayIcon = createTrayIcon()
    private val timer = createTimer()

    init {
        if (!SystemTray.isSupported()) {
            showError("System tray is not supported")
            exitProcess(1)
        }
        runCatching {
            SystemTray.getSystemTray().add(trayIcon)
        }.onFailure {
            showError("Failed to add tray icon")
            exitProcess(1)
        }
    }

    private fun createStartStopItem() = MenuItem("Start").apply {
        addActionListener {
            when (state) {
                is TimerState.Started -> stopTimer()
                is TimerState.Stopped -> startTimer()
            }
        }
    }

    private fun createTrayIcon() = TrayIcon(TrayIcons.stopped, tooltip(), createPopupMenu()).apply {
        isImageAutoSize = true
    }

    private fun createPopupMenu() = PopupMenu().apply {
        add(startStopItem)
        add(MenuItem("Show Overview").apply {
            addActionListener { overviewWindow.show(state) }
        })
        addSeparator()
        add(MenuItem("Exit").apply {
            addActionListener { exit() }
        })
    }

    private fun exit() {
        stopTimer()
        SystemTray.getSystemTray().remove(trayIcon)
        exitProcess(0)
    }

    private fun createTimer() = Timer(TIMER_INTERVAL.inWholeMilliseconds.toInt()) {
        when (val s = state) {
            is TimerState.Started -> {
                val lastActiveTime = idleDetector.lastActiveTime(s.startTime)
                if (lastActiveTime != null) {
                    stopTimer(lastActiveTime)
                } else {
                    trayIcon.toolTip = tooltip()
                }
            }

            is TimerState.Stopped -> {}
        }
    }

    private fun startTimer() {
        state = TimerState.Started(LocalDateTime.now())
        idleDetector.reset()
        timer.start()
        trayIcon.image = TrayIcons.started
        trayIcon.toolTip = tooltip()
        startStopItem.label = "Stop"
        overviewWindow.update(state)
    }

    private fun stopTimer(endTime: LocalDateTime? = null) {
        when (val s = state) {
            is TimerState.Started -> {
                timeTracker.addSession(s.startTime, endTime)
                state = TimerState.Stopped
                timer.stop()
                trayIcon.image = TrayIcons.stopped
                trayIcon.toolTip = tooltip()
                startStopItem.label = "Start"
                overviewWindow.update(state)
            }

            is TimerState.Stopped -> {}
        }
    }

    private fun tooltip() = when (val s = state) {
        is TimerState.Started -> s.startTime.untilNow().format()
        is TimerState.Stopped -> "Stopped"
    }
}
