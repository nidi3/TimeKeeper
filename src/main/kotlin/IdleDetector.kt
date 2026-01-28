package com.timekeeper

import java.time.LocalDateTime
import kotlin.time.Duration

class IdleDetector(
    private val timeout: Duration,
    private val clock: () -> LocalDateTime = { LocalDateTime.now() }
) {
    private var lastCheckTime: LocalDateTime = LocalDateTime.MIN

    fun reset() {
        lastCheckTime = clock()
    }

    fun lastActiveTime(startTime: LocalDateTime): LocalDateTime? {
        val previousCheckTime = lastCheckTime
        lastCheckTime = clock()
        return if (previousCheckTime.until(lastCheckTime) > timeout) maxOf(startTime, previousCheckTime) else null
    }
}
