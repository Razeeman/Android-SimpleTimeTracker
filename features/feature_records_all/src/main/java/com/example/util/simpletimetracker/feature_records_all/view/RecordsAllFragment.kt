package com.example.util.simpletimetracker.feature_records_all.view

import com.example.util.simpletimetracker.feature_records_all.databinding.RecordsAllFragmentBinding as Binding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.empty.createEmptyAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.record.createRecordAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordsDateDivider.createRecordsDateDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_records_all.viewData.RecordsAllSortOrderViewData
import com.example.util.simpletimetracker.feature_records_all.viewModel.RecordsAllViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordsAllFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsAllViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory }
    )
    private val recordsAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createRecordAdapterDelegate(throttle(viewModel::onRecordClick)),
            createRecordsDateDividerAdapterDelegate(),
            createEmptyAdapterDelegate(),
            createLoaderAdapterDelegate()
        )
    }
    private val params: RecordsAllParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = RecordsAllParams()
    )

    override fun initUi(): Unit = with(binding) {
        postponeEnterTransition()

        rvRecordsAllList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recordsAdapter
        }

        setOnPreDrawListener {
            startPostponedEnterTransition()
        }
    }

    override fun initUx() = with(binding) {
        spinnerRecordsAllSort.onPositionSelected = viewModel::onRecordTypeOrderSelected
    }

    override fun initViewModel() {
        with(viewModel) {
            extra = params
            records.observe(recordsAdapter::replaceAsNew)
            sortOrderViewData.observe(::updateSortOrderViewData)
        }
        with(removeRecordViewModel) {
            needUpdate.observe(::onUpdateNeeded)
            message.observe(::showMessage)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onVisible()
        binding.spinnerRecordsAllSort.jumpDrawablesToCurrentState()
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && message.tag == SnackBarParams.TAG.RECORDS_ALL_DELETE) {
            router.show(message)
            removeRecordViewModel.onMessageShown()
        }
    }

    private fun onUpdateNeeded(isUpdateNeeded: Boolean) {
        if (isUpdateNeeded && this@RecordsAllFragment.isResumed) {
            viewModel.onNeedUpdate()
            removeRecordViewModel.onUpdated()
        }
    }

    private fun updateSortOrderViewData(viewData: RecordsAllSortOrderViewData) = with(binding) {
        spinnerRecordsAllSort.setData(viewData.items, viewData.selectedPosition)
        tvRecordsAllSortValue.text = viewData.items.getOrNull(viewData.selectedPosition)?.text.orEmpty()
    }

    companion object {
        private const val ARGS_PARAMS = "args_params"

        fun createBundle(data: RecordsAllParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}
