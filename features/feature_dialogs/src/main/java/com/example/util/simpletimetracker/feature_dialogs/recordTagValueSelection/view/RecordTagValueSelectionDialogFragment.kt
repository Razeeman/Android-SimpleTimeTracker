package com.example.util.simpletimetracker.feature_dialogs.recordTagValueSelection.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagValueSelectedListener
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.hideKeyboard
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.extension.showKeyboard
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.commentField.createCommentFieldAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordTagValueSelection.viewModel.RecordTagValueSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.postDelayed
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordTagValueSelectionDialogFragmentBinding as Binding

@AndroidEntryPoint
class RecordTagValueSelectionDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: RecordTagValueSelectionViewModel by viewModels()

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createCommentFieldAdapterDelegate(
                afterTextChange = viewModel::onValueChange,
                onKeyboardButtonClick = viewModel::onKeyboardButtonClick,
            ),
        )
    }
    private val params: RecordTagValueSelectionParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordTagValueSelectionParams.Empty,
    )
    private var listeners: List<OnTagValueSelectedListener> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listeners = context.findListeners<OnTagValueSelectedListener>()
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi() {
        binding.tvRecordTagValueSelection.text = params.title
            ?: getString(R.string.change_record_type_value_type_field)

        binding.rvRecordTagValueSelectionList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@RecordTagValueSelectionDialogFragment.adapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnRecordTagSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(::setViewData)
        keyboardVisibility.observe(::showKeyboard)
        onDataSelected.observe(::onDataSelected)
    }

    private fun setViewData(data: List<ViewHolderType>) {
        adapter.replace(data)
    }

    private fun showKeyboard(isVisible: Boolean) {
        if (isVisible) {
            binding.rvRecordTagValueSelectionList.postDelayed(200) {
                findViewById<EditText>(R.id.etCommentItemField)
                    ?.let(::showKeyboard)
            }
        } else {
            hideKeyboard()
        }
    }

    private fun onDataSelected(value: Double) {
        listeners.forEach { it.onTagValueSelected(params, value) }
        dismiss()
    }

    companion object {
        fun createBundle(data: RecordTagValueSelectionParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}