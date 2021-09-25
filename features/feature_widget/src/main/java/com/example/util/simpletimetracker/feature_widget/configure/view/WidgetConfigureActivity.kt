package com.example.util.simpletimetracker.feature_widget.configure.view

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseActivity
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.feature_widget.configure.adapter.createWidgetAdapterDelegate
import com.example.util.simpletimetracker.feature_widget.configure.extra.WidgetExtra
import com.example.util.simpletimetracker.feature_widget.configure.viewModel.WidgetViewModel
import com.example.util.simpletimetracker.feature_widget.databinding.WidgetConfigureActivityBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigureActivity : BaseActivity() {

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
        val binding = WidgetConfigureActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvWidgetConfigureRecordType.apply {
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