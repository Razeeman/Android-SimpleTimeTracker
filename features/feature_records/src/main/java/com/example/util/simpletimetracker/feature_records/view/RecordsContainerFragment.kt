package com.example.util.simpletimetracker.feature_records.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewDelegate.DateSelectorViewDelegate
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.dialog.OptionsListDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.MainTabsViewModel
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.view.SafeFragmentStateAdapter
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerPosition
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_records.databinding.RecordsContainerFragmentBinding as Binding

@AndroidEntryPoint
class RecordsContainerFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener,
    OptionsListDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var mainTabsViewModelFactory: BaseViewModelFactory<MainTabsViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsContainerViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )
    private val mainTabsViewModel: MainTabsViewModel by activityViewModels(
        factoryProducer = { mainTabsViewModelFactory },
    )
    private val dateSelectorViewHolder by lazy {
        DateSelectorViewDelegate.getViewHolder(
            viewModel = viewModel.dateSelectorViewModelDelegate,
        )
    }

    override fun initUi(): Unit = with(binding) {
        pagerRecordsContainer.apply {
            adapter = SafeFragmentStateAdapter(
                RecordsContainerAdapter(this@RecordsContainerFragment),
            )
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
        DateSelectorViewDelegate.initUi(
            fragment = this@RecordsContainerFragment,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = containerDatesSelector,
        )
        viewModel.initialize()
    }

    override fun initUx() {
        DateSelectorViewDelegate.initUx(
            fragment = this,
            binding = binding.containerDatesSelector,
            onRecordAddClick = viewModel::onRecordAddClick,
            onOptionsClick = viewModel::onOptionsClick,
            onOptionsLongClick = viewModel::onOptionsLongClick,
        )
    }

    override fun initViewModel() {
        with(viewModel) {
            position.observe(::setPosition)
        }
        with(removeRecordViewModel) {
            message.observe(::showMessage)
        }
        with(mainTabsViewModel) {
            isNavBatAtTheBottom.observe(::updateInsetConfiguration)
        }
        DateSelectorViewDelegate.initViewModel(
            fragment = this,
            viewHolder = dateSelectorViewHolder,
            viewModel = viewModel.dateSelectorViewModelDelegate,
            binding = binding.containerDatesSelector,
        )
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    override fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        viewModel.onOptionsItemClick(id)
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && message.tag == SnackBarParams.TAG.RECORD_DELETE) {
            router.show(message, binding.containerDatesSelector.btnRecordsContainerAdd)
            removeRecordViewModel.onMessageShown()
        }
    }

    private fun setPosition(data: RecordsContainerPosition) = with(binding) {
        pagerRecordsContainer.setCurrentItem(
            data.position + RecordsContainerAdapter.FIRST,
            data.animate && viewPagerSmoothScroll,
        )
    }

    private fun updateInsetConfiguration(isNavBatAtTheBottom: Boolean) {
        insetConfiguration = if (isNavBatAtTheBottom) {
            InsetConfiguration.DoNotApply
        } else {
            InsetConfiguration.ApplyToView { binding.root }
        }
        initInsets()
    }

    companion object {
        @VisibleForTesting
        var viewPagerSmoothScroll: Boolean = true

        fun newInstance() = RecordsContainerFragment()
    }
}