package com.example.util.simpletimetracker.feature_archive.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.feature_archive.R
import com.example.util.simpletimetracker.feature_archive.di.ArchiveComponentProvider
import com.example.util.simpletimetracker.feature_archive.dialog.ArchiveDialogListener
import com.example.util.simpletimetracker.feature_archive.dialog.ArchiveDialogParams
import com.example.util.simpletimetracker.feature_archive.viewModel.ArchiveViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.archive_fragment.*
import javax.inject.Inject

class ArchiveFragment : BaseFragment(R.layout.archive_fragment),
    ArchiveDialogListener,
    StandardDialogListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<ArchiveViewModel>

    private val viewModel: ArchiveViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val archiveAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick)
        )
    }

    override fun initDi() {
        (activity?.application as ArchiveComponentProvider)
            .archiveComponent
            ?.inject(this)
    }

    override fun initUi() {
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
        viewData.observe(viewLifecycleOwner, archiveAdapter::replace)
    }

    override fun onDeleteClick(params: ArchiveDialogParams?) =
        viewModel.onDeleteClick(params)

    override fun onRestoreClick(params: ArchiveDialogParams?) =
        viewModel.onRestoreClick(params)

    override fun onPositiveClick(tag: String?, data: Any?) =
        viewModel.onPositiveDialogClick(tag, data)
}
