package com.example.util.simpletimetracker.data_local.mapper

import com.example.util.simpletimetracker.data_local.model.CategoryDBO
import com.example.util.simpletimetracker.domain.model.Category
import javax.inject.Inject

class CategoryDataLocalMapper @Inject constructor() {

    fun map(dbo: CategoryDBO): Category {
        return Category(
            id = dbo.id,
            name = dbo.name,
            color = dbo.color
        )
    }

    fun map(domain: Category): CategoryDBO {
        return CategoryDBO(
            id = domain.id,
            name = domain.name,
            color = domain.color
        )
    }
}