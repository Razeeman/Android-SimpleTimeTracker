package com.example.util.simpletimetracker.feature_widget.statistics.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.category.createCategoryAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetStatisticsSettingsActivityBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetStatisticsSettingsActivity : BaseActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var contextProvider: ContextProvider

    private val viewModel: WidgetStatisticsSettingsViewModel by viewModels()

    private val recordTypesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createCategoryAdapterDelegate(viewModel::onCategoryClick),
            createLoaderAdapterDelegate(),
            createEmptyAdapterDelegate(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contextProvider.attach(this)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED)

        initUi()
    }

    private fun initUi() {
        themeManager.setTheme(this)
        val binding = WidgetStatisticsSettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ui
        binding.rvWidgetStatisticsFilterContainer.apply {
            layoutManager = FlexboxLayoutManager(this@WidgetStatisticsSettingsActivity).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = recordTypesAdapter
        }

        // Ux
        with(binding) {
            buttonsWidgetStatisticsSettingsFilterType.listener = viewModel::onFilterTypeClick
            btnWidgetStatisticsShowAll.setOnClick(viewModel::onShowAllClick)
            btnWidgetStatisticsHideAll.setOnClick(viewModel::onHideAllClick)
            btnWidgetStatisticsSettingsSave.setOnClick(viewModel::onSaveClick)
            spinnerWidgetStatisticsSettingsRange.onItemSelected = {
                viewModel.onRangeSelected(it)
            }
            btnWidgetStatisticsSettingsRange.setOnClick { spinnerWidgetStatisticsSettingsRange.performClick() }
        }

        // ViewModel
        with(viewModel) {
            val widgetId = intent?.extras
                ?.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID,
                )
                ?: AppWidgetManager.INVALID_APPWIDGET_ID

            extra = WidgetStatisticsSettingsExtra(widgetId)
            filterTypeViewData.observe(binding.buttonsWidgetStatisticsSettingsFilterType.adapter::replace)
            types.observe(recordTypesAdapter::replace)
            title.observe(binding.btnWidgetStatisticsSettingsRange::setText)
            rangeItems.observe { binding.spinnerWidgetStatisticsSettingsRange.setData(it.items, it.selectedPosition) }
            handled.observe(::exit)
        }
    }

    private fun exit(widgetId: Int) {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}