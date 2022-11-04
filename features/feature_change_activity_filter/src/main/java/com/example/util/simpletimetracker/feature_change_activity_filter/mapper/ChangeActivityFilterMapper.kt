package com.example.util.simpletimetracker.feature_change_activity_filter.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_activity_filter.R
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterTypeSwitchViewData
import javax.inject.Inject

class ChangeActivityFilterMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapToTypeSwitchViewData(type: ActivityFilter.Type): List<ViewHolderType> {
        return listOf(
            ActivityFilter.Type.Activity,
            ActivityFilter.Type.ActivityTag,
        ).map {
            ChangeActivityFilterTypeSwitchViewData(
                type = it,
                name = mapToTagTypeName(it),
                isSelected = it == type
            )
        }
    }

    private fun mapToTagTypeName(type: ActivityFilter.Type): String {
        return when (type) {
            ActivityFilter.Type.Activity -> R.string.activity_hint
            ActivityFilter.Type.ActivityTag -> R.string.activity_tag_hint
        }.let(resourceRepo::getString)
    }
}