package com.example.util.simpletimetracker.feature_running_records.adapter.recordType

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.di.adapter.ViewHolderType

data class RecordTypeViewData(
    var name: String,
    @ColorInt var color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}