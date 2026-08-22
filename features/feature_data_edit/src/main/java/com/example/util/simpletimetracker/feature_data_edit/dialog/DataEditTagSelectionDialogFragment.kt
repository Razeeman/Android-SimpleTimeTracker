package com.example.util.simpletimetracker.feature_data_edit.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.feature_dialogs.api.OnTagValueSelectedListener
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.findListeners
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.ARGS_PARAMS
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTagSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagValueSelectionParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_data_edit.databinding.DataEditTagSelectionDialogFragmentBinding as Binding

@AndroidEntryPoint
class DataEditTagSelectionDialogFragment :
    BaseBottomSheetFragment<Binding>(),
    OnTagValueSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: DataEditTagSelectionViewModel by viewModels()

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createInfoAdapterDelegate(),
            createCategoryAdapterDelegate(viewModel::onTagClick),
            createEmptyAdapterDelegate(),
            createEmptySpaceAdapterDelegate(),
        )
    }
    private val params: DataEditTagSelectionDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DataEditTagSelectionDialogParams.Empty,
    )
    private var listener: DataEditTagSelectionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context.findListeners<DataEditTagSelectionDialogListener>().firstOrNull()
    }

    override fun onDismiss(dialog: DialogInterface) {
        listener?.onTagsDismissed()
        super.onDismiss(dialog)
    }

    override fun initDialog() {
        setSkipCollapsed()
        setFullScreen()
        blockContentScroll(binding.rvDataEditTagSelectionContainer)
    }

    override fun initUi(): Unit = with(binding) {
        rvDataEditTagSelectionContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@DataEditTagSelectionDialogFragment.adapter
        }
    }

    override fun initUx() = with(binding) {
        btnDataEditTagSelectionSave.setOnClick(viewModel::onSaveClick)
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(adapter::replace)
        tagSelected.observe(::dismiss)
    }

    override fun onTagValueSelected(params: RecordTagValueSelectionParams, data: Double) {
        viewModel.onTagValueSelected(params, data)
    }

    private fun dismiss(tagIds: List<RecordBase.Tag>) {
        listener?.onTagsSelected(params.tag, tagIds)
        dismiss()
    }

    companion object {
        fun createBundle(data: DataEditTagSelectionDialogParams) = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}