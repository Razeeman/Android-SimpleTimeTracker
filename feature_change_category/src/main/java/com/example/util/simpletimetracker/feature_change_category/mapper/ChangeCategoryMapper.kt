package com.example.util.simpletimetracker.feature_change_category.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_change_category.R
import javax.inject.Inject

class ChangeCategoryMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    fun mapToEmpty(): List<ViewHolderType> {
        return EmptyViewData(
            message = resourceRepo.getString(R.string.change_category_types_empty)
        ).let(::listOf)
    }
}