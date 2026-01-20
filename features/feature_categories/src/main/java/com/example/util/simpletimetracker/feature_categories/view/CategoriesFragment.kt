package com.example.util.simpletimetracker.feature_categories.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.ChartFilterDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.dialog.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.utils.doOnApplyWindowInsetsListener
import com.example.util.simpletimetracker.core.utils.getNavBarInsets
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAddAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_categories.viewData.CategoriesSearchState
import com.example.util.simpletimetracker.feature_categories.viewModel.CategoriesViewModel
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.setMargins
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_categories.databinding.CategoriesFragmentBinding as Binding

@AndroidEntryPoint
class CategoriesFragment :
    BaseFragment<Binding>(),
    OptionsListDialogListener,
    ChartFilterDialogListener,
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    private val viewModel: CategoriesViewModel by viewModels()

    private val categoriesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createEmptySpaceAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createDividerAdapterDelegate(),
            createHintAdapterDelegate(),
            createCategoryAdapterDelegate(onClickWithTransition = throttle(viewModel::onCategoryClick)),
            createCategoryAddAdapterDelegate(throttle(viewModel::onAddCategoryClick)),
        )
    }

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        rvCategoriesList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = categoriesAdapter
        }

        btnCategoriesOptions.doOnApplyWindowInsetsListener {
            val navBarHeight = it.getNavBarInsets().bottom.pxToDp()
            viewModel.onChangeInsets(navBarHeight = navBarHeight)
            setMargins(bottom = navBarHeight)
        }

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx(): Unit = with(binding) {
        btnCategoriesOptions.setOnClick(throttle(viewModel::onOptionsClick))
        btnCategoriesOptions.setOnLongClick(throttle(viewModel::onOptionsLongClick))
        etCategoriesSearchField.doAfterTextChanged { viewModel.onSearchChange(it.toString()) }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        categories.observe { categoriesAdapter.replace(it.items) }
        showHint.observe(binding.tvCategoriesEditHint::isVisible::set)
        searchState.observe(::setSearchState)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        viewModel.onOptionsItemClick(id)
    }

    override fun onChartFilterDataSelected(
        chartFilterType: ChartFilterType,
        dataIds: List<Long>,
    ) {
        viewModel.onFilterApplied(chartFilterType, dataIds)
    }

    override fun onChartFilterDialogDismissed() {
        viewModel.onFilterClosed()
    }

    override fun onDataSelected(
        tag: String?,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
    ) {
        viewModel.onDataSelected(dataIds, tag)
    }

    private fun setSearchState(
        state: CategoriesSearchState,
    ) = with(binding) {
        groupCategoriesSearch.isVisible = state.isVisible
        if (state.text != etCategoriesSearchField.text.toString()) {
            etCategoriesSearchField.setText(state.text)
            etCategoriesSearchField.setSelection(state.text.length)
        }
    }
}
