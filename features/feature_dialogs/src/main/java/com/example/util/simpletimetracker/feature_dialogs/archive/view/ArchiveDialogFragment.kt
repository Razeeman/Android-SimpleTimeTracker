package com.example.util.simpletimetracker.feature_dialogs.archive.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseBottomSheetFragment
import com.example.util.simpletimetracker.core.dialog.ArchiveDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.setSkipCollapsed
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.adapter.createArchiveDialogButtonsAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.adapter.createArchiveDialogInfoAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.adapter.createArchiveDialogTitleAdapterDelegate
import com.example.util.simpletimetracker.feature_dialogs.archive.viewModel.ArchiveDialogViewModel
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.example.util.simpletimetracker.feature_dialogs.databinding.ArchiveDialogFragmentBinding as Binding

@AndroidEntryPoint
class ArchiveDialogFragment : BaseBottomSheetFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    private val viewModel: ArchiveDialogViewModel by viewModels()

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createRecordTypeAdapterDelegate(),
            createCategoryAdapterDelegate(),
            createArchiveDialogInfoAdapterDelegate(),
            createArchiveDialogTitleAdapterDelegate(),
            createArchiveDialogButtonsAdapterDelegate(
                onDeleteClick = ::onDeleteClick,
                onRestoreClick = ::onRestoreClick,
            ),
        )
    }
    private val params: ArchiveDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = ArchiveDialogParams.Activity(0),
    )
    private var archiveDialogListener: ArchiveDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is ArchiveDialogListener -> {
                archiveDialogListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is ArchiveDialogListener && it.isResumed }
                    ?.let { archiveDialogListener = it as? ArchiveDialogListener }
            }
        }
    }

    override fun initDialog() {
        setSkipCollapsed()
    }

    override fun initUi() {
        binding.rvArchiveDialogContent.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = this@ArchiveDialogFragment.adapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = params
        viewData.observe(adapter::replace)
    }

    private fun onDeleteClick() {
        dismiss()
        MainScope().launch { archiveDialogListener?.onDeleteClick(params) }
    }

    private fun onRestoreClick() {
        dismiss()
        MainScope().launch { archiveDialogListener?.onRestoreClick(params) }
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: ArchiveDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}