package org.fossify.calendar.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.fossify.calendar.helpers.FLAG_SUBTASK_COMPLETED
import org.fossify.commons.extensions.addBitIf
import java.io.Serializable

@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = Event::class,
            parentColumns = ["id"],
            childColumns = ["parent_task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["parent_task_id"])]
)
data class Subtask(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "parent_task_id") var parentTaskId: Long,
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "sort_order") var sortOrder: Int = 0,
    @ColumnInfo(name = "flags") var flags: Int = 0,
    @ColumnInfo(name = "alarm_ts") var alarmTS: Long = 0L,
) : Serializable {

    companion object {
        private const val serialVersionUID = -32456795132345617L
    }

    fun isCompleted() = flags and FLAG_SUBTASK_COMPLETED != 0

    fun setCompleted(completed: Boolean) {
        flags = flags.addBitIf(FLAG_SUBTASK_COMPLETED, completed)
    }

    fun hasAlarm() = alarmTS > 0L
}
