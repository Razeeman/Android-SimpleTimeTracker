package com.example.util.simpletimetracker.feature_records.view

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.core.viewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.SnackBarParams
import kotlinx.android.synthetic.main.records_container_fragment.*
import javax.inject.Inject

class RecordsContainerFragment : BaseFragment(R.layout.records_container_fragment),
    DateTimeDialogListener {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory<RecordsContainerViewModel>

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsContainerViewModel by viewModels(
        factoryProducer = { viewModelFactory }
    )
    private val removeRecordViewModel: RemoveRecordViewModel by viewModels(
        ownerProducer = { activity as AppCompatActivity },
        factoryProducer = { removeRecordViewModelFactory }
    )

    override fun initDi() {
        (activity?.application as RecordsComponentProvider)
            .recordsComponent
            ?.inject(this)
    }

    override fun initUi() {
        setupPager()
    }

    override fun initUx() {
        btnRecordAdd.setOnClick(viewModel::onRecordAddClick)
        btnRecordsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnRecordsContainerNext.setOnClick(viewModel::onNextClick)
        btnRecordsContainerToday.setOnClick(viewModel::onTodayClick)
        btnRecordsContainerToday.setOnLongClick(viewModel::onTodayLongClick)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(viewLifecycleOwner, ::updateTitle)
            position.observe(viewLifecycleOwner) {
                pagerRecordsContainer.setCurrentItem(
                    it + RecordsContainerAdapter.FIRST,
                    viewPagerSmoothScroll
                )
            }
        }
        with(removeRecordViewModel) {
            message.observe(viewLifecycleOwner, ::showMessage)
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun setupPager() {
        val adapter = RecordsContainerAdapter(this)
        pagerRecordsContainer.apply {
            this.adapter = adapter
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    private fun updateTitle(title: String) {
        btnRecordsContainerToday.text = title
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && this.isResumed) {
            router.show(Notification.SNACK_BAR, message, btnRecordAdd)
            removeRecordViewModel.onMessageShown()
        }
    }

    companion object {
        var viewPagerSmoothScroll: Boolean = true
        fun newInstance() = RecordsContainerFragment()
    }
}