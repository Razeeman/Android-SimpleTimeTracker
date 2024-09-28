package com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model

import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType

interface PartialRestoreSelectionDialogListener {

    fun onDataSelected(
        tag: String?,
        type: PartialRestoreFilterType,
        dataIds: Set<Long>,
    )
}