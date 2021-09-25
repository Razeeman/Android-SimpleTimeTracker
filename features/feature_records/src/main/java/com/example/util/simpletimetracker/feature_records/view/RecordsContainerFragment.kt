package com.example.util.simpletimetracker.feature_records.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.core.extension.setOnLongClick
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.SnackBarParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_records.databinding.RecordsContainerFragmentBinding as Binding

@AndroidEntryPoint
class RecordsContainerFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

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

    override fun initUi(): Unit = with(binding) {
        pagerRecordsContainer.apply {
            adapter = RecordsContainerAdapter(this@RecordsContainerFragment)
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    override fun initUx() = with(binding) {
        btnRecordAdd.setOnClick(viewModel::onRecordAddClick)
        btnRecordsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnRecordsContainerNext.setOnClick(viewModel::onNextClick)
        btnRecordsContainerToday.setOnClick(viewModel::onTodayClick)
        btnRecordsContainerToday.setOnLongClick(viewModel::onTodayLongClick)
    }

    override fun initViewModel() = with(binding) {
        with(viewModel) {
            title.observe(::updateTitle)
            position.observe {
                pagerRecordsContainer.setCurrentItem(
                    it + RecordsContainerAdapter.FIRST,
                    viewPagerSmoothScroll
                )
            }
        }
        with(removeRecordViewModel) {
            message.observe(::showMessage)
        }
    }

    override fun onDateTimeSet(timestamp: Long, tag: String?) {
        viewModel.onDateTimeSet(timestamp, tag)
    }

    private fun updateTitle(title: String) {
        binding.btnRecordsContainerToday.text = title
    }

    private fun showMessage(message: SnackBarParams?) {
        if (message != null && message.tag == SnackBarParams.TAG.RECORD_DELETE) {
            router.show(Notification.SNACK_BAR, message, binding.btnRecordAdd)
            removeRecordViewModel.onMessageShown()
        }
    }

    companion object {
        var viewPagerSmoothScroll: Boolean = true
        fun newInstance() = RecordsContainerFragment()
    }
}