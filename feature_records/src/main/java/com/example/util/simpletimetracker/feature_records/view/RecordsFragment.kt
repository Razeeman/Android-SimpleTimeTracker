package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordAdapter
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.navigation.params.RecordsParams
import kotlinx.android.synthetic.main.records_fragment.*
import javax.inject.Inject

class RecordsFragment : BaseFragment(R.layout.records_fragment) {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RecordsViewModel>

    private val viewModel: RecordsViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val recordsAdapter: RecordAdapter by lazy {
        RecordAdapter(
            viewModel::onRecordClick
        )
    }

    override fun initDi() {
        (activity?.application as RecordsComponentProvider)
            .recordsComponent
            ?.inject(this)
    }

    override fun initUi() {
        parentFragment?.postponeEnterTransition()

        rvRecordsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter

            viewTreeObserver.addOnPreDrawListener {
                parentFragment?.startPostponedEnterTransition()
                true
            }
        }
    }

    override fun initViewModel(): Unit = with(viewModel) {
        extra = RecordsExtra(
            rangeStart = arguments?.getLong(ARGS_RANGE_START).orZero(),
            rangeEnd = arguments?.getLong(ARGS_RANGE_END).orZero()
        )
        viewModel.records.observe(viewLifecycleOwner, recordsAdapter::replace)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        private const val ARGS_RANGE_START = "args_range_start"
        private const val ARGS_RANGE_END = "args_range_end"

        fun newInstance(data: Any?): RecordsFragment = RecordsFragment().apply {
            val bundle = Bundle()
            when (data) {
                is RecordsParams -> {
                    bundle.putLong(ARGS_RANGE_START, data.rangeStart)
                    bundle.putLong(ARGS_RANGE_END, data.rangeEnd)
                }
            }
            arguments = bundle
        }
    }
}
