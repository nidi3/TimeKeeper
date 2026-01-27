package com.timekeeper

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JOptionPane
import kotlin.time.Duration

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)")

fun Duration.format(): String {
    fun Number.pad() = toString().padStart(2, '0')
    return toComponents { hours, minutes, seconds, _ ->
        "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
    }
}

fun LocalDateTime.formatTime(): String = format(timeFormatter)
fun LocalDate.formatDate(): String = format(dateFormatter)

fun showError(message: String) =
    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
