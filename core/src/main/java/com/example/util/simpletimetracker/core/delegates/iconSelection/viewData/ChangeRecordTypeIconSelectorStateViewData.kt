package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.domain.model.IconImageState

sealed interface ChangeRecordTypeIconSelectorStateViewData {

    data class Available(
        val state: IconImageState,
        val searchButtonIsVisible: Boolean,
        @ColorInt val searchButtonColor: Int,
    ) : ChangeRecordTypeIconSelectorStateViewData

    object None : ChangeRecordTypeIconSelectorStateViewData
}