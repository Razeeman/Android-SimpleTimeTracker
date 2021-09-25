package com.example.util.simpletimetracker.feature_change_record_type.viewData

sealed class ChangeRecordTypeScrollViewData {

    data class ScrollTo(val position: Int) : ChangeRecordTypeScrollViewData()
    object NoScroll : ChangeRecordTypeScrollViewData()
}