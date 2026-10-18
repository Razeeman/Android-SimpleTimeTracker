package com.example.util.simpletimetracker.data_local.activityFilter

import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.domain.color.model.AppColor
import javax.inject.Inject

class ActivityFilterDataLocalMapper @Inject constructor() {

    fun map(dbo: ActivityFilterDBO): ActivityFilter {
        return ActivityFilter(
            id = dbo.id,
            selectedIds = dbo.selectedIds
                .split(',')
                .mapNotNull(String::toLongOrNull)
                .toSet(),
            type = when (dbo.type) {
                0L -> ActivityFilter.Type.Activity
                1L -> ActivityFilter.Type.Category
                else -> ActivityFilter.Type.Activity
            },
            name = dbo.name,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt,
                gradient = null,
            ),
            selected = dbo.selected,
        )
    }

    fun map(domain: ActivityFilter): ActivityFilterDBO {
        return ActivityFilterDBO(
            id = domain.id,
            selectedIds = domain.selectedIds
                .joinToString(separator = ","),
            type = when (domain.type) {
                is ActivityFilter.Type.Activity -> 0L
                is ActivityFilter.Type.Category -> 1L
            },
            name = domain.name,
            color = domain.color.colorId,
            colorInt = domain.color.colorInt,
            selected = domain.selected,
        )
    }
}