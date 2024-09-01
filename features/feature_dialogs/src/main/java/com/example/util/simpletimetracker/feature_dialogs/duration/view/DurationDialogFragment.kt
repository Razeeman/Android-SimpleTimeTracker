package com.example.util.simpletimetracker.feature_dialogs.duration.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_dialogs.duration.model.DurationDialogState
import com.example.util.simpletimetracker.feature_dialogs.duration.viewModel.DurationPickerViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.DurationDialogFragmentBinding as Binding

@AndroidEntryPoint
class DurationDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DurationPickerViewModel by viewModels()

    private val listeners: MutableList<DurationDialogListener> = mutableListOf()
    private val params: DurationDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DurationDialogParams(),
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listeners += context.findListeners<DurationDialogListener>()
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    override fun initUx(): Unit = with(binding) {
        btnDurationPickerSave.setOnClick(::onSaveClick)
        btnDurationPickerDisable.setOnClick(::onDisableClick)
        viewDurationPickerNumberKeyboard.listener = viewModel::onButtonPressed
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        stateViewData.observe(::updateState)
    }

    private fun updateState(state: DurationDialogState) {
        binding.btnDurationPickerDisable.visible = state.showDisableButton

        when (state.value) {
            is DurationDialogState.Value.Duration -> {
                binding.viewDurationPickerValue.visible = true
                binding.tvDurationPickerValue.visible = false
                binding.viewDurationPickerValue.setData(state.value.data)
            }
            is DurationDialogState.Value.Count -> {
                binding.viewDurationPickerValue.visible = false
                binding.tvDurationPickerValue.visible = true
                binding.tvDurationPickerValue.text = state.value.data
            }
        }
    }

    private fun onSaveClick() {
        viewModel.stateViewData.observeOnce(viewLifecycleOwner) { state ->
            when (state.value) {
                is DurationDialogState.Value.Duration -> {
                    val data = state.value.data
                    val duration = data.seconds + data.minutes * 60L + data.hours * 3600L
                    listeners.forEach { it.onDurationSet(duration, params.tag) }
                }
                is DurationDialogState.Value.Count -> {
                    val data = state.value.data.toLongOrNull().orZero()
                    listeners.forEach { it.onCountSet(data, params.tag) }
                }
            }
            dismiss()
        }
    }

    private fun onDisableClick() {
        listeners.forEach { it.onDisable(params.tag) }
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: DurationDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}