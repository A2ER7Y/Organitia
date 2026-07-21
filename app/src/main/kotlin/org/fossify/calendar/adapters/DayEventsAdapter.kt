package org.fossify.calendar.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import org.fossify.calendar.R
import org.fossify.calendar.activities.SimpleActivity
import org.fossify.calendar.databinding.EventListItemBinding
import org.fossify.calendar.dialogs.DeleteEventDialog
import org.fossify.calendar.extensions.*
import org.fossify.calendar.helpers.Formatter
import org.fossify.calendar.models.DayEventRow
import org.fossify.calendar.models.Event
import org.fossify.commons.adapters.MyRecyclerViewAdapter
import org.fossify.commons.extensions.adjustAlpha
import org.fossify.commons.extensions.applyColorFilter
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.helpers.MEDIUM_ALPHA
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.views.MyRecyclerView

class DayEventsAdapter(
    activity: SimpleActivity,
    val rows: ArrayList<DayEventRow>,
    recyclerView: MyRecyclerView,
    var dayCode: String,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private val allDayString = resources.getString(R.string.all_day)
    private val displayDescription = activity.config.displayDescription
    private val replaceDescriptionWithLocation = activity.config.replaceDescription
    private val dimPastEvents = activity.config.dimPastEvents
    private val dimCompletedTasks = activity.config.dimCompletedTasks
    private var isPrintVersion = false
    private val mediumMargin = activity.resources.getDimension(org.fossify.commons.R.dimen.medium_margin).toInt()
    private val subtaskMargin = activity.resources.getDimension(org.fossify.commons.R.dimen.activity_margin).toInt()

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_day

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_share -> shareEvents()
            R.id.cab_delete -> askConfirmDelete()
        }
    }

    override fun getSelectableItemCount() = rows.count { !it.isSubtask }

    override fun getIsItemSelectable(position: Int) = !rows[position].isSubtask

    override fun getItemSelectionKey(position: Int) = rows.getOrNull(position)?.takeIf { !it.isSubtask }?.event?.id?.toInt()

    override fun getItemKeyPosition(key: Int) = rows.indexOfFirst { !it.isSubtask && it.event.id?.toInt() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(
            view = EventListItemBinding.inflate(activity.layoutInflater, parent, false).root
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = rows[position]
        holder.bindView(row, allowSingleClick = !row.isSubtask, allowLongClick = !row.isSubtask) { itemView, _ ->
            if (row.isSubtask && row.subtask != null) {
                setupSubtaskView(itemView, row)
            } else {
                setupView(itemView, row.event)
            }
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = rows.size

    fun togglePrintMode() {
        isPrintVersion = !isPrintVersion
        textColor = if (isPrintVersion) {
            resources.getColor(org.fossify.commons.R.color.theme_light_text_color)
        } else {
            activity.getProperTextColor()
        }

        notifyDataSetChanged()
    }

    private fun setupView(view: View, event: Event) {
        EventListItemBinding.bind(view).apply {
            eventItemHolder.isSelected = selectedKeys.contains(event.id?.toInt())
            eventItemHolder.background.applyColorFilter(textColor)
            eventItemTitle.text = event.title
            eventItemTitle.checkViewStrikeThrough(event.shouldStrikeThrough())
            eventItemTime.text = if (event.getIsAllDay()) allDayString else Formatter.getTimeFromTS(activity, event.startTS)
            if (event.startTS != event.endTS) {
                val startDayCode = Formatter.getDayCodeFromTS(event.startTS)
                val endDayCode = Formatter.getDayCodeFromTS(event.endTS)
                val startDate = Formatter.getDayTitle(activity, startDayCode, false)
                val endDate = Formatter.getDayTitle(activity, endDayCode, false)
                val startDayString = if (startDayCode != dayCode) " ($startDate)" else ""
                if (!event.getIsAllDay()) {
                    val endTimeString = Formatter.getTimeFromTS(activity, event.endTS)
                    val endDayString = if (endDayCode != dayCode) " ($endDate)" else ""
                    eventItemTime.text = "${eventItemTime.text}$startDayString - $endTimeString$endDayString"
                } else {
                    val endDayString = if (endDayCode != dayCode) " - ($endDate)" else ""
                    eventItemTime.text = "${eventItemTime.text}$startDayString$endDayString"
                }
            }

            eventItemDescription.text = if (replaceDescriptionWithLocation) event.location else event.description.replace("\n", " ")
            eventItemDescription.beVisibleIf(displayDescription && eventItemDescription.text.isNotEmpty())
            eventItemColorBar.background.applyColorFilter(event.color)

            var newTextColor = textColor

            val adjustAlpha = if (event.isTask()) {
                dimCompletedTasks && event.isTaskCompleted()
            } else {
                dimPastEvents && event.isPastEvent && !isPrintVersion
            }

            if (adjustAlpha) {
                newTextColor = newTextColor.adjustAlpha(MEDIUM_ALPHA)
            }

            eventItemTime.setTextColor(newTextColor)
            eventItemTitle.setTextColor(newTextColor)
            eventItemDescription.setTextColor(newTextColor)
            eventItemTaskImage.applyColorFilter(newTextColor)
            eventItemTaskImage.beVisibleIf(event.isTask())

            val startMargin = if (event.isTask()) {
                0
            } else {
                mediumMargin
            }

            (eventItemTitle.layoutParams as ConstraintLayout.LayoutParams).marginStart = startMargin
        }
    }

    private fun setupSubtaskView(view: View, row: DayEventRow) {
        val subtask = row.subtask ?: return
        EventListItemBinding.bind(view).apply {
            eventItemHolder.isSelected = false
            eventItemHolder.background.applyColorFilter(textColor)
            eventItemTitle.text = subtask.title
            eventItemTitle.checkViewStrikeThrough(subtask.isCompleted())
            eventItemTime.text = if (subtask.hasAlarm()) {
                Formatter.getTimeWithSecondsFromTS(activity, subtask.alarmTS)
            } else {
                ""
            }
            eventItemDescription.text = activity.getString(R.string.subtask)
            eventItemDescription.beVisibleIf(true)
            eventItemColorBar.background.applyColorFilter(row.event.color)
            eventItemTaskImage.beVisibleIf(true)
            eventItemTaskImage.applyColorFilter(textColor)

            var newTextColor = textColor
            if (subtask.isCompleted()) {
                newTextColor = newTextColor.adjustAlpha(MEDIUM_ALPHA)
            }

            eventItemTime.setTextColor(newTextColor)
            eventItemTitle.setTextColor(newTextColor)
            eventItemDescription.setTextColor(newTextColor)
            (eventItemTitle.layoutParams as ConstraintLayout.LayoutParams).marginStart = subtaskMargin
        }
    }

    private fun shareEvents() = activity.shareEvents(selectedKeys.distinct().map { it.toLong() })

    private fun askConfirmDelete() {
        val eventIds = selectedKeys.map { it.toLong() }.toMutableList()
        val eventsToDelete = rows.filter { !it.isSubtask && selectedKeys.contains(it.event.id?.toInt()) }.map { it.event }
        val timestamps = eventsToDelete.map { it.startTS }
        val positions = getSelectedItemPositions()

        val hasRepeatableEvent = eventsToDelete.any { it.repeatInterval > 0 }
        DeleteEventDialog(activity, eventIds, hasRepeatableEvent) { it ->
            rows.removeAll { row -> !row.isSubtask && eventsToDelete.contains(row.event) }

            ensureBackgroundThread {
                val nonRepeatingEventIDs = eventsToDelete.asSequence().filter { it.repeatInterval == 0 }.mapNotNull { it.id }.toMutableList()
                activity.eventsHelper.deleteEvents(nonRepeatingEventIDs, true)

                val repeatingEventIDs = eventsToDelete.asSequence().filter { it.repeatInterval != 0 }.mapNotNull { it.id }.toList()
                activity.handleEventDeleting(repeatingEventIDs, timestamps, it)
                activity.runOnUiThread {
                    removeSelectedItems(positions)
                }
            }
        }
    }
}
