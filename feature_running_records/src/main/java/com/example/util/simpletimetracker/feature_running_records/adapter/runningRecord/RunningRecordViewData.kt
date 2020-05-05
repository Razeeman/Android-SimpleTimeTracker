package com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord

import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RunningRecordViewData(
    var id: Long,
    var name: String,
    var timeString: String
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}