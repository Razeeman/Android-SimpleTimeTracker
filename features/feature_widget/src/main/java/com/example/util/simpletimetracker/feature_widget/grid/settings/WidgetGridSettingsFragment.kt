package com.example.util.simpletimetracker.feature_widget.grid.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_widget.utils.getAppWidgetIdOrInvalid
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetGridSettingsFragmentBinding as Binding

@AndroidEntryPoint
class WidgetGridSettingsFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: WidgetGridSettingsViewModel by viewModels()

    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }

    override fun initUi() {
        binding.rvWidgetGridSettingsTypes.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }
    }

    override fun initUx() = with(binding) {
        btnWidgetGridSettingsShowAll.setOnClick(viewModel::onShowAllClick)
        btnWidgetGridSettingsHideAll.setOnClick(viewModel::onHideAllClick)
        btnWidgetGridSettingsSave.setOnClick(throttle(viewModel::onSaveClick))
        checkboxWidgetGridNewItems.setOnClick(throttle(viewModel::onDoNotIncludeNewItemsClick))
    }

    override fun initViewModel() = with(viewModel) {
        extra = WidgetGridSettingsExtra(getWidgetId())
        types.observe(recordTypesAdapter::replace)
        doNotIncludeNewItems.observe(::setDoNotIncludeItemsState)
        handled.observe(::exit)
    }

    private fun getWidgetId(): Int {
        return activity?.intent.getAppWidgetIdOrInvalid()
    }

    private fun setDoNotIncludeItemsState(isChecked: Boolean) = with(binding) {
        if (checkboxWidgetGridNewItems.isChecked != isChecked) {
            checkboxWidgetGridNewItems.isChecked = isChecked
        }
    }

    private fun exit(widgetId: Int) {
        (activity as? WidgetGridSettingsActivity)?.exit(widgetId)
    }
}
