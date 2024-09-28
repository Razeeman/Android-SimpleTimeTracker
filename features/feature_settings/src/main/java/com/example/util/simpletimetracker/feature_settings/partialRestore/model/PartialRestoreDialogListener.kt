package com.example.util.simpletimetracker.feature_settings.partialRestore.model

interface PartialRestoreDialogListener {

    fun onDataSelected(
        tag: String?,
        filters: Map<PartialRestoreFilterType, List<Long>>,
    )
}