package com.example.util.simpletimetracker.feature_data_edit.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.extension.blockContentScroll
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setFullScreen
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.info.createInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTagSelectionDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_data_edit.databinding.DataEditTagSelectionDialogFragmentBinding as Binding

@AndroidEntryPoint
class DataEditTagSelectionDialogFragment : BaseBottomSheetFragment<Binding>() {

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
        )
    }
    private val params: DataEditTagSelectionDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DataEditTagSelectionDialogParams()
    )
    private var listener: DataEditTagSelectionDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is DataEditTagSelectionDialogListener -> {
                listener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is DataEditTagSelectionDialogListener && it.isResumed }
                    ?.let { listener = it as? DataEditTagSelectionDialogListener }
            }
        }
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

    private fun dismiss(tagIds: List<Long>) {
        listener?.onTagsSelected(params.tag, tagIds)
        dismiss()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: DataEditTagSelectionDialogParams) = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}