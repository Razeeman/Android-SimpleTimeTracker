package com.example.util.simpletimetracker.feature_running_records.adapter

import com.example.util.simpletimetracker.core.di.adapter.ViewHolderType

data class RunningRecordViewData(
    var id: Long = 0,
    var name: String,
    var timeStarted: Long,
    var timeEnded: Long
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}