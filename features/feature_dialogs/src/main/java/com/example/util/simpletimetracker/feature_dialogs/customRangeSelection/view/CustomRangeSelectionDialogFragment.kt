package com.example.util.simpletimetracker.feature_dialogs.customRangeSelection.view

import com.example.util.simpletimetracker.feature_dialogs.databinding.CustomRangeSelectionFragmentBinding as Binding
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.CustomRangeSelectionDialogListener
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.customRangeSelection.viewModel.CustomRangeSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomRangeSelectionDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: CustomRangeSelectionViewModel by viewModels()

    private val params: CustomRangeSelectionParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = CustomRangeSelectionParams()
    )

    private var dialogListener: CustomRangeSelectionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is CustomRangeSelectionDialogListener -> {
                dialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is CustomRangeSelectionDialogListener && it.isResumed }
                    ?.let { dialogListener = it as? CustomRangeSelectionDialogListener }
            }
        }
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUx(): Unit = with(binding) {
        fieldCustomRangeSelectionTimeStarted.setOnClick(viewModel::onRangeStartClick)
        fieldCustomRangeSelectionTimeEnded.setOnClick(viewModel::onRangeEndClick)
        btnCustomRangeSelection.setOnClick(viewModel::onRangeSelected)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(viewLifecycleOwner) { viewData ->
            binding.tvCustomRangeSelectionTimeStarted.text = viewData.rangeStartString
            binding.tvCustomRangeSelectionTimeEnded.text = viewData.rangeEndString
        }
        customRangeSelectionParams.observeOnce(viewLifecycleOwner) { range ->
            dialogListener?.onCustomRangeSelected(range)
            dismiss()
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: CustomRangeSelectionParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}