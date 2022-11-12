package com.example.util.simpletimetracker.feature_change_category.mapper

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_change_category.R
import javax.inject.Inject

class ChangeCategoryMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    fun mapSelectedTypesHint(isEmpty: Boolean): ViewHolderType {
        return InfoViewData(
            text = if (isEmpty) {
                R.string.nothing_selected
            } else {
                R.string.something_selected
            }.let(resourceRepo::getString)
        )
    }
}