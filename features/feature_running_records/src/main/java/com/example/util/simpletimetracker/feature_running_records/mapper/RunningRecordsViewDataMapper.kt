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
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_running_records.R
import com.example.util.simpletimetracker.feature_running_records.viewData.RunningRecordTypeAddViewData
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
            isFiltered
        )
    }

    fun mapToTypesEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_types_empty.let(resourceRepo::getString)
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString),
            hint = R.string.running_records_empty_hint.let(resourceRepo::getString)
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
    ): RunningRecordTypeAddViewData {
        return mapToAdd(
            type = RunningRecordTypeAddViewData.Type.Add,
            name = R.string.running_records_add_type,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme
        )
    }

    fun mapToAddDefaultItem(
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeAddViewData {
        return mapToAdd(
            type = RunningRecordTypeAddViewData.Type.Default,
            name = R.string.running_records_add_default,
            numberOfCards = numberOfCards,
            isDarkTheme = isDarkTheme
        )
    }

    private fun mapToAdd(
        type: RunningRecordTypeAddViewData.Type,
        @StringRes name: Int,
        numberOfCards: Int,
        isDarkTheme: Boolean,
    ): RunningRecordTypeAddViewData {
        return RunningRecordTypeAddViewData(
            type = type,
            name = name.let(resourceRepo::getString),
            iconId = RecordTypeIcon.Image(R.drawable.add),
            color = colorMapper.toInactiveColor(isDarkTheme),
            width = recordTypeCardSizeMapper.toCardWidth(numberOfCards),
            height = recordTypeCardSizeMapper.toCardHeight(numberOfCards),
            asRow = recordTypeCardSizeMapper.toCardAsRow(numberOfCards)
        )
    }
}