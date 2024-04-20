package com.example.util.simpletimetracker.feature_change_record_tag.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import com.example.util.simpletimetracker.feature_change_record_tag.R
import javax.inject.Inject

class ChangeRecordTagMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapHint(nothingSelected: Boolean): ViewHolderType {
        val text = if (nothingSelected) {
            R.string.change_record_tag_type_general_hint
        } else {
            R.string.change_record_tag_type_typed_hint
        }
        return HintBigViewData(
            text = resourceRepo.getString(text),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapDefaultTypeHint(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.change_record_tag_default_types_hint),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapSelectedTypesHint(isEmpty: Boolean): ViewHolderType {
        return InfoViewData(
            text = if (isEmpty) {
                R.string.nothing_selected
            } else {
                R.string.something_selected
            }.let(resourceRepo::getString),
        )
    }
}