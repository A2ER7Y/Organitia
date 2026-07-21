package org.fossify.calendar.helpers

import android.content.Context
import org.fossify.calendar.extensions.cancelSubtaskAlarm
import org.fossify.calendar.extensions.scheduleSubtaskAlarm
import org.fossify.calendar.extensions.subtasksDB
import org.fossify.calendar.models.Subtask
import org.fossify.commons.helpers.ensureBackgroundThread

class SubtasksHelper(val context: Context) {
    private val subtasksDB = context.subtasksDB

    fun getSubtasksForTask(parentTaskId: Long): List<Subtask> = subtasksDB.getSubtasksForTask(parentTaskId)

    fun getSubtasksForTasks(parentTaskIds: List<Long>): List<Subtask> {
        if (parentTaskIds.isEmpty()) {
            return emptyList()
        }
        return subtasksDB.getSubtasksForTasks(parentTaskIds)
    }

    fun saveSubtasks(parentTaskId: Long, subtasks: List<Subtask>, callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            saveSubtasksSync(parentTaskId, subtasks)
            callback?.invoke()
        }
    }

    fun saveSubtasksSync(parentTaskId: Long, subtasks: List<Subtask>) {
        subtasksDB.deleteSubtasksForTask(parentTaskId)
        subtasks.forEachIndexed { index, subtask ->
            val saved = subtask.copy(
                id = null,
                parentTaskId = parentTaskId,
                sortOrder = index
            )
            val newId = subtasksDB.insertOrUpdate(saved)
            val persisted = saved.copy(id = newId)
            if (persisted.hasAlarm() && !persisted.isCompleted()) {
                context.scheduleSubtaskAlarm(persisted)
            } else {
                context.cancelSubtaskAlarm(newId)
            }
        }
    }

    fun scheduleAllSubtaskAlarms() {
        ensureBackgroundThread {
            subtasksDB.getSubtasksWithAlarms().forEach { subtask ->
                if (!subtask.isCompleted()) {
                    context.scheduleSubtaskAlarm(subtask)
                }
            }
        }
    }
}
