package com.example.util.simpletimetracker.feature_records.adapter

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.example.util.simpletimetracker.core.adapter.ViewHolderType

data class RecordViewData(
    var id: Long,
    var name: String,
    var timeStarted: String,
    var timeFinished: String,
    var duration: String,
    @DrawableRes val iconId: Int,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getViewType(): Int = ViewHolderType.VIEW
}