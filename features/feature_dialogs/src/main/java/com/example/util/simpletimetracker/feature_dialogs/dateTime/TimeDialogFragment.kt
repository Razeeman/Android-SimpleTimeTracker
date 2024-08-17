package com.example.util.simpletimetracker.feature_dialogs.dateTime

import com.example.util.simpletimetracker.feature_dialogs.databinding.TimeDialogFragmentBinding as Binding
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import java.util.Calendar

class TimeDialogFragment : BaseFragment<Binding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override val insetConfiguration: InsetConfiguration =
        InsetConfiguration.DoNotApply

    interface OnTimeSetListener {
        fun onTimeSet(hourOfDay: Int, minute: Int, seconds: Int)
    }

    var listener: OnTimeSetListener? = null

    private val timestamp: Long by lazy {
        arguments?.getLong(ARGS_TIMESTAMP).orZero()
    }
    private val useMilitaryTime: Boolean by lazy {
        arguments?.getBoolean(ARGS_MILITARY).orFalse()
    }
    private val showSeconds: Boolean by lazy {
        arguments?.getBoolean(ARGS_SECONDS).orFalse()
    }

    override fun initUi(): Unit = with(binding) {
        val calendar = Calendar.getInstance()
            .apply { timeInMillis = timestamp }

        timePicker.apply {
            setIs24HourView(useMilitaryTime)
            currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            currentMinute = calendar.get(Calendar.MINUTE)
            setOnTimeChangedListener { _, hourOfDay, minute ->
                listener?.onTimeSet(hourOfDay, minute, getSeconds())
            }
        }

        inputTimePickerSeconds.isVisible = showSeconds
        inputTimePickerSeconds.requestFocus()
        etTimePickerSeconds.filters = listOf(MinMaxFilter(0, 59)).toTypedArray()
        etTimePickerSeconds.setText(calendar.get(Calendar.SECOND).toString())
        etTimePickerSeconds.doAfterTextChanged {
            val data = getSelectedTime()
            listener?.onTimeSet(data.first, data.second, data.third)
        }
    }

    fun getSelectedTime(): Triple<Int, Int, Int> = with(binding) {
        return Triple(timePicker.currentHour, timePicker.currentMinute, getSeconds())
    }

    private fun getSeconds(): Int = with(binding) {
        return etTimePickerSeconds.text?.toString()?.toIntOrNull().orZero()
    }

    inner class MinMaxFilter(
        private val min: Int,
        private val max: Int,
    ) : InputFilter {

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dStart: Int,
            dEnd: Int,
        ): CharSequence? {
            val input = (dest.toString() + source.toString()).toIntOrNull().orZero()
            if (input in min..max) return null
            return ""
        }
    }

    companion object {
        private const val ARGS_TIMESTAMP = "args_timestamp"
        private const val ARGS_MILITARY = "args_military"
        private const val ARGS_SECONDS = "args_seconds"

        fun newInstance(
            timestamp: Long,
            useMilitaryTime: Boolean,
            showSeconds: Boolean,
        ) = TimeDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARGS_TIMESTAMP, timestamp)
                putBoolean(ARGS_MILITARY, useMilitaryTime)
                putBoolean(ARGS_SECONDS, showSeconds)
            }
        }
    }
}