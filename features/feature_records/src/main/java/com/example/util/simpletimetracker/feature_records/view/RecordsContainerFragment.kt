package com.example.util.simpletimetracker.feature_records.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.di.BaseViewModelFactory
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.sharedViewModel.RemoveRecordViewModel
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.core.view.SafeFragmentStateAdapter
import com.example.util.simpletimetracker.feature_records.adapter.RecordsContainerAdapter
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerPosition
import com.example.util.simpletimetracker.feature_records.model.RecordsOptionsSwitchState
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_records.databinding.RecordsContainerFragmentBinding as Binding

@AndroidEntryPoint
class RecordsContainerFragment :
    BaseFragment<Binding>(),
    DateTimeDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override val insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    @Inject
    lateinit var removeRecordViewModelFactory: BaseViewModelFactory<RemoveRecordViewModel>

    @Inject
    lateinit var router: Router

    private val viewModel: RecordsContainerViewModel by viewModels()
    private val removeRecordViewModel: RemoveRecordViewModel by activityViewModels(
        factoryProducer = { removeRecordViewModelFactory },
    )

    override fun initUi(): Unit = with(binding) {
        pagerRecordsContainer.apply {
            adapter = SafeFragmentStateAdapter(RecordsContainerAdapter(this@RecordsContainerFragment))
            offscreenPageLimit = 1
            isUserInputEnabled = false
        }
    }

    override fun initUx() = with(binding) {
        btnRecordAdd.setOnClick(throttle(viewModel::onRecordAddClick))
        btnRecordsContainerOptions.setOnClick(viewModel::onOptionsClick)
        btnRecordsContainerFilter.setOnClick(throttle(viewModel::onFilterClick))
        btnRecordsContainerShare.setOnClick(throttle(viewModel::onShareClick))
        btnRecordsContainerCalendarSwitch.setOnClick(viewModel::onCalendarSwitchClick)
        btnRecordsContainerPrevious.setOnClick(viewModel::onPreviousClick)
        btnRecordsContainerNext.setOnClick(viewModel::onNextClick)
        btnRecordsContainerToday.setOnClick(viewModel::onTodayClick)
        btnRecordsContainerToday.setOnLongClick(viewModel::onTodayLongClick)
    }

    override fun initViewModel() {
        with(viewModel) {
            title.observe(::updateTitle)
            position.observe(::setPosition)
            optionsSwitchState.observe(::setOptionsSwitchState)
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
            router.show(message, binding.btnRecordAdd)
            removeRecordViewModel.onMessageShown()
        }
    }

    private fun setPosition(data: RecordsContainerPosition) = with(binding) {
        pagerRecordsContainer.setCurrentItem(
            data.position + RecordsContainerAdapter.FIRST,
            data.animate && viewPagerSmoothScroll,
        )
    }

    private fun setOptionsSwitchState(
        data: RecordsOptionsSwitchState,
    ) = with(binding) {
        when (data.state) {
            is RecordsOptionsSwitchState.State.Opened -> {
                btnRecordsContainerFilter.visible = true
                btnRecordsContainerShare.visible = true
                btnRecordsContainerCalendarSwitch.visible =
                    data.calendarSwitchState is RecordsOptionsSwitchState.CalendarSwitchState.Visible
            }
            is RecordsOptionsSwitchState.State.Closed -> {
                btnRecordsContainerFilter.visible = false
                btnRecordsContainerShare.visible = false
                btnRecordsContainerCalendarSwitch.visible = false
            }
        }

        ivRecordsContainerOptions.setImageResource(data.moreIconResId)
        if (data.calendarSwitchState is RecordsOptionsSwitchState.CalendarSwitchState.Visible) {
            ivRecordsContainerCalendarSwitch.setImageResource(data.calendarSwitchState.iconResId)
        }
    }

    companion object {
        @VisibleForTesting
        var viewPagerSmoothScroll: Boolean = true

        fun newInstance() = RecordsContainerFragment()
    }
}