package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.model.CalendarDate
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.domain.calendar.CalendarTypeProvider
import com.example.util.simpletimetracker.domain.calendar.model.CalendarType
import com.example.util.simpletimetracker.domain.extension.orZero
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import com.example.util.simpletimetracker.feature_dialogs.databinding.DateDialogFragmentBinding as Binding

@AndroidEntryPoint
class DateDialogFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    @Inject
    lateinit var calendarTypeProvider: CalendarTypeProvider

    @Inject
    lateinit var timeMapper: TimeMapper

    interface OnDateSetListener {
        fun onDateSet(date: CalendarDate)
    }

    var listener: OnDateSetListener? = null

    private var usePersianPicker: Boolean = false

    private val timestamp: Long by lazy {
        arguments?.getLong(ARGS_TIMESTAMP, 0).orZero()
    }
    private val firstDayOfWeek: Int by lazy {
        arguments?.getInt(ARGS_FIRST_DAY_OF_WEEK, Calendar.MONDAY)
            ?: Calendar.MONDAY
    }

    override fun initUi(): Unit = with(binding) {
        usePersianPicker = calendarTypeProvider.get() == CalendarType.PERSIAN
        val date = timeMapper.toCalendarDate(timestamp)
        datePicker.isVisible = !usePersianPicker
        persianDatePicker.isVisible = usePersianPicker

        if (usePersianPicker) {
            // Preserve the themed native picker's civil bounds, including its entire last day.
            persianDatePicker.initialize(
                selectedDate = date,
                minDate = timeMapper.toCalendarDate(datePicker.minDate),
                maxDate = timeMapper.toCalendarDate(datePicker.maxDate),
                firstDayOfWeek = timeMapper.toDayOfWeek(firstDayOfWeek),
                timeMapper = timeMapper,
            )
            persianDatePicker.onDateSet = { data ->
                listener?.onDateSet(data)
            }
        } else {
            datePicker.firstDayOfWeek = firstDayOfWeek
            datePicker.init(
                date.year,
                date.month - 1,
                date.day,
            ) { _, year, monthOfYear, dayOfMonth ->
                listener?.onDateSet(CalendarDate(year, monthOfYear + 1, dayOfMonth))
            }
        }
    }

    fun getSelectedDate(): CalendarDate = with(binding) {
        return if (usePersianPicker) {
            persianDatePicker.selectedDate
        } else {
            CalendarDate(datePicker.year, datePicker.month + 1, datePicker.dayOfMonth)
        }
    }

    companion object {
        private const val ARGS_TIMESTAMP = "args_timestamp"
        private const val ARGS_FIRST_DAY_OF_WEEK = "args_first_day_of_week"

        fun newInstance(
            timestamp: Long,
            firstDayOfWeek: Int,
        ) = DateDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARGS_TIMESTAMP, timestamp)
                putInt(ARGS_FIRST_DAY_OF_WEEK, firstDayOfWeek)
            }
        }
    }
}