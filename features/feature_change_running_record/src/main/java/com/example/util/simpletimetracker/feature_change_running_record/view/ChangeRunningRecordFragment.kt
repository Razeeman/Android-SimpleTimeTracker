package com.example.util.simpletimetracker.feature_change_running_record.view

import com.example.util.simpletimetracker.feature_change_running_record.databinding.ChangeRunningRecordFragmentBinding as Binding
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
        key = ARGS_PARAMS, default = ChangeRunningRecordParams()
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
            sharedView = binding.previewChangeRunningRecord,
        )
        core.initUi(binding.layoutChangeRunningRecordCore)

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx() = with(binding) {
        core.initUx(binding.layoutChangeRunningRecordCore)
        binding.btnChangeRunningRecordDelete.setOnClick(viewModel::onDeleteClick)
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = params
            record.observeOnce(viewLifecycleOwner) {
                core.updateUi(binding.layoutChangeRunningRecordCore, it.recordPreview?.comment.orEmpty())
            }
            record.observe(::updatePreview)
            core.initViewModel(this@ChangeRunningRecordFragment, binding.layoutChangeRunningRecordCore)

            deleteButtonEnabled.observe(btnChangeRunningRecordDelete::setEnabled)
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

    private fun setPreview() = params.preview?.run {
        ChangeRunningRecordViewData(
            RunningRecordViewData(
                id = 0, // Doesn't matter for preview.
                name = name,
                tagName = tagName,
                timeStarted = timeStarted,
                timer = duration,
                timerTotal = durationTotal,
                goalTime = goalTime.toViewData(),
                goalTime2 = goalTime2.toViewData(),
                goalTime3 = goalTime3.toViewData(),
                goalTime4 = goalTime4.toViewData(),
                iconId = iconId.toViewData(),
                color = color,
                comment = comment,
                nowIconVisible = params.from is ChangeRunningRecordParams.From.Records,
            ),
            dateTimeStarted = "",
        ).let(::updatePreview)
    }

    private fun updatePreview(item: ChangeRunningRecordViewData) = with(binding.layoutChangeRunningRecordCore) {
        tvChangeRecordTimeStarted.text = item.dateTimeStarted

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
            itemGoalTime2 = item.recordPreview.goalTime2.text
            itemGoalTime2Complete = item.recordPreview.goalTime2.complete
            itemGoalTime3 = item.recordPreview.goalTime3.text
            itemGoalTime3Complete = item.recordPreview.goalTime3.complete
            itemGoalTime4 = item.recordPreview.goalTime4.text
            itemGoalTime4Complete = item.recordPreview.goalTime4.complete
            itemComment = item.recordPreview.comment
            itemNowIconVisible = item.recordPreview.nowIconVisible
        }
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null) {
            router.show(message, binding.layoutChangeRunningRecordCore.btnChangeRecordSave)
            viewModel.onMessageShown()
        }
    }

    private fun coreSetup() = with(binding) {
        // TODO move to view model
        // No time ended in running record.
        layoutChangeRunningRecordCore.fieldChangeRecordTimeEnded.isVisible = false
        layoutChangeRunningRecordCore.btnChangeRecordTimeEndedAdjust.isVisible = false
        // Can't continue running record.
        layoutChangeRunningRecordCore.containerChangeRecordContinue.isVisible = false
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