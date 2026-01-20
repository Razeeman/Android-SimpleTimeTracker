package com.example.util.simpletimetracker.feature_archive.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.ArchiveDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.dialog.StandardDialogListener
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.doOnApplyWindowInsetsListener
import com.example.util.simpletimetracker.core.utils.getNavBarInsets
import com.example.util.simpletimetracker.feature_archive.viewData.ArchiveSearchState
import com.example.util.simpletimetracker.feature_archive.viewModel.ArchiveViewModel
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_archive.databinding.ArchiveFragmentBinding as Binding

@AndroidEntryPoint
class ArchiveFragment :
    BaseFragment<Binding>(),
    ArchiveDialogListener,
    StandardDialogListener,
    OptionsListDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    private val viewModel: ArchiveViewModel by viewModels()

    private val archiveAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptySpaceAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createDividerAdapterDelegate(),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
        )
    }

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        rvArchiveList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = archiveAdapter
        }

        btnArchiveOptions.doOnApplyWindowInsetsListener {
            val navBarHeight = it.getNavBarInsets().bottom.pxToDp()
            viewModel.onChangeInsets(navBarHeight = navBarHeight)
            setMargins(bottom = navBarHeight)
        }

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnArchiveOptions.setOnClick(throttle(viewModel::onOptionsClick))
        btnArchiveOptions.setOnLongClick(throttle(viewModel::onOptionsLongClick))
        etArchiveSearchField.doAfterTextChanged { viewModel.onSearchChange(it.toString()) }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        viewData.observe { archiveAdapter.replace(it.items) }
        showHint.observe(binding.tvArchiveHint::isVisible::set)
        searchState.observe(::setSearchState)
    }

    override fun onDeleteClick(params: ArchiveDialogParams) {
        viewModel.onDeleteClick(params)
    }

    override fun onRestoreClick(params: ArchiveDialogParams) {
        viewModel.onRestoreClick(params)
    }

    override fun onPositiveClick(tag: String?, data: Any?) {
        viewModel.onPositiveDialogClick(tag, data)
    }

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        viewModel.onOptionsItemClick(id)
    }

    private fun setSearchState(
        state: ArchiveSearchState,
    ) = with(binding) {
        groupArchiveSearch.isVisible = state.isVisible
        if (state.text != etArchiveSearchField.text.toString()) {
            etArchiveSearchField.setText(state.text)
            etArchiveSearchField.setSelection(state.text.length)
        }
    }
}
