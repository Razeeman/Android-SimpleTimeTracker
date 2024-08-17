package com.example.util.simpletimetracker.feature_archive.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.ArchiveDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.feature_archive.viewModel.ArchiveViewModel
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_archive.databinding.ArchiveFragmentBinding as Binding
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.core.utils.InsetConfiguration

@AndroidEntryPoint
class ArchiveFragment :
    BaseFragment<Binding>(),
    ArchiveDialogListener,
    StandardDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override val insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.rvArchiveList }

    private val viewModel: ArchiveViewModel by viewModels()

    private val archiveAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
        )
    }

    override fun initUi(): Unit = with(binding) {
        rvArchiveList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = archiveAdapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        viewData.observe {
            archiveAdapter.replace(it.items)
            binding.tvArchiveHint.isVisible = it.showHint
        }
    }

    override fun onDeleteClick(params: ArchiveDialogParams) =
        viewModel.onDeleteClick(params)

    override fun onRestoreClick(params: ArchiveDialogParams) =
        viewModel.onRestoreClick(params)

    override fun onPositiveClick(tag: String?, data: Any?) =
        viewModel.onPositiveDialogClick(tag, data)
}
