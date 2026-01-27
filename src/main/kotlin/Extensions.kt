package com.timekeeper

import kotlin.time.Duration

fun Duration.format(): String {
    fun Number.pad() = toString().padStart(2, '0')
    return toComponents { hours, minutes, seconds, _ ->
        "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
    }
}
