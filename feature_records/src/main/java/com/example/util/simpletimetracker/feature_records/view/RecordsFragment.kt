package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.viewModel.RemoveRecordViewModel
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
    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    private val viewModel: RecordsViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
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

    override fun initViewModel() {
        with(viewModel) {
            extra = RecordsExtra(shift = arguments?.getInt(ARGS_POSITION).orZero())
            records.observe(viewLifecycleOwner, recordsAdapter::replace)
        }
        with(removeRecordViewModel) {
            needUpdate.observe(viewLifecycleOwner) {
                if (it && this@RecordsFragment.isResumed) {
                    viewModel.onNeedUpdate()
                    removeRecordViewModel.onUpdated()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
    }

    companion object {
        private const val ARGS_POSITION = "args_position"

        fun newInstance(data: Any?): RecordsFragment = RecordsFragment().apply {
            val bundle = Bundle()
            when (data) {
                is RecordsParams -> {
                    bundle.putInt(ARGS_POSITION, data.shift)
                }
            }
            arguments = bundle
        }
    }
}
