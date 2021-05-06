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
import com.example.util.simpletimetracker.core.extension.onTabSelected
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.dateTime.di.DateTimeComponentProvider
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogType
import kotlinx.android.synthetic.main.date_time_dialog_fragment.btnDateTimeDialogPositive
import kotlinx.android.synthetic.main.date_time_dialog_fragment.datePickerContainer
import kotlinx.android.synthetic.main.date_time_dialog_fragment.tabsDateTimeDialog
import kotlinx.android.synthetic.main.date_time_dialog_fragment.timePickerContainer
import java.util.Calendar
import javax.inject.Inject

class DateTimeDialogFragment : AppCompatDialogFragment(),
    DateDialogFragment.OnDateSetListener,
    TimeDialogFragment.OnTimeSetListener {

    @Inject
    lateinit var timeMapper: TimeMapper

    private var timeDialogFragment: TimeDialogFragment? = null
    private var dateDialogFragment: DateDialogFragment? = null
    private var dateTimeDialogListeners: MutableList<DateTimeDialogListener> = mutableListOf()
    private val dialogTag: String? by lazy {
        arguments?.getString(ARGS_TAG)
    }
    private val type: DateTimeDialogType by lazy {
        arguments?.getSerializable(ARGS_TYPE) as? DateTimeDialogType
            ?: DateTimeDialogType.DATETIME
    }
    private val useMilitary: Boolean by lazy {
        arguments?.getBoolean(ARGS_MILITARY).orFalse()
    }
    private val timestamp: Long by lazy {
        arguments?.getLong(ARGS_TIMESTAMP).orZero()
    }
    private val firstDayOfWeek: DayOfWeek by lazy {
        arguments?.getSerializable(ARGS_FIRST_DAY_OF_WEEK) as? DayOfWeek
            ?: DayOfWeek.MONDAY
    }
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
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.date_time_dialog_fragment, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newTimestamp = timestamp
        initDi()
        initUi()
        initUx()
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

    private fun initDi() {
        (activity?.application as DateTimeComponentProvider)
            .dateTimeComponent
            ?.inject(this)
    }

    private fun initUi() {
        initFragments()
        initTabs()
    }

    private fun initUx() {
        btnDateTimeDialogPositive.setOnClickListener {
            timeDialogFragment?.getSelectedTime()?.let { (hour, minute) ->
                onTimeSet(hour, minute)
            }
            dateDialogFragment?.getSelectedDate()?.let { (year, month, day) ->
                onDateSet(year, month, day)
            }
            dateTimeDialogListeners.forEach { it.onDateTimeSet(newTimestamp, dialogTag) }
            dismiss()
        }
    }

    private fun initFragments() {
        val dayOfWeek = timeMapper.toCalendarDayOfWeek(firstDayOfWeek)
        childFragmentManager.commit {
            replace(
                R.id.datePickerContainer,
                DateDialogFragment.newInstance(timestamp, dayOfWeek)
                    .apply { listener = this@DateTimeDialogFragment }
                    .also { dateDialogFragment = it }
            )
        }

        if (type == DateTimeDialogType.DATETIME) {
            childFragmentManager.commit {
                replace(
                    R.id.timePickerContainer,
                    TimeDialogFragment.newInstance(timestamp, useMilitary)
                        .apply { listener = this@DateTimeDialogFragment }
                        .also { timeDialogFragment = it }
                )
            }
        }
    }

    private fun initTabs() {
        when (type) {
            DateTimeDialogType.DATE -> {
                tabsDateTimeDialog.visible = false
                datePickerContainer.visible = true
                timePickerContainer.visible = false
            }
            DateTimeDialogType.DATETIME -> {
                tabsDateTimeDialog.getTabAt(1)?.select()
                tabsDateTimeDialog.onTabSelected { tab ->
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
            }
        }
    }

    companion object {
        private const val ARGS_TAG = "tag"
        private const val ARGS_TYPE = "type"
        private const val ARGS_MILITARY = "military"
        private const val ARGS_TIMESTAMP = "timestamp"
        private const val ARGS_FIRST_DAY_OF_WEEK = "firstDayOfWeek"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is DateTimeDialogParams -> {
                    putString(ARGS_TAG, data.tag)
                    putSerializable(ARGS_TYPE, data.type)
                    putBoolean(ARGS_MILITARY, data.useMilitaryTime)
                    putLong(ARGS_TIMESTAMP, data.timestamp)
                    putSerializable(ARGS_FIRST_DAY_OF_WEEK, data.firstDayOfWeek)
                }
            }
        }
    }
}