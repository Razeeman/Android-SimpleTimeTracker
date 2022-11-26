package com.example.util.simpletimetracker.feature_change_running_record.view

import com.example.util.simpletimetracker.feature_change_running_record.databinding.ChangeRunningRecordFragmentBinding as Binding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSharedTransitions
import com.example.util.simpletimetracker.core.extension.toViewData
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordCore
import com.example.util.simpletimetracker.feature_change_running_record.viewData.ChangeRunningRecordViewData
import com.example.util.simpletimetracker.feature_change_running_record.viewModel.ChangeRunningRecordViewModel
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
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
    lateinit var viewModelFactory: BaseViewModelFactory<ChangeRunningRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: ChangeRunningRecordViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val params: ChangeRunningRecordParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ChangeRunningRecordParams()
    )

    private val core by lazy {
        ChangeRecordCore(viewModel = viewModel)
    }

    override fun initUi(): Unit = with(binding) {
        layoutChangeRunningRecordCore.fieldChangeRecordTimeEnded.isVisible = false
        layoutChangeRunningRecordCore.btnChangeRecordTimeEndedAdjust.isVisible = false

        postponeEnterTransition()

        setPreview()
        setSharedTransitions(
            transitionName = TransitionNames.RECORD_RUNNING + params.id,
            sharedView = binding.previewChangeRunningRecord,
        )
        core.initUi(binding.layoutChangeRunningRecordCore)

        root.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
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
                core.updateUi(binding.layoutChangeRunningRecordCore, it.comment)
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
            name = name,
            tagName = tagName,
            timeStarted = timeStarted,
            dateTimeStarted = "",
            duration = duration,
            goalTime = goalTime,
            iconId = iconId.toViewData(),
            color = color,
            comment = comment
        ).let(::updatePreview)
    }

    private fun updatePreview(item: ChangeRunningRecordViewData) = with(binding.layoutChangeRunningRecordCore) {
        with(binding.previewChangeRunningRecord) {
            itemName = item.name
            itemTagName = item.tagName
            itemIcon = item.iconId
            itemColor = item.color
            itemTimeStarted = item.timeStarted
            itemTimer = item.duration
            itemGoalTime = item.goalTime
            itemComment = item.comment
        }
        tvChangeRecordTimeStarted.text = item.dateTimeStarted
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null) {
            router.show(message, binding.layoutChangeRunningRecordCore.btnChangeRecordSave)
            viewModel.onMessageShown()
        }
    }

    companion object {
        private const val ARGS_PARAMS = "args_running_record_params"

        fun createBundle(data: ChangeRunningRecordParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}