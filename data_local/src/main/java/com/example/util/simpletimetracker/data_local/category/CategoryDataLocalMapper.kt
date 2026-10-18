package com.example.util.simpletimetracker.data_local.category

import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.color.model.AppColor
import javax.inject.Inject

class CategoryDataLocalMapper @Inject constructor() {

    fun map(dbo: CategoryDBO): Category {
        return Category(
            id = dbo.id,
            name = dbo.name,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt,
                gradient = null,
            ),
            note = dbo.note,
        )
    }

    fun map(domain: Category): CategoryDBO {
        return CategoryDBO(
            id = domain.id,
            name = domain.name,
            color = domain.color.colorId,
            colorInt = domain.color.colorInt,
            note = domain.note,
        )
    }
}