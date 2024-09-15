package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.CategoryDBO
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Category
import javax.inject.Inject

class CategoryDataLocalMapper @Inject constructor() {

    fun map(dbo: CategoryDBO): Category {
        return Category(
            id = dbo.id,
            name = dbo.name,
            color = AppColor(
                colorId = dbo.color,
                colorInt = dbo.colorInt,
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