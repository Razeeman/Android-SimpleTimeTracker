package com.example.util.simpletimetracker.feature_dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.extension.getAllFragments
import com.example.util.simpletimetracker.core.extension.onTabSelected
import com.example.util.simpletimetracker.core.extension.visible
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.navigation.params.DateTimeDialogParams
import kotlinx.android.synthetic.main.date_time_dialog_fragment.*

class DateTimeDialogFragment : AppCompatDialogFragment() {

    private var dateTimeListener: DateTimeListener? = null
    private val dialogTag: String? by lazy { arguments?.getString(ARGS_TAG) }
    private var newTimestamp: Long = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            is DateTimeListener -> {
                dateTimeListener = context
                return
            }
            is AppCompatActivity -> {
                context.getAllFragments()
                    .firstOrNull { it is DateTimeListener }
                    ?.let { dateTimeListener = it as? DateTimeListener }
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

        val timestamp: Long = arguments?.getLong(ARGS_TIMESTAMP, 0).orZero()
        newTimestamp = timestamp

        childFragmentManager.commit {
            replace(
                R.id.datePickerContainer,
                DateDialogFragment.newInstance(timestamp)
                    .apply { onDateSetListener = { newTimestamp = it } }
            )
        }
        childFragmentManager.commit {
            replace(
                R.id.timePickerContainer,
                TimeDialogFragment.newInstance(timestamp)
                    .apply { onTimeSetListener = { newTimestamp = it } }
            )
        }

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

        btnDateTimeDialogPositive.setOnClickListener {
            dateTimeListener?.onDateTimeSet(newTimestamp, dialogTag)
            dismiss()
        }
    }

    companion object {
        private const val ARGS_TAG = "tag"
        private const val ARGS_TIMESTAMP = "timestamp"

        fun createBundle(data: Any?): Bundle = Bundle().apply {
            when (data) {
                is DateTimeDialogParams -> {
                    putString(ARGS_TAG, data.tag)
                    putLong(ARGS_TIMESTAMP, data.timestamp)
                }
            }
        }
    }
}