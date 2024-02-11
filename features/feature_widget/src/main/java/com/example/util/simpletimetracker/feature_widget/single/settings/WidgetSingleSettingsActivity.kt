package com.example.util.simpletimetracker.feature_widget.single.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.createRunningRecordTypeSpecialAdapterDelegate
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetSingleConfigureActivityBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetSingleSettingsActivity : BaseActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var contextProvider: ContextProvider

    private val viewModel: WidgetSingleSettingsViewModel by viewModels()

    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createRunningRecordTypeSpecialAdapterDelegate(viewModel::onSpecialRecordTypeClick),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate(),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contextProvider.attach(this)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED)

        initUi()
        initViewModel()
    }

    private fun initUi() {
        themeManager.setTheme(this)
        val binding = WidgetSingleConfigureActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvWidgetSingleConfigureRecordType.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    private fun initViewModel(): Unit = with(viewModel) {
        val widgetId = intent?.extras
            ?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            )
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        extra = WidgetSingleSettingsExtra(widgetId)
        recordTypes.observe(typesAdapter::replace)
        handled.observe(::exit)
    }

    private fun exit(widgetId: Int) {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}