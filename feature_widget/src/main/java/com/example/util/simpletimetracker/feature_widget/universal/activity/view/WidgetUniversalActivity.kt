package com.example.util.simpletimetracker.feature_widget.universal.activity.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.manager.ThemeManager
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider
import com.example.util.simpletimetracker.feature_widget.universal.activity.adapter.WidgetUniversalAdapter
import com.example.util.simpletimetracker.feature_widget.universal.activity.viewModel.WidgetUniversalViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.widget_universal_activity.*
import javax.inject.Inject

class WidgetUniversalActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<WidgetUniversalViewModel>

    @Inject
    lateinit var themeManager: ThemeManager

    private val viewModel: WidgetUniversalViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val typesAdapter: WidgetUniversalAdapter by lazy {
        WidgetUniversalAdapter(
            viewModel::onRecordTypeClick
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDi()
        initUi()
        initViewModel()
    }

    private fun initDi() {
        (application as WidgetComponentProvider)
            .widgetComponent
            ?.inject(this)
    }

    private fun initUi() {
        themeManager.setTheme(this)
        setContentView(R.layout.widget_universal_activity)

        rvWidgetUniversalRecordType.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    private fun initViewModel(): Unit = with(viewModel) {
        recordTypes.observe(this@WidgetUniversalActivity, typesAdapter::replace)
    }
}