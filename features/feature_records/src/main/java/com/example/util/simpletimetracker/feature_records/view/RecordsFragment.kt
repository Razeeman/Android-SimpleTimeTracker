package com.example.util.simpletimetracker.feature_records.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_records.extra.RecordsExtra
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import com.example.util.simpletimetracker.navigation.params.RecordsParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_records.databinding.RecordsFragmentBinding as Binding

@AndroidEntryPoint
class RecordsFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

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
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordAdapterDelegate(viewModel::onRecordClick),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate()

        )
    }

    override fun initUi(): Unit = with(binding) {
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
            records.observe(recordsAdapter::replace)
        }
        with(removeRecordViewModel) {
            needUpdate.observe {
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
