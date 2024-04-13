package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

sealed class IconSelectionScrollViewData {

    data class ScrollTo(val position: Int) : IconSelectionScrollViewData()
    object NoScroll : IconSelectionScrollViewData()
}