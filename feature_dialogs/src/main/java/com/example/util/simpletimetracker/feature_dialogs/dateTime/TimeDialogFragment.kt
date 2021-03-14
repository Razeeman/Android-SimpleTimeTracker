package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_dialogs.R
import kotlinx.android.synthetic.main.time_dialog_fragment.*
import java.util.Calendar

class TimeDialogFragment : Fragment() {

    interface OnTimeSetListener {
        fun onTimeSet(hourOfDay: Int, minute: Int)
    }

    var listener: OnTimeSetListener? = null

    private val timestamp: Long by lazy {
        arguments?.getLong(ARGS_TIMESTAMP).orZero()
    }
    private val useMilitaryTime: Boolean by lazy {
        arguments?.getBoolean(ARGS_MILITARY).orFalse()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.time_dialog_fragment,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = Calendar.getInstance()
            .apply { timeInMillis = timestamp }

        timePicker.apply {
            setIs24HourView(useMilitaryTime)
            currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            currentMinute = calendar.get(Calendar.MINUTE)
            setOnTimeChangedListener { _, hourOfDay, minute ->
                listener?.onTimeSet(hourOfDay, minute)
            }
        }
    }

    fun getSelectedTime(): Pair<Int, Int> {
        return timePicker.currentHour to timePicker.currentMinute
    }

    companion object {
        private const val ARGS_TIMESTAMP = "args_timestamp"
        private const val ARGS_MILITARY = "args_military"

        fun newInstance(
            timestamp: Long,
            useMilitaryTime: Boolean
        ) = TimeDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARGS_TIMESTAMP, timestamp)
                putBoolean(ARGS_MILITARY, useMilitaryTime)
            }
        }
    }
}