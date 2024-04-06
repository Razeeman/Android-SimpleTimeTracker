package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

sealed class ChangeRecordTypeScrollViewData {

    data class ScrollTo(val position: Int) : ChangeRecordTypeScrollViewData()
    object NoScroll : ChangeRecordTypeScrollViewData()
}