package com.example.util.simpletimetracker.feature_change_reminder.view

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_change_reminder.R
import com.example.util.simpletimetracker.feature_change_reminder.databinding.ChangeReminderFragmentBinding as Binding
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ConditionType
import com.example.util.simpletimetracker.feature_change_reminder.model.ChangeReminderEditor.ScheduleType
import com.example.util.simpletimetracker.feature_change_reminder.viewData.ChangeReminderViewData
import com.example.util.simpletimetracker.feature_change_reminder.viewModel.ChangeReminderViewModel
import com.example.util.simpletimetracker.feature_dialogs.api.DateTimeDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.TypesSelectionDialogListener
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeReminderParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeReminderFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: ChangeReminderViewModel by viewModels()

    private val daysAdapter by lazy {
        BaseRecyclerAdapter(createDayOfWeekAdapterDelegate(viewModel::onDayClick))
    }

    override fun initUi(): Unit = with(binding) {
        rvChangeReminderDays.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
            }
            adapter = daysAdapter
        }
        spinnerChangeReminderSchedule.setProcessSameItemSelection(false)
        spinnerChangeReminderCondition.setProcessSameItemSelection(false)
        spinnerChangeReminderDayOfMonth.setProcessSameItemSelection(false)
    }

    override fun initUx(): Unit = with(binding) {
        etChangeReminderMessage.doAfterTextChanged { viewModel.onMessageChanged(it.toString()) }
        fieldChangeReminderSchedule.setOnClick(spinnerChangeReminderSchedule::performClick)
        spinnerChangeReminderSchedule.onPositionSelected = viewModel::onScheduleSelected
        fieldChangeReminderCondition.setOnClick(spinnerChangeReminderCondition::performClick)
        spinnerChangeReminderCondition.onPositionSelected = viewModel::onConditionSelected
        fieldChangeReminderDayOfMonth.setOnClick(spinnerChangeReminderDayOfMonth::performClick)
        spinnerChangeReminderDayOfMonth.onPositionSelected = viewModel::onDayOfMonthSelected
        tvChangeReminderDate.setOnClick(viewModel::onDateClick)
        tvChangeReminderTime.setOnClick(viewModel::onTimeClick)
        btnChangeReminderActivity.setOnClick(viewModel::onActivityClick)
        btnChangeReminderSave.setOnClick(viewModel::onSaveClick)
        btnChangeReminderDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel() {
        viewModel.viewData.observe(::render)
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onDataSelected(
        tag: String,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
        selectValueOnStartTagIds: List<Long>,
    ) {
        viewModel.onActivitySelected(tag, dataIds)
    }

    // TODO switch to recycler
    private fun render(data: ChangeReminderViewData): Unit = with(binding) {
        // Delete button
        btnChangeReminderDelete.isVisible = data.deleteVisible

        // Message
        if (etChangeReminderMessage.text?.toString() != data.message) {
            etChangeReminderMessage.setText(data.message)
            etChangeReminderMessage.setSelection(data.message.length)
        }

        // Schedule
        spinnerChangeReminderSchedule.setData(
            items = data.scheduleItems,
            selectedPosition = data.scheduleSelectedPosition,
        )
        tvChangeReminderSchedule.text = data.scheduleItems
            .getOrNull(data.scheduleSelectedPosition)?.text.orEmpty()

        // Condition
        containerChangeReminderCondition.isVisible = data.scheduleType == ScheduleType.WEEKLY
        spinnerChangeReminderCondition.setData(
            items = data.conditionItems,
            selectedPosition = data.conditionSelectedPosition,
        )
        tvChangeReminderCondition.text = data.conditionItems
            .getOrNull(data.conditionSelectedPosition)?.text.orEmpty()

        // Days of week
        containerChangeReminderWeekdays.isVisible = data.scheduleType == ScheduleType.WEEKLY
        daysAdapter.replace(data.daysOfWeek)

        // Date and time
        val isOneTimeSchedule = data.scheduleType == ScheduleType.ONE_TIME
        tvChangeReminderDate.isVisible = isOneTimeSchedule

        tvChangeReminderDate.text = data.dateText
        tvChangeReminderTime.text = data.timeText
        btnChangeReminderActivity.text = data.activityName

        tvChangeReminderDate.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        tvChangeReminderTime.gravity = if (isOneTimeSchedule) {
            Gravity.CENTER_VERTICAL or Gravity.START
        } else {
            Gravity.CENTER
        }
        tvChangeReminderFirstHint.setText(
            if (isOneTimeSchedule) R.string.date_time_dialog_date else R.string.date_time_dialog_time,
        )
        tvChangeReminderFirstHint.labelFor = if (isOneTimeSchedule) {
            tvChangeReminderDate.id
        } else {
            tvChangeReminderTime.id
        }
        tvChangeReminderFirstHint.isVisible = true
        tvChangeReminderSecondHint.isVisible = isOneTimeSchedule

        // Day of month
        containerChangeReminderDayOfMonth.isVisible = data.scheduleType == ScheduleType.MONTHLY
        spinnerChangeReminderDayOfMonth.setData(
            items = data.dayOfMonthItems,
            selectedPosition = data.dayOfMonthSelectedPosition,
        )
        tvChangeReminderDayOfMonth.text = data.dayOfMonthItems
            .getOrNull(data.dayOfMonthSelectedPosition)?.text.orEmpty()

        // Activity
        btnChangeReminderActivity.isVisible = data.scheduleType == ScheduleType.WEEKLY &&
            data.conditionType == ConditionType.NOT_TRACKED

        // Controls
        etChangeReminderMessage.isEnabled = data.controlsEnabled
        fieldChangeReminderSchedule.isEnabled = data.controlsEnabled
        rvChangeReminderDays.isEnabled = data.controlsEnabled
        tvChangeReminderDate.isEnabled = data.controlsEnabled
        fieldChangeReminderDayOfMonth.isEnabled = data.controlsEnabled
        tvChangeReminderTime.isEnabled = data.controlsEnabled
        fieldChangeReminderCondition.isEnabled = data.controlsEnabled
        btnChangeReminderActivity.isEnabled = data.controlsEnabled
        btnChangeReminderSave.isEnabled = data.controlsEnabled
        btnChangeReminderDelete.isEnabled = data.controlsEnabled
    }

    companion object {
        fun createBundle(data: ChangeReminderParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
