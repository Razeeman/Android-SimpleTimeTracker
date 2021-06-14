package com.example.util.simpletimetracker.feature_widget.configure.view

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.configure.adapter.createWidgetAdapterDelegate
import com.example.util.simpletimetracker.feature_widget.configure.extra.WidgetExtra
import com.example.util.simpletimetracker.feature_widget.configure.viewModel.WidgetViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.widget_configure_activity.rvWidgetConfigureRecordType
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigureActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<WidgetViewModel>

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: WidgetViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createWidgetAdapterDelegate(viewModel::onRecordTypeClick),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED)

        initUi()
        initViewModel()
    }

    private fun initUi() {
        themeManager.setTheme(this)
        setContentView(R.layout.widget_configure_activity)

        rvWidgetConfigureRecordType.apply {
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
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        extra = WidgetExtra(widgetId)
        recordTypes.observe(this@WidgetConfigureActivity, typesAdapter::replace)
        handled.observe(this@WidgetConfigureActivity, ::exit)
    }

    private fun exit(widgetId: Int) {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}