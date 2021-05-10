package com.example.util.simpletimetracker.feature_change_category.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.info.InfoViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_change_category.R
import javax.inject.Inject

class ChangeCategoryMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    fun mapSelectedTypesHint(isEmpty: Boolean): ViewHolderType {
        return InfoViewData(
            text = if (isEmpty) {
                R.string.change_category_selected_types_empty
            } else {
                R.string.change_category_selected_types_hint
            }.let(resourceRepo::getString)
        )
    }
}