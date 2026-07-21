package org.fossify.calendar.interfaces

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.fossify.calendar.models.Subtask

@Dao
interface SubtasksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(subtask: Subtask): Long

    @Query("SELECT * FROM subtasks WHERE parent_task_id = :parentTaskId ORDER BY sort_order ASC, id ASC")
    fun getSubtasksForTask(parentTaskId: Long): List<Subtask>

    @Query("SELECT * FROM subtasks WHERE parent_task_id IN (:parentTaskIds) ORDER BY sort_order ASC, id ASC")
    fun getSubtasksForTasks(parentTaskIds: List<Long>): List<Subtask>

    @Query("SELECT * FROM subtasks WHERE id = :id")
    fun getSubtaskWithId(id: Long): Subtask?

    @Query("SELECT * FROM subtasks WHERE alarm_ts > 0")
    fun getSubtasksWithAlarms(): List<Subtask>

    @Query("DELETE FROM subtasks WHERE id = :id")
    fun deleteSubtask(id: Long)

    @Query("DELETE FROM subtasks WHERE parent_task_id = :parentTaskId")
    fun deleteSubtasksForTask(parentTaskId: Long)
}
