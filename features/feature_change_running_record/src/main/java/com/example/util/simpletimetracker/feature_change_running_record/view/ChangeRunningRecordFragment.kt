package com.example.util.simpletimetracker.feature_change_running_record.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_dialogs.api.DateTimeDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.DurationDialogListener
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagValueSelectedListener
import com.example.util.simpletimetracker.feature_dialogs.api.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.GoalTimeViewData.Subtype
import com.example.util.simpletimetracker.feature_base_adapter.runningRecord.RunningRecordViewData
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordCore
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.feature_change_running_record.viewModel.ChangeRunningRecordViewModel
import com.example.util.simpletimetracker.feature_comment_selection.api.CommentSelectionViewDelegateProvider
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView.CheckState
import com.example.util.simpletimetracker.feature_views.extension.animateColor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordFromScreen
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_running_record.databinding.ChangeRunningRecordFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRunningRecordFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    DurationDialogListener,
    TypesSelectionDialogListener,
    OnTagValueSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var commentDelegateProvider: CommentSelectionViewDelegateProvider

    private val viewModel: ChangeRunningRecordViewModel by viewModels()

    private var typeColorAnimator: ValueAnimator? = null
    private val core by lazy { ChangeRecordCore(viewModel, commentDelegateProvider) }

    private val params: ChangeRunningRecordParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeRunningRecordParams.Empty,
    )

    override fun initUi(): Unit = with(binding) {
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

    override fun initUx(): Unit = with(binding) {
        core.initUx(this@ChangeRunningRecordFragment, layoutChangeRunningRecordCore)
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
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

    override fun onDestroy() {
        typeColorAnimator?.cancel()
        super.onDestroy()
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onDurationSet(durationSeconds: Long, tag: String?) {
        viewModel.onDurationSet(durationSeconds, tag)
    }

    override fun onTagValueSelected(params: RecordTagValueSelectionParams, data: Double) {
        viewModel.onCategoryValueSelected(params, data)
    }

    override fun onDataSelected(
        tag: String?,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
        selectValueOnStartTagIds: List<Long>,
    ) {
        viewModel.onDataSelected(tag, dataIds)
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
        ).let { updatePreview(it, animated = false) }

        core.onSetPreview(
            binding = binding.layoutChangeRunningRecordCore,
            color = preview.color,
            iconId = preview.iconId.toViewData(),
        )
    }

    private fun updatePreview(
        item: ChangeRunningRecordViewData,
        animated: Boolean = true,
    ) = with(binding.layoutChangeRunningRecordCore) {
        core.setDateTime(
            state = item.dateTimeStarted,
            dateView = tvChangeRecordTimeStartedDate,
            timeView = tvChangeRecordTimeStartedTime,
            hintView = tvChangeRecordTimeStartedAdjust,
        )

        if (item.recordPreview == null) return
        with(binding.previewChangeRunningRecord) {
            itemName = item.recordPreview.name
            itemTagName = item.recordPreview.tagName
            itemIcon = item.recordPreview.iconId
            itemTimeStarted = item.recordPreview.timeStarted
            itemTimer = item.recordPreview.timer
            itemTimerTotal = item.recordPreview.timerTotal
            itemGoalTime = item.recordPreview.goalTime.text
            itemGoalTimeCheck = when (item.recordPreview.goalTime.state) {
                is Subtype.Hidden -> CheckState.HIDDEN
                is Subtype.Goal -> CheckState.GOAL_REACHED
                is Subtype.Limit -> CheckState.LIMIT_REACHED
            }
            itemComment = item.recordPreview.comment
            itemNowIconVisible = item.recordPreview.nowIconVisible

            if (animated) {
                typeColorAnimator?.cancel()
                typeColorAnimator = animateColor(
                    from = itemColor,
                    to = item.recordPreview.color,
                    doOnUpdate = { value -> itemColor = value },
                )
            } else {
                itemColor = item.recordPreview.color
            }
        }

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

    companion object {
        fun createBundle(data: ChangeRunningRecordFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}