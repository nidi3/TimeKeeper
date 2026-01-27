package com.timekeeper

import javax.swing.JOptionPane
import kotlin.time.Duration

fun Duration.format(): String {
    fun Number.pad() = toString().padStart(2, '0')
    return toComponents { hours, minutes, seconds, _ ->
        "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
    }
}

fun showError(message: String) =
    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
