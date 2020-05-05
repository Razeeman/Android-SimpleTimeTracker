package com.example.util.simpletimetracker.feature_running_records.adapter.runningRecord

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RunningRecordViewData(
    var name: String,
    var timeStarted: String,
    var timer: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}