package com.example.util.simpletimetracker.feature_change_record_type.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.adapter.hint.HintViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_change_record_type.R
import javax.inject.Inject

class ChangeRecordTypeMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    fun mapToEmpty(): List<ViewHolderType> {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_record_type_categories_empty)
        ).let(::listOf)
    }

    fun mapSelectedCategoriesHint(isEmpty: Boolean): ViewHolderType {
        return HintViewData(
            text = if (isEmpty) {
                R.string.change_record_type_selected_categories_empty
            } else {
                R.string.change_record_type_selected_categories_hint
            }.let(resourceRepo::getString)
        )
    }
}