package com.example.util.simpletimetracker.feature_widget.configure.mapper

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_widget.R
import javax.inject.Inject

class WidgetViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo
) {

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.widget_empty.let(resourceRepo::getString)
        )
    }
}