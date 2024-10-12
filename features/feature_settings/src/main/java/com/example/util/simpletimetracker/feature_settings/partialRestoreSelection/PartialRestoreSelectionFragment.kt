package com.example.util.simpletimetracker.feature_settings.partialRestoreSelection

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.delegates.iconSelection.adapter.createIconSelectionAdapterDelegate
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.color.createColorAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.complexRule.createComplexRuleAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emoji.createEmojiAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider.createRecordsDateDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogListener
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogParams
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_settings.databinding.SettingsPartialRestoreSelectionFragmentBinding as Binding

@AndroidEntryPoint
class PartialRestoreSelectionFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: PartialRestoreSelectionViewModel by viewModels()

    private val viewDataAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createActivityFilterAdapterDelegate(viewModel::onActivityFilterClick),
            createComplexRuleAdapterDelegate(
                onItemClick = viewModel::onComplexRuleClick,
                onDisableClick = {}, // Do nothing.
            ),
            createIconSelectionAdapterDelegate(viewModel::onIconClick),
            createEmojiAdapterDelegate(viewModel::onEmojiClick),
            createColorAdapterDelegate(viewModel::onColorClick),
            createRecordAdapterDelegate(viewModel::onRecordClick),
            createRecordsDateDividerAdapterDelegate(),
            createLoaderAdapterDelegate(),
        )
    }
    private val extra: PartialRestoreSelectionDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = PartialRestoreSelectionDialogParams.Empty,
    )
    private var listener: PartialRestoreSelectionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener()
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvSettingsPartialRestoreSelectionContainer)
    }

    override fun initUi() {
        binding.rvSettingsPartialRestoreSelectionContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = viewDataAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnSettingsPartialRestoreSelectionShowAll.setOnClick(viewModel::onShowAllClick)
        btnSettingsPartialRestoreSelectionHideAll.setOnClick(viewModel::onHideAllClick)
        btnSettingsPartialRestoreSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = this@PartialRestoreSelectionFragment.extra
        viewData.observe(viewDataAdapter::replace)
        saveButtonEnabled.observe(binding.btnSettingsPartialRestoreSelectionSave::setEnabled)
        onDataSelected.observeOnce(viewLifecycleOwner, ::onDataSelected)
    }

    private fun onDataSelected(typeIds: Set<Long>) {
        listener?.onDataSelected(tag = extra.tag, type = extra.type, dataIds = typeIds)
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_partial_restore_selection_params"

        fun createBundle(data: PartialRestoreSelectionDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}