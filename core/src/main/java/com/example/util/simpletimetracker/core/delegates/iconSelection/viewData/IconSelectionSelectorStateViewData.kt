package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.domain.model.IconImageState

sealed interface IconSelectionSelectorStateViewData {

    data class Available(
        val state: IconImageState,
        @ColorInt val searchButtonColor: Int,
        @ColorInt val favouriteButtonColor: Int,
    ) : IconSelectionSelectorStateViewData

    object None : IconSelectionSelectorStateViewData
}