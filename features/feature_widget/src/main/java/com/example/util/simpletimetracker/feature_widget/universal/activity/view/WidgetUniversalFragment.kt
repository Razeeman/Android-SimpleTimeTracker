package com.example.util.simpletimetracker.feature_widget.universal.activity.view

import com.example.util.simpletimetracker.feature_widget.databinding.WidgetUniversalFragmentBinding as Binding
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.OnTagSelectedListener
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.createActivityFilterAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_widget.universal.activity.viewModel.WidgetUniversalViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WidgetUniversalFragment :
    BaseFragment<Binding>(),
    OnTagSelectedListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<WidgetUniversalViewModel>

    private val viewModel: WidgetUniversalViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )

    private val typesAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createActivityFilterAdapterDelegate(viewModel::onActivityFilterClick),
            createRecordTypeAdapterDelegate(viewModel::onRecordTypeClick),
            createDividerAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
        )
    }

    override fun initUi() {
        binding.rvWidgetUniversalRecordType.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.WRAP
            }
            adapter = typesAdapter
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        recordTypes.observe(typesAdapter::replace)
        exit.observe { exit() }
    }

    override fun onTagSelected() {
        viewModel.onTagSelected()
    }

    private fun exit() {
        Handler(Looper.getMainLooper()).postDelayed({ activity?.finish() }, EXIT_DELAY_MS)
    }

    companion object {
        private const val EXIT_DELAY_MS = 500L
    }
}