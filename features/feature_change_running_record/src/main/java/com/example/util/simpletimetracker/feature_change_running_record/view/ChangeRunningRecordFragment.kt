package com.example.util.simpletimetracker.feature_change_running_record.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordCore
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.feature_change_running_record.viewModel.ChangeRunningRecordViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_running_record.databinding.ChangeRunningRecordFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRunningRecordFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var router: Router

    private val viewModel: ChangeRunningRecordViewModel by viewModels()

    private val params: ChangeRunningRecordParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeRunningRecordParams(),
    )

    private val core by lazy {
        ChangeRecordCore(viewModel = viewModel)
    }

    override fun initUi(): Unit = with(binding) {
        coreSetup()
        postponeEnterTransition()

        setPreview()
        setSharedTransitions(
            additionalCondition = { params.transitionName.isNotEmpty() },
            transitionName = params.transitionName,
            sharedView = previewChangeRunningRecord,
        )
        core.initUi(layoutChangeRunningRecordCore)

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx() = with(binding) {
        core.initUx(this@ChangeRunningRecordFragment, layoutChangeRunningRecordCore)
        layoutChangeRunningRecordCore.btnChangeRecordStatistics.setOnClick(viewModel::onStatisticsClick)
        layoutChangeRunningRecordCore.btnChangeRecordDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = params
            record.observeOnce(viewLifecycleOwner) {
                core.updateUi(layoutChangeRunningRecordCore, it.recordPreview?.comment.orEmpty())
            }
            record.observe(::updatePreview)
            core.initViewModel(this@ChangeRunningRecordFragment, layoutChangeRunningRecordCore)

            deleteButtonEnabled.observe(layoutChangeRunningRecordCore.btnChangeRecordDelete::setEnabled)
            message.observe(::showMessage)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onHidden()
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun setPreview() = params.preview?.let { preview ->
        ChangeRunningRecordViewData(
            recordPreview = RunningRecordViewData(
                id = 0, // Doesn't matter for preview.
                name = preview.name,
                tagName = preview.tagName,
                timeStarted = preview.timeStarted,
                timeStartedTimestamp = 0,
                timer = preview.duration,
                timerTotal = preview.durationTotal,
                goalTime = preview.goalTime.toViewData(),
                iconId = preview.iconId.toViewData(),
                color = preview.color,
                comment = preview.comment,
                nowIconVisible = params.from is ChangeRunningRecordParams.From.Records,
            ),
            dateTimeStarted = preview.timeStartedDateTime.toViewData(),
        ).let(::updatePreview)

        binding.viewChangeRunningRecordPreviewBackground.backgroundTintList =
            ColorStateList.valueOf(preview.color)
        core.onSetPreview(
            binding = binding.layoutChangeRunningRecordCore,
            color = preview.color,
            iconId = preview.iconId.toViewData(),
        )
    }

    private fun updatePreview(item: ChangeRunningRecordViewData) = with(binding.layoutChangeRunningRecordCore) {
        tvChangeRecordTimeStartedDate.text = item.dateTimeStarted.date
        tvChangeRecordTimeStartedTime.text = item.dateTimeStarted.time

        if (item.recordPreview == null) return
        with(binding.previewChangeRunningRecord) {
            itemName = item.recordPreview.name
            itemTagName = item.recordPreview.tagName
            itemIcon = item.recordPreview.iconId
            itemColor = item.recordPreview.color
            itemTimeStarted = item.recordPreview.timeStarted
            itemTimer = item.recordPreview.timer
            itemTimerTotal = item.recordPreview.timerTotal
            itemGoalTime = item.recordPreview.goalTime.text
            itemGoalTimeComplete = item.recordPreview.goalTime.complete
            itemComment = item.recordPreview.comment
            itemNowIconVisible = item.recordPreview.nowIconVisible
        }

        binding.viewChangeRunningRecordPreviewBackground.backgroundTintList =
            ColorStateList.valueOf(item.recordPreview.color)
        core.onSetPreview(
            binding = this,
            color = item.recordPreview.color,
            iconId = item.recordPreview.iconId,
        )
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null) {
            router.show(message, binding.layoutChangeRunningRecordCore.btnChangeRecordSave)
            viewModel.onMessageShown()
        }
    }

    private fun coreSetup() = with(binding) {
        // TODO move to view model
        // Can't continue running record.
        layoutChangeRunningRecordCore.containerChangeRecordContinue.isVisible = false
        // Can't repeat running record.
        layoutChangeRunningRecordCore.containerChangeRecordRepeat.isVisible = false
        // Can't duplicate running record.
        layoutChangeRunningRecordCore.containerChangeRecordDuplicate.isVisible = false

        context?.getString(R.string.change_record_change_prev_record)
            ?.let(layoutChangeRunningRecordCore.tvChangeRecordAdjustHint::setText)
    }

    companion object {
        private const val ARGS_PARAMS = "args_running_record_params"

        fun createBundle(data: ChangeRunningRecordFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}