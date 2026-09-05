package com.example.util.simpletimetracker.feature_change_reminder.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_change_reminder.databinding.ChangeActivityReminderFragmentBinding as Binding
import com.example.util.simpletimetracker.feature_change_reminder.viewData.ChangeActivityReminderViewData
import com.example.util.simpletimetracker.feature_change_reminder.viewModel.ChangeActivityReminderViewModel
import com.example.util.simpletimetracker.feature_dialogs.api.DateTimeDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.DurationDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.TypesSelectionDialogListener
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityReminderParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeActivityReminderFragment :
    BaseFragment<Binding>(),
    DurationDialogListener,
    DateTimeDialogListener,
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: ChangeActivityReminderViewModel by viewModels()

    private val daysAdapter by lazy {
        BaseRecyclerAdapter(createDayOfWeekAdapterDelegate(viewModel::onDayClick))
    }

    override fun initUi(): Unit = with(binding) {
        rvActivityReminderDays.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
            }
            adapter = daysAdapter
        }
        spinnerActivityReminderMode.setProcessSameItemSelection(false)
    }

    override fun initUx(): Unit = with(binding) {
        fieldActivityReminderMode.setOnClick(spinnerActivityReminderMode::performClick)
        spinnerActivityReminderMode.onPositionSelected = viewModel::onModeSelected
        fieldActivityReminderName.setOnClick(viewModel::onActivityClick)
        fieldActivityReminderDuration.setOnClick(viewModel::onDurationClick)
        tvActivityReminderDndStart.setOnClick(viewModel::onDoNotDisturbStartClick)
        tvActivityReminderDndEnd.setOnClick(viewModel::onDoNotDisturbEndClick)
        btnActivityReminderSave.setOnClick(viewModel::onSaveClick)
        btnActivityReminderDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel() {
        viewModel.viewData.observe(::render)
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModel.onDurationSet(durationSeconds, tag)
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

    private fun render(data: ChangeActivityReminderViewData): Unit = with(binding) {
        // Activity
        tvActivityReminderName.text = data.activityName
        fieldActivityReminderName.isEnabled = data.activitySelectionEnabled

        // Delete
        btnActivityReminderDelete.isVisible = data.deleteVisible

        // Mode
        spinnerActivityReminderMode.setData(
            items = data.modeItems,
            selectedPosition = data.modeSelectedPosition,
        )
        tvActivityReminderMode.text = data.modeItems
            .getOrNull(data.modeSelectedPosition)?.text.orEmpty()
        containerActivityReminderCustom.isVisible = data.customFieldsVisible

        // Duration
        tvActivityReminderDurationValue.text = data.durationText

        // Days
        daysAdapter.replace(data.daysOfWeek)

        // DnD
        tvActivityReminderDndStart.text = data.doNotDisturbStartText
        tvActivityReminderDndEnd.text = data.doNotDisturbEndText

        // Recurrent
        checkboxActivityReminderRecurrent.isChecked = data.recurrent
        checkboxActivityReminderRecurrent.setOnClick(viewModel::onRecurrentChanged)

        // Controls
        fieldActivityReminderMode.isEnabled = data.controlsEnabled
        fieldActivityReminderDuration.isEnabled = data.controlsEnabled
        checkboxActivityReminderRecurrent.isEnabled = data.controlsEnabled
        rvActivityReminderDays.isEnabled = data.controlsEnabled
        tvActivityReminderDndStart.isEnabled = data.controlsEnabled
        tvActivityReminderDndEnd.isEnabled = data.controlsEnabled
        btnActivityReminderSave.isEnabled = data.controlsEnabled
        btnActivityReminderDelete.isEnabled = data.controlsEnabled
    }

    companion object {
        fun createBundle(data: ChangeActivityReminderParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
