package com.example.util.simpletimetracker.feature_data_edit.model

import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData

sealed interface DataEditAddTagsState {

    object Disabled : DataEditAddTagsState
    data class Enabled(val viewData: List<CategoryViewData.Record>) : DataEditAddTagsState
}