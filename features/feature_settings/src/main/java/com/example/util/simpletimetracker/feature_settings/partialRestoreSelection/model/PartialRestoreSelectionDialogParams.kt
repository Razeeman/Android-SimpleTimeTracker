package com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model

import android.os.Parcelable
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams
import kotlinx.parcelize.Parcelize

@Parcelize
data class PartialRestoreSelectionDialogParams(
    val tag: String,
    val type: PartialRestoreFilterType,
    val filteredIds: Set<Long>,
) : Parcelable, ScreenParams {

    companion object {
        val Empty = PartialRestoreSelectionDialogParams(
            tag = "",
            type = PartialRestoreFilterType.Activities,
            filteredIds = emptySet(),
        )
    }
}