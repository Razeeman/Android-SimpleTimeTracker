package com.example.util.simpletimetracker.feature_widget.universal.activity.view

import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider
import com.example.util.simpletimetracker.feature_widget.universal.activity.viewModel.WidgetUniversalViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.android.synthetic.main.widget_universal_fragment.*
import javax.inject.Inject

class WidgetUniversalFragment : BaseFragment(R.layout.widget_universal_fragment) {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<WidgetUniversalViewModel>

    private val viewModel: WidgetUniversalViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate()
        )
    }

    override fun initDi() {
        (activity?.application as WidgetComponentProvider)
            .widgetComponent
            ?.inject(this)
    }

    override fun initUi() {
        rvWidgetUniversalRecordType.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        recordTypes.observe(this@WidgetUniversalFragment, typesAdapter::replace)
    }
}