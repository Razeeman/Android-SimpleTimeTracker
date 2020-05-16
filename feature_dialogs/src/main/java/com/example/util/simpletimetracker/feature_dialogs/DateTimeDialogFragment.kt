package com.example.util.simpletimetracker.feature_dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.commit
import com.example.util.simpletimetracker.core.extension.onTabSelected
import com.example.util.simpletimetracker.core.extension.visible
import kotlinx.android.synthetic.main.date_time_dialog_fragment.*

class DateTimeDialogFragment : AppCompatDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.date_time_dialog_fragment, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.commit {
            replace(R.id.datePickerContainer, DateDialogFragment.newInstance())
        }
        childFragmentManager.commit {
            replace(R.id.timePickerContainer, TimeDialogFragment.newInstance())
        }

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