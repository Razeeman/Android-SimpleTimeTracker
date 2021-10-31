package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.dialog.DateTimeDialogListener
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.feature_views.extension.onTabSelected
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.fragmentArgumentDelegate
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.databinding.DateTimeDialogFragmentBinding
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class DateTimeDialogFragment :
    AppCompatDialogFragment(),
    DateDialogFragment.OnDateSetListener,
    TimeDialogFragment.OnTimeSetListener {

    @Inject
    lateinit var timeMapper: TimeMapper

    private val binding: DateTimeDialogFragmentBinding get() = _binding!!
    private var _binding: DateTimeDialogFragmentBinding? = null

    private var timeDialogFragment: TimeDialogFragment? = null
    private var dateDialogFragment: DateDialogFragment? = null
    private var dateTimeDialogListeners: MutableList<DateTimeDialogListener> = mutableListOf()
    private val params: DateTimeDialogParams by fragmentArgumentDelegate(
        key = ARGS_PARAMS, default = DateTimeDialogParams()
    )
    private var newTimestamp: Long = 0
    private val calendar = Calendar.getInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is DateTimeDialogListener -> {
                dateTimeDialogListeners.add(context)
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments().forEach {
                    (it as? DateTimeDialogListener)?.let(dateTimeDialogListeners::add)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DateTimeDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newTimestamp = params.timestamp
        initUi()
        initUx()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onDateSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        calendar.timeInMillis = newTimestamp

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        newTimestamp = calendar.timeInMillis
    }

    override fun onTimeSet(hourOfDay: Int, minute: Int) {
        calendar.timeInMillis = newTimestamp

        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        newTimestamp = calendar.timeInMillis
    }

    private fun initUi() {
        initFragments()
        initTabs()
    }

    private fun initUx() {
        binding.btnDateTimeDialogPositive.setOnClickListener {
            timeDialogFragment?.getSelectedTime()?.let { (hour, minute) ->
                onTimeSet(hour, minute)
            }
            dateDialogFragment?.getSelectedDate()?.let { (year, month, day) ->
                onDateSet(year, month, day)
            }
            dateTimeDialogListeners.forEach { it.onDateTimeSet(newTimestamp, params.tag) }
            dismiss()
        }
    }

    private fun initFragments() {
        val dayOfWeek = timeMapper.toCalendarDayOfWeek(params.firstDayOfWeek)
        childFragmentManager.commit {
            replace(
                R.id.datePickerContainer,
                DateDialogFragment.newInstance(params.timestamp, dayOfWeek)
                    .apply { listener = this@DateTimeDialogFragment }
                    .also { dateDialogFragment = it }
            )
        }

        if (params.type is DateTimeDialogType.DATETIME) {
            childFragmentManager.commit {
                replace(
                    R.id.timePickerContainer,
                    TimeDialogFragment.newInstance(params.timestamp, params.useMilitaryTime)
                        .apply { listener = this@DateTimeDialogFragment }
                        .also { timeDialogFragment = it }
                )
            }
        }
    }

    private fun initTabs() = with(binding) {
        when (val type = params.type) {
            is DateTimeDialogType.DATE -> {
                setDateTabOnly()
            }
            is DateTimeDialogType.DATETIME -> {
                tabsDateTimeDialog.onTabSelected(::changeTabsVisibility)
                when (type.initialTab) {
                    DateTimeDialogType.Tab.DATE -> tabsDateTimeDialog.getTabAt(0)?.select()
                    DateTimeDialogType.Tab.TIME -> tabsDateTimeDialog.getTabAt(1)?.select()
                }
            }
        }
    }

    private fun setDateTabOnly() = with(binding) {
        tabsDateTimeDialog.visible = false
        datePickerContainer.visible = true
        timePickerContainer.visible = false
    }

    private fun changeTabsVisibility(tab: TabLayout.Tab) = with(binding) {
        when (tab.position) {
            0 -> {
                datePickerContainer.visible = true
                timePickerContainer.visible = false
            }
            1 -> {
                datePickerContainer.visible = false
                timePickerContainer.visible = true
            }
        }
    }

    companion object {
        private const val ARGS_PARAMS = "params"

        fun createBundle(data: DateTimeDialogParams): Bundle = Bundle().apply {
            putParcelable(ARGS_PARAMS, data)
        }
    }
}