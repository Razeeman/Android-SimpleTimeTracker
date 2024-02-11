package com.example.util.simpletimetracker.feature_widget.quickSettings.settings

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.core.provider.ContextProvider
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetQuickSettingsConfigureActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetQuickSettingsConfigureActivity : BaseActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var contextProvider: ContextProvider

    private val viewModel: WidgetQuickSettingsConfigureViewModel by viewModels()

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
        val binding = WidgetQuickSettingsConfigureActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ux
        binding.layoutWidgetQuickSettingsAllowMultitasking.setOnClick(viewModel::onAllowMultitaskingClicked)
        binding.layoutWidgetQuickSettingsShowTagSelection.setOnClick(viewModel::onShowRecordTagSelectionClicked)
    }

    private fun initViewModel(): Unit = with(viewModel) {
        val widgetId = intent?.extras
            ?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            )
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        extra = WidgetQuickSettingsConfigureExtra(widgetId)
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