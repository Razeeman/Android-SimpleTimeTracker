package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import javax.inject.Inject

class CommonViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapSelectedHint(isEmpty: Boolean): ViewHolderType {
        return InfoViewData(
            text = if (isEmpty) {
                R.string.nothing_selected
            } else {
                R.string.something_selected
            }.let(resourceRepo::getString),
        )
    }
}