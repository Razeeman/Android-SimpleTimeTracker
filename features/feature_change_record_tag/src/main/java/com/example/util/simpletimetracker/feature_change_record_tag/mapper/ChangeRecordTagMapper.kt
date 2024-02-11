package com.example.util.simpletimetracker.feature_change_record_tag.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeSetupViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypeSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.RecordTagType
import javax.inject.Inject

class ChangeRecordTagMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapToTagTypeSwitchViewData(tagType: RecordTagType): List<ViewHolderType> {
        return listOf(
            RecordTagType.GENERAL,
            RecordTagType.TYPED,
        ).map {
            ChangeRecordTagTypeSwitchViewData(
                tagType = it,
                name = mapToTagTypeName(it),
                isSelected = it == tagType,
            )
        }
    }

    fun mapToTagTypeSetupViewData(
        recordTagId: Long,
        typeId: Long,
        tagType: RecordTagType,
    ): ChangeRecordTagTypeSetupViewData {
        return ChangeRecordTagTypeSetupViewData(
            // Show color if changing existing general tag, or if creating new general tag.
            colorChooserVisibility = (recordTagId != 0L && typeId == 0L) || (recordTagId == 0L && tagType == RecordTagType.GENERAL),
            // Show types if creating new typed tag.
            typesChooserVisibility = recordTagId == 0L && tagType == RecordTagType.TYPED,
            hint = when (tagType) {
                RecordTagType.GENERAL -> R.string.change_record_tag_type_general_hint
                RecordTagType.TYPED -> R.string.change_record_tag_type_typed_hint
            }.let(resourceRepo::getString),
        )
    }

    private fun mapToTagTypeName(tagType: RecordTagType): String {
        return when (tagType) {
            RecordTagType.GENERAL -> R.string.change_record_tag_type_general
            RecordTagType.TYPED -> R.string.change_record_tag_type_typed
        }.let(resourceRepo::getString)
    }
}