package com.example.util.simpletimetracker.feature_dialogs.duration.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.duration.extra.DurationPickerExtra
import com.example.util.simpletimetracker.feature_dialogs.duration.viewModel.DurationPickerViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.DurationDialogFragmentBinding as Binding

@AndroidEntryPoint
class DurationDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DurationPickerViewModel by viewModels()

    private var dialogListener: DurationDialogListener? = null
    private val params: DurationDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DurationDialogParams(),
    )

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
        binding.btnDurationPickerDisable.isGone = params.hideDisableButton
    }

    override fun initUx(): Unit = with(binding) {
        btnDurationPickerSave.setOnClick(::onSaveClick)
        btnDurationPickerDisable.setOnClick(::onDisableClick)
        viewDurationPickerNumberKeyboard.listener = viewModel::onButtonPressed
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = DurationPickerExtra(params.duration)
        durationViewData.observe(binding.viewDurationPickerValue::setData)
    }

    private fun onSaveClick() {
        viewModel.durationViewData.observeOnce(viewLifecycleOwner) { data ->
            val duration = data.seconds + data.minutes * 60L + data.hours * 3600L
            dialogListener?.onDurationSet(duration, params.tag)
            dismiss()
        }
    }

    private fun onDisableClick() {
        dialogListener?.onDisable(params.tag)
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: DurationDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}