package com.example.util.simpletimetracker.feature_dialogs.defaultTypesSelection.view

import com.example.util.simpletimetracker.feature_dialogs.databinding.DefaultTypesSelectionDialogFragmentBinding as Binding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.defaultTypesSelection.viewModel.DefaultTypesSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DefaultTypesSelectionDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DefaultTypesSelectionViewModel by viewModels()

    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createLoaderAdapterDelegate(),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
        )
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvDefaultTypesSelectionContainer)
    }

    override fun initUi() {
        binding.rvDefaultTypesSelectionContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnDefaultTypesSelectionShowAll.setOnClick(viewModel::onShowAllClick)
        btnDefaultTypesSelectionHideAll.setOnClick(viewModel::onHideAllClick)
        btnDefaultTypesSelectionSave.setOnClick(viewModel::onSaveClick)
        btnDefaultTypesSelectionHide.setOnClick(viewModel::onHideClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        types.observe(recordTypesAdapter::replace)
        saveButtonEnabled.observe(::bindSaveButtonState)
        close.observeOnce(viewLifecycleOwner) { dismiss() }
    }

    private fun bindSaveButtonState(enabled: Boolean) = with(binding) {
        btnDefaultTypesSelectionSave.isEnabled = enabled
        val color = if (enabled) {
            R.attr.appActiveColor
        } else {
            R.attr.appInactiveColor
        }
        context?.getThemedAttr(color)
            ?.let(btnDefaultTypesSelectionSave::setBackgroundColor)
    }
}