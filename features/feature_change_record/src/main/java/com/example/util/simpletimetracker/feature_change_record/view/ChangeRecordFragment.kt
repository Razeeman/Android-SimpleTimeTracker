package com.example.util.simpletimetracker.feature_change_record.view

import com.example.util.simpletimetracker.feature_change_record.databinding.ChangeRecordPreviewFragmentBinding as Binding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import com.example.util.simpletimetracker.feature_change_record.viewData.ChangeRecordViewData
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordsFromScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeRecordFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRecordViewModel>

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: ChangeRecordViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val extra: ChangeRecordParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeRecordParams.New()
    )

    private val core by lazy {
        ChangeRecordCore(viewModel = viewModel)
    }

    override fun initUi(): Unit = with(binding) {
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
        core.initUi(binding.layoutChangeRecordCore)

        root.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }

    override fun initUx() = with(binding) {
        core.initUx(binding.layoutChangeRecordCore)
        btnChangeRecordDelete.setOnClick {
            viewModel.onDeleteClick()
            removeRecordViewModel.onDeleteClick(
                (extra as? ChangeRecordParams.Tracked)?.from
            )
        }
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = this@ChangeRecordFragment.extra
            record.observeOnce(viewLifecycleOwner) {
                core.updateUi(binding.layoutChangeRecordCore, it.comment)
            }
            record.observe(::updatePreview)
            core.initViewModel(this@ChangeRecordFragment, binding.layoutChangeRecordCore)
        }
        with(removeRecordViewModel) {
            prepare((extra as? ChangeRecordParams.Tracked)?.id.orZero())
            deleteButtonEnabled.observe(btnChangeRecordDelete::setEnabled)
            deleteIconVisibility.observe(btnChangeRecordDelete::visible::set)
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
            dateTimeStarted = "",
            dateTimeFinished = "",
            duration = preview.duration,
            iconId = preview.iconId.toViewData(),
            color = preview.color,
            comment = preview.comment
        ).let(::updatePreview)
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
        tvChangeRecordTimeStarted.text = item.dateTimeStarted
        tvChangeRecordTimeEnded.text = item.dateTimeFinished
    }

    companion object {
        private const val ARGS_PARAMS = "args_change_record_params"

        fun createBundle(data: ChangeRecordsFromScreen): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data.params)
        }
    }
}