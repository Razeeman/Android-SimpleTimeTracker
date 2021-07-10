package com.example.util.simpletimetracker.feature_dialogs.duration.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_dialogs.duration.extra.DurationPickerExtra
import com.example.util.simpletimetracker.feature_dialogs.duration.viewModel.DurationPickerViewModel
import com.example.util.simpletimetracker.navigation.params.DurationDialogParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.DurationDialogFragmentBinding as Binding

@AndroidEntryPoint
class DurationDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<DurationPickerViewModel>

    private val viewModel: DurationPickerViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private var dialogListener: DurationDialogListener? = null
    private val dialogTag: String? by lazy { arguments?.getString(ARGS_TAG) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is DurationDialogListener -> {
                dialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is DurationDialogListener && it.isResumed }
                    ?.let { dialogListener = it as? DurationDialogListener }
            }
        }
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    override fun initUi() {
        // Do nothing
    }

    override fun initUx(): Unit = with(binding) {
        btnDurationPickerSave.setOnClick(::onSaveClick)
        btnDurationPickerDisable.setOnClick(::onDisableClick)
        viewDurationPickerNumberKeyboard.listener = viewModel::onNumberPressed
        ivDurationPickerDelete.setOnClick(viewModel::onNumberDelete)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = DurationPickerExtra(arguments?.getLong(ARGS_DURATION).orZero())
        durationViewData.observe(binding.viewDurationPickerValue::setData)
    }

    private fun onSaveClick() {
        viewModel.durationViewData.observeOnce(viewLifecycleOwner) { data ->
            val duration = data.seconds + data.minutes * 60L + data.hours * 3600L
            dialogListener?.onDurationSet(duration, dialogTag)
            dismiss()
        }
    }

    private fun onDisableClick() {
        dialogListener?.onDisable(dialogTag)
        dismiss()
    }

    companion object {
        private const val ARGS_TAG = "tag"
        private const val ARGS_DURATION = "duration"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is DurationDialogParams -> {
                    putString(ARGS_TAG, data.tag)
                    putLong(ARGS_DURATION, data.duration)
                }
            }
        }
    }
}