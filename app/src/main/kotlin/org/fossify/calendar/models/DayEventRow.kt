package org.fossify.calendar.models

data class DayEventRow(
    val event: Event,
    val subtask: Subtask? = null,
    val isSubtask: Boolean = false,
)
