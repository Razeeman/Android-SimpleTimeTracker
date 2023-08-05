package com.example.util.simpletimetracker.feature_running_records.mapper

import androidx.annotation.StringRes
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeCardSizeMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class RunningRecordsViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val recordTypeCardSizeMapper: RecordTypeCardSizeMapper,
) {

    fun map(
        recordType: RecordType,
        isFiltered: Boolean,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.mapFiltered(
            recordType,
            numberOfCards,
            isDarkTheme,
            isFiltered,
        )
    }

    fun mapToTypesEmpty(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(
                R.string.running_records_types_empty,
                resourceRepo.getString(R.string.running_records_add_type),
                resourceRepo.getString(R.string.running_records_add_default),
            ),
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString),
            hint = R.string.running_records_empty_hint.let(resourceRepo::getString),
        )
    }

    fun mapToHasRunningRecords(): ViewHolderType {
        return HintViewData(
            text = R.string.running_records_has_timers.let(resourceRepo::getString),
            paddingVertical = 0,
        )
    }

    fun mapToAddItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Add,
            name = R.string.running_records_add_type,
            icon = RecordTypeIcon.Image(R.drawable.add),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )
    }

    fun mapToAddDefaultItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Default,
            name = R.string.running_records_add_default,
            icon = RecordTypeIcon.Image(R.drawable.add),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )
    }

    fun mapToRepeatItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return mapToSpecial(
            type = RunningRecordTypeSpecialViewData.Type.Repeat,
            name = R.string.running_records_repeat,
            icon = RecordTypeIcon.Image(R.drawable.repeat),
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme,
        )
    }

    private fun mapToSpecial(
        type: RunningRecordTypeSpecialViewData.Type,
        @StringRes name: Int,
        icon: RecordTypeIcon,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        return RunningRecordTypeSpecialViewData(
            type = type,
            name = name.let(resourceRepo::getString),
            iconId = icon,
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards),
        )
    }
}