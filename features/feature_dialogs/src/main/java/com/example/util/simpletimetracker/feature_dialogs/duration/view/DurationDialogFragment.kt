package com.example.util.simpletimetracker.feature_dialogs.duration.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.feature_dialogs.api.DurationDialogListener
import com.example.util.simpletimetracker.core.extension.behavior
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_dialogs.duration.adapter.createDurationSuggestionAdapter
import com.example.util.simpletimetracker.feature_dialogs.duration.customView.DurationView
import com.example.util.simpletimetracker.feature_dialogs.duration.model.DurationDialogState
import com.example.util.simpletimetracker.feature_dialogs.duration.viewModel.DurationPickerViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.DurationDialogFragmentBinding as Binding

@AndroidEntryPoint
class DurationDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DurationPickerViewModel by viewModels()
    private val suggestionsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDurationSuggestionAdapter(
                onClick = viewModel::onSuggestionClick,
                onLongClick = viewModel::onSuggestionLongClick,
            ),
        )
    }
    private var listeners: List<DurationDialogListener> = emptyList()
    private val params: DurationDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DurationDialogParams(),
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listeners = context.findListeners<DurationDialogListener>()
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
    }

    override fun initUi(): Unit = with(binding) {
        rvDurationPickerSuggestions.apply {
            adapter = suggestionsAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnDurationPickerSave.setOnClick(::onSaveClick)
        btnDurationPickerDisable.setOnClick(::onDisableClick)
        viewDurationPickerNumberKeyboard.listener = viewModel::onButtonPressed
        viewDurationPickerTouchInterceptor.attachTouchInterceptor(parent = viewDurationPickerValue)
        viewDurationPickerValue.listener = DurationView.Listener(viewModel::onValueChanged)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        stateViewData.observe(::updateState)
        suggestionsViewData.observe(::updateSuggestionsState)
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
                binding.tvDurationPickerValue.text = "${state.value.data}"
            }
        }
    }

    private fun updateSuggestionsState(viewData: List<ViewHolderType>) = with(binding) {
        rvDurationPickerSuggestions.isVisible = viewData.isNotEmpty()
        suggestionsAdapter.replace(viewData)
    }

    private fun onSaveClick() {
        viewModel.stateViewData.observeOnce(viewLifecycleOwner) { state ->
            val data = state.value.getDurationSeconds()

            when (state.value) {
                is DurationDialogState.Value.Duration -> {
                    listeners.forEach { it.onDurationSet(data, params.tag) }
                }
                is DurationDialogState.Value.Count -> {
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

    @SuppressLint("ClickableViewAccessibility")
    private fun View.attachTouchInterceptor(
        parent: View,
    ) {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> behavior?.isDraggable = false
                MotionEvent.ACTION_MOVE -> behavior?.isDraggable = false
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL,
                -> behavior?.isDraggable = true
            }
            parent.onTouchEvent(event)
            true
        }
    }

    companion object {
        fun createBundle(data: DurationDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}