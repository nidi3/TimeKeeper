package guru.nidi.timekeeper

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)")

fun Duration.format(): String {
    fun Number.pad() = toString().padStart(2, '0')
    val rounded = inWholeMinutes + if (inWholeSeconds % 60 >= 30) 1 else 0
    return "${(rounded / 60).pad()}:${(rounded % 60).pad()}"
}

fun LocalDateTime.formatTime(): String = format(timeFormatter)
fun LocalDate.formatDate(): String = format(dateFormatter)

fun LocalDateTime.untilNow() = until(LocalDateTime.now())
fun LocalDateTime.until(time: LocalDateTime) = java.time.Duration.between(this, time).toKotlinDuration()
