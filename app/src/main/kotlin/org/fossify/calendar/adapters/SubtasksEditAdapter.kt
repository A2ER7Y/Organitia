package org.fossify.calendar.adapters

import android.app.TimePickerDialog
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import org.fossify.calendar.R
import org.fossify.calendar.activities.SimpleActivity
import org.fossify.calendar.databinding.ItemSubtaskEditBinding
import org.fossify.calendar.extensions.getFormattedReminderOffset
import org.fossify.calendar.helpers.Formatter
import org.fossify.calendar.models.Subtask
import org.fossify.commons.adapters.MyRecyclerViewAdapter
import org.fossify.commons.extensions.getTimePickerDialogTheme
import org.fossify.commons.extensions.updateTextColors
import org.fossify.commons.views.MyRecyclerView
import org.joda.time.DateTime

class SubtasksEditAdapter(
    activity: SimpleActivity,
    private val subtasks: ArrayList<Subtask>,
    recyclerView: MyRecyclerView,
    private val dayCode: String,
) : MyRecyclerViewAdapter(activity, recyclerView, null) {

    override fun getActionMenuId() = 0

    override fun prepareActionMode(menu: android.view.Menu) {}

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = 0

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemSelectionKey(position: Int) = null

    override fun getItemKeyPosition(key: Int) = -1

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubtaskEditBinding.inflate(activity.layoutInflater, parent, false)
        return createViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subtask = subtasks[position]
        holder.bindView(subtask, allowSingleClick = false, allowLongClick = false) { itemView, _ ->
            ItemSubtaskEditBinding.bind(itemView).apply {
                activity.updateTextColors(root)
                subtaskCompleted.isChecked = subtask.isCompleted()
                subtaskTitle.setText(subtask.title)
                subtaskAlarm.text = if (subtask.hasAlarm()) {
                    Formatter.getTimeWithSecondsFromTS(activity, subtask.alarmTS)
                } else {
                    activity.getString(R.string.set_alarm)
                }

                subtaskCompleted.setOnCheckedChangeListener { _, isChecked ->
                    subtask.setCompleted(isChecked)
                }

                subtaskTitle.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        subtask.title = subtaskTitle.text.toString().trim()
                    }
                }

                subtaskAlarm.setOnClickListener {
                    val current = if (subtask.hasAlarm()) {
                        Formatter.getDateTimeFromTS(subtask.alarmTS)
                    } else {
                        Formatter.getDateTimeFromCode(dayCode)
                    }
                    TimePickerDialog(
                        activity,
                        activity.getTimePickerDialogTheme(),
                        { _: TimePicker, hour: Int, minute: Int ->
                            val baseDate = Formatter.getDateTimeFromCode(dayCode)
                                .withTime(hour, minute, 0, 0)
                            showSecondPicker(baseDate.secondOfMinute) { second ->
                                val alarmDate = baseDate.withSecondOfMinute(second)
                                subtask.alarmTS = alarmDate.seconds()
                                subtaskAlarm.text = Formatter.getTimeWithSecondsFromTS(activity, subtask.alarmTS)
                            }
                        },
                        current.hourOfDay,
                        current.minuteOfHour,
                        activity.config.use24HourFormat
                    ).show()
                }

                subtaskDelete.setOnClickListener {
                    val index = subtasks.indexOf(subtask)
                    if (index != -1) {
                        subtasks.removeAt(index)
                        notifyItemRemoved(index)
                    }
                }
            }
        }
    }

    override fun getItemCount() = subtasks.size

    fun addSubtask() {
        subtasks.add(Subtask(id = null, parentTaskId = 0))
        notifyItemInserted(subtasks.lastIndex)
    }

    fun collectSubtasks(): List<Subtask> {
        return subtasks.mapIndexed { index, subtask ->
            val binding = recyclerView.findViewHolderForAdapterPosition(index)
            if (binding != null) {
                val itemBinding = ItemSubtaskEditBinding.bind(binding.itemView)
                subtask.copy(
                    title = itemBinding.subtaskTitle.text.toString().trim(),
                )
            } else {
                subtask
            }
        }.filter { it.title.isNotEmpty() }
    }

    private fun showSecondPicker(currentSecond: Int, callback: (Int) -> Unit) {
        val picker = NumberPicker(activity).apply {
            minValue = 0
            maxValue = 59
            value = currentSecond
        }
        AlertDialog.Builder(activity)
            .setTitle(R.string.pick_seconds)
            .setView(picker)
            .setPositiveButton(org.fossify.commons.R.string.ok) { _, _ -> callback(picker.value) }
            .setNegativeButton(org.fossify.commons.R.string.cancel, null)
            .show()
    }
}
