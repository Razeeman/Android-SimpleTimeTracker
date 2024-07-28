package com.example.util.simpletimetracker.feature_dialogs.typesSelection.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListener
import com.example.util.simpletimetracker.core.extension.observeOnce
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.typesSelection.viewData.TypesSelectionDialogViewData
import com.example.util.simpletimetracker.feature_dialogs.typesSelection.viewModel.TypesSelectionViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_dialogs.databinding.RecordTagSelectionTypesDialogFragmentBinding as Binding

@AndroidEntryPoint
class TypesSelectionDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: TypesSelectionViewModel by viewModels()

    private val viewDataAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createLoaderAdapterDelegate(),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }
    private val extra: TypesSelectionDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = TypesSelectionDialogParams.Empty,
    )
    private var listener: TypesSelectionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListener()
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvTypesSelectionContainer)
    }

    override fun initUi() {
        binding.rvTypesSelectionContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = viewDataAdapter
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnTypesSelectionShowAll.setOnClick(viewModel::onShowAllClick)
        btnTypesSelectionHideAll.setOnClick(viewModel::onHideAllClick)
        btnTypesSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = this@TypesSelectionDialogFragment.extra
        viewState.observe(::updateViewState)
        viewData.observe(viewDataAdapter::replace)
        saveButtonEnabled.observe(binding.btnTypesSelectionSave::setEnabled)
        onDataSelected.observeOnce(viewLifecycleOwner, ::onDataSelected)
    }

    private fun updateViewState(data: TypesSelectionDialogViewData) = with(binding) {
        tvTypesSelectionDialogTitle.text = data.title
        tvTypesSelectionDialogTitle.isVisible = data.title.isNotEmpty()

        tvTypesSelectionDialogSubtitle.text = data.subtitle
        tvTypesSelectionDialogSubtitle.isVisible = data.subtitle.isNotEmpty()

        viewTypesSelectionDialogDivider.isVisible =
            data.title.isNotEmpty() || data.subtitle.isNotEmpty()
        containerTypesSelectionButtons.isVisible = data.isButtonsVisible
    }

    private fun onDataSelected(typeIds: List<Long>) {
        listener?.onDataSelected(typeIds, extra.tag)
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_types_selection_params"

        fun createBundle(data: TypesSelectionDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}