package com.example.util.simpletimetracker.feature_dialogs.recordTagSelectionTypes.view

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
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.recordTagSelectionTypes.viewModel.RecordTagSelectionTypesViewModel
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordTagSelectionTypesDialogFragmentBinding as Binding

@AndroidEntryPoint
class RecordTagSelectionTypesDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: RecordTagSelectionTypesViewModel by viewModels()

    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createLoaderAdapterDelegate(),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvRecordTagSelectionTypesContainer)
    }

    override fun initUi() {
        binding.rvRecordTagSelectionTypesContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnRecordTagSelectionTypesShowAll.setOnClick(viewModel::onShowAllClick)
        btnRecordTagSelectionTypesHideAll.setOnClick(viewModel::onHideAllClick)
        btnRecordTagSelectionTypesSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        types.observe(recordTypesAdapter::replace)
        saveButtonEnabled.observe(binding.btnRecordTagSelectionTypesSave::setEnabled)
        close.observeOnce(viewLifecycleOwner) { dismiss() }
    }
}