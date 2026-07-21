package org.fossify.calendar.models

data class Reminder(val seconds: Int, val type: Int) {
    @Deprecated("Use seconds", ReplaceWith("seconds"))
    val minutes: Int get() = seconds
}
