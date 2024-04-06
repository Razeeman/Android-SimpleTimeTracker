package com.example.util.simpletimetracker.core.delegates.iconSelection.viewData

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

sealed interface ChangeRecordTypeIconStateViewData {

    data class Icons(
        val items: List<ViewHolderType>,
    ) : ChangeRecordTypeIconStateViewData

    object Text : ChangeRecordTypeIconStateViewData
}