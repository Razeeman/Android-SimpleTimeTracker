package com.example.util.simpletimetracker.feature_widget.statistics.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.dialog.DurationDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.viewData.RangeSelectionOptionsListItem
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_widget.utils.getAppWidgetIdOrInvalid
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetStatisticsSettingsFragmentBinding as Binding

@AndroidEntryPoint
class WidgetStatisticsSettingsFragment :
    BaseFragment<Binding>(),
    DurationDialogListener,
    OptionsListDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: WidgetStatisticsSettingsViewModel by viewModels()

    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }

    override fun initUi() {
        binding.rvWidgetStatisticsFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    override fun initUx() = with(binding) {
        buttonsWidgetStatisticsSettingsFilterType.listener = viewModel::onFilterTypeClick
        btnWidgetStatisticsShowAll.setOnClick(viewModel::onShowAllClick)
        btnWidgetStatisticsHideAll.setOnClick(viewModel::onHideAllClick)
        btnWidgetStatisticsSettingsSave.setOnClick(throttle(viewModel::onSaveClick))
        btnWidgetStatisticsSettingsRange.setOnClick(throttle(viewModel::onSelectRangeClick))
        checkboxWidgetStatisticsNewItems.setOnClick(throttle(viewModel::onDoNotIncludeNewItemsClick))
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            extra = WidgetStatisticsSettingsExtra(getWidgetId())
            filterTypeViewData.observe(buttonsWidgetStatisticsSettingsFilterType::replace)
            types.observe(recordTypesAdapter::replace)
            title.observe(btnWidgetStatisticsSettingsRange::setText)
            doNotIncludeNewItems.observe(::setDoNotIncludeItemsState)
            handled.observe(::exit)
        }
    }

    override fun onCountSet(count: Long, tag: String?) {
        viewModel.onCountSet(count, tag)
    }

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        when (id) {
            is RangeSelectionOptionsListItem -> {
                viewModel.onRangeSelected(id)
            }
        }
    }

    private fun getWidgetId(): Int {
        return activity?.intent.getAppWidgetIdOrInvalid()
    }

    private fun setDoNotIncludeItemsState(isChecked: Boolean) = with(binding) {
        if (checkboxWidgetStatisticsNewItems.isChecked != isChecked) {
            checkboxWidgetStatisticsNewItems.isChecked = isChecked
        }
    }

    // TODO refactor to shared viewModel to pass data?
    private fun exit(widgetId: Int) {
        (activity as? WidgetStatisticsActivity)?.exit(widgetId)
    }
}