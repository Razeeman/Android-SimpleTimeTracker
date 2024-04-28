package com.example.util.simpletimetracker.feature_change_record.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordsFromScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordFragmentBinding as Binding

@AndroidEntryPoint
class ChangeRecordFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: ChangeRecordViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )
    private val extra: ChangeRecordParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeRecordParams.New(),
    )

    private val core by lazy {
        ChangeRecordCore(viewModel = viewModel)
    }

    override fun initUi(): Unit = with(binding) {
        coreSetup()
        postponeEnterTransition()

        setPreview()
        val transitionName: String = when (extra) {
            is ChangeRecordParams.Tracked -> (extra as? ChangeRecordParams.Tracked)?.transitionName.orEmpty()
            is ChangeRecordParams.Untracked -> (extra as? ChangeRecordParams.Untracked)?.transitionName.orEmpty()
            else -> ""
        }
        setSharedTransitions(
            additionalCondition = { transitionName.isNotEmpty() },
            transitionName = transitionName,
            sharedView = previewChangeRecord,
        )
        core.initUi(layoutChangeRecordCore)

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx() = with(binding) {
        core.initUx(this@ChangeRecordFragment, layoutChangeRecordCore)
        layoutChangeRecordCore.btnChangeRecordStatistics.setOnClick(viewModel::onStatisticsClick)
        layoutChangeRecordCore.btnChangeRecordDelete.setOnClick {
            viewModel.onDeleteClick()
            removeRecordViewModel.onDeleteClick(
                (extra as? ChangeRecordParams.Tracked)?.from,
            )
        }
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = this@ChangeRecordFragment.extra
            record.observeOnce(viewLifecycleOwner) {
                core.updateUi(layoutChangeRecordCore, it.comment)
            }
            record.observe(::updatePreview)
            core.initViewModel(this@ChangeRecordFragment, layoutChangeRecordCore)
        }
        with(removeRecordViewModel) {
            prepare((extra as? ChangeRecordParams.Tracked)?.id.orZero())
            deleteButtonEnabled.observe(layoutChangeRecordCore.btnChangeRecordDelete::setEnabled)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun setPreview() = when (extra) {
        is ChangeRecordParams.Tracked -> (extra as? ChangeRecordParams.Tracked)?.preview
        is ChangeRecordParams.Untracked -> (extra as? ChangeRecordParams.Untracked)?.preview
        else -> null
    }?.let { preview ->
        ChangeRecordViewData(
            name = preview.name,
            tagName = preview.tagName,
            timeStarted = preview.timeStarted,
            timeFinished = preview.timeFinished,
            dateTimeStarted = preview.timeStartedDateTime.toViewData(),
            dateTimeFinished = preview.timeEndedDateTime.toViewData(),
            duration = preview.duration,
            iconId = preview.iconId.toViewData(),
            color = preview.color,
            comment = preview.comment,
        ).let(::updatePreview)

        binding.viewChangeRecordPreviewBackground.backgroundTintList =
            ColorStateList.valueOf(preview.color)
        core.onSetPreview(
            binding = binding.layoutChangeRecordCore,
            color = preview.color,
            iconId = preview.iconId.toViewData(),
        )
    }

    private fun updatePreview(item: ChangeRecordViewData) = with(binding.layoutChangeRecordCore) {
        with(binding.previewChangeRecord) {
            itemName = item.name
            itemTagName = item.tagName
            itemIcon = item.iconId
            itemColor = item.color
            itemTimeStarted = item.timeStarted
            itemTimeEnded = item.timeFinished
            itemDuration = item.duration
            itemComment = item.comment
        }
        tvChangeRecordTimeStartedDate.text = item.dateTimeStarted.date
        tvChangeRecordTimeStartedTime.text = item.dateTimeStarted.time
        tvChangeRecordTimeEndedDate.text = item.dateTimeFinished.date
        tvChangeRecordTimeEndedTime.text = item.dateTimeFinished.time
        tvChangeRecordChangeCurrentPreviewTimeStarted.text = item.dateTimeStarted.time
        tvChangeRecordChangeCurrentPreviewTimeEnded.text = item.dateTimeFinished.time

        with(binding) {
            viewChangeRecordPreviewBackground.backgroundTintList =
                ColorStateList.valueOf(item.color)
        }
        core.onSetPreview(
            binding = this,
            color = item.color,
            iconId = item.iconId,
        )
    }

    private fun coreSetup() = with(binding) {
        // TODO move to view model
        context?.getString(R.string.change_record_change_adjacent_records)
            ?.let(layoutChangeRecordCore.tvChangeRecordChangeHint::setText)
    }

    companion object {
        private const val ARGS_PARAMS = "args_change_record_params"

        fun createBundle(data: ChangeRecordsFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}