package com.example.util.simpletimetracker.feature_records_all.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.core.adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.viewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.feature_records_all.R
import com.example.util.simpletimetracker.feature_records_all.adapter.createRecordAllDateAdapterDelegate
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import com.example.util.simpletimetracker.feature_records_all.viewModel.RecordsAllViewModel
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.SnackBarParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.records_all_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class RecordsAllFragment : BaseFragment() {

    override val layout: Int get() = R.layout.records_all_fragment

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RecordsAllViewModel>

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsAllViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordAdapterDelegate(viewModel::onRecordClick),
            createRecordAllDateAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate()
        )
    }
    private val params: RecordsAllParams by lazy {
        arguments?.getParcelable(ARGS_PARAMS) ?: RecordsAllParams()
    }

    override fun initUi() {
        postponeEnterTransition()

        rvRecordsAllList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter

            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
    }

    override fun initUx() {
        spinnerRecordsAllSort.onPositionSelected = viewModel::onRecordTypeOrderSelected
    }

    override fun initViewModel() {
        with(viewModel) {
            extra = params
            records.observe(recordsAdapter::replaceAsNew)
            sortOrderViewData.observe(::updateCardOrderViewData)
        }
        with(removeRecordViewModel) {
            needUpdate.observe(::onUpdateNeeded)
            message.observe(::showMessage)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
        spinnerRecordsAllSort.jumpDrawablesToCurrentState()
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && message.tag == SnackBarParams.TAG.RECORDS_ALL_DELETE) {
            router.show(Notification.SNACK_BAR, message)
            removeRecordViewModel.onMessageShown()
        }
    }

    private fun onUpdateNeeded(isUpdateNeeded: Boolean) {
        if (isUpdateNeeded && this@RecordsAllFragment.isResumed) {
            viewModel.onNeedUpdate()
            removeRecordViewModel.onUpdated()
        }
    }

    private fun updateCardOrderViewData(viewData: RecordsAllSortOrderViewData) {
        spinnerRecordsAllSort.setData(viewData.items, viewData.selectedPosition)
        tvRecordsAllSortValue.text = viewData.items.getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is RecordsAllParams -> putParcelable(ARGS_PARAMS, data)
            }
        }
    }
}
