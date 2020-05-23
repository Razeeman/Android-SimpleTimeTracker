package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_dialogs.R
import kotlinx.android.synthetic.main.time_dialog_fragment.*
import java.util.*

class TimeDialogFragment : Fragment() {

    var onTimeSetListener: ((Long) -> Unit)? = null

    private val timestamp: Long by lazy {
        arguments?.getLong(ARGS_TIMESTAMP, 0).orZero()
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

        timePicker.setIs24HourView(true)
        timePicker.currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.currentMinute = calendar.get(Calendar.MINUTE)
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            onTimeSetListener?.invoke(calendar.timeInMillis)
        }
    }

    companion object {
        private const val ARGS_TIMESTAMP = "args_timestamp"

        fun newInstance(timestamp: Long) = TimeDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ARGS_TIMESTAMP, timestamp)
            }
        }
    }
}