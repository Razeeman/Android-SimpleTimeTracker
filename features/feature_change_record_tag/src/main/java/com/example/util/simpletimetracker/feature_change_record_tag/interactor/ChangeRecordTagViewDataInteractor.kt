package com.example.util.simpletimetracker.feature_change_record_tag.interactor

import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.ButtonsRowItemViewData
import com.example.util.simpletimetracker.feature_base_adapter.commentField.CommentFieldViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_change_record_tag.R
import com.example.util.simpletimetracker.feature_change_record_tag.mapper.ChangeRecordTagMapper
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagButtonsRowId
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypesViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagValueTypeViewData
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagValueViewData
import javax.inject.Inject

class ChangeRecordTagViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeRecordTagMapper: ChangeRecordTagMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    suspend fun getTypesViewData(
        selectedTypes: Set<Long>,
        initialTypeIds: Set<Long>,
    ): ChangeRecordTagTypesViewData {
        return getViewData(
            selectedTypes = selectedTypes,
            initialTypeIds = initialTypeIds,
            hintViewDataProvider = { nothingSelected ->
                changeRecordTagMapper.mapHint(nothingSelected)
            },
        )
    }

    suspend fun getDefaultTypesViewData(
        selectedTypes: Set<Long>,
    ): ChangeRecordTagTypesViewData {
        return getViewData(
            selectedTypes = selectedTypes,
            initialTypeIds = emptySet(),
            hintViewDataProvider = {
                changeRecordTagMapper.mapDefaultTypeHint()
            },
        )
    }

    fun getTagValueState(
        valueType: RecordTagValueType,
        valueSuffix: String,
        fromValueChange: Boolean,
    ): ChangeRecordTagValueViewData {
        fun mapValueTypeName(type: RecordTagValueType): String {
            return when (type) {
                RecordTagValueType.NONE -> R.string.change_record_type_goal_time_disabled
                RecordTagValueType.NUMERIC -> R.string.settings_dark_mode_enabled
            }.let(resourceRepo::getString)
        }

        val result = mutableListOf<ViewHolderType>()

        result += HintViewData(
            text = resourceRepo.getString(R.string.change_record_type_value_type_hint),
        )
        result += ButtonsRowItemViewData(
            block = ChangeRecordTagButtonsRowId.VALUE_TYPE,
            marginTopDp = 0,
            data = listOf(
                RecordTagValueType.NONE,
                RecordTagValueType.NUMERIC,
            ).map {
                ChangeRecordTagValueTypeViewData(
                    valueType = it,
                    name = mapValueTypeName(it),
                    isSelected = it == valueType,
                )
            },
        )
        if (valueType == RecordTagValueType.NUMERIC) {
            result += CommentFieldViewData(
                id = "change_record_tag_value_suffix".hashCode().toLong(),
                text = if (fromValueChange) null else valueSuffix,
                marginTopDp = 0,
                marginHorizontal = resourceRepo.getDimenInDp(R.dimen.edit_screen_margin_horizontal),
                hint = resourceRepo.getString(R.string.change_record_type_value_suffix),
                valueType = CommentFieldViewData.ValueType.TextSingleLine,
            )
        }

        return ChangeRecordTagValueViewData(
            hint = mapValueTypeName(valueType),
            viewData = result,
        )
    }

    private suspend fun getViewData(
        selectedTypes: Set<Long>,
        initialTypeIds: Set<Long>,
        hintViewDataProvider: (nothingSelected: Boolean) -> ViewHolderType,
    ): ChangeRecordTagTypesViewData {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val data = recordTypeInteractor.getAll()
            .filter { !it.hidden || (it.id in initialTypeIds) }

        return if (data.isNotEmpty()) {
            val selected = data.filter { it.id in selectedTypes }
            val available = data.filter { it.id !in selectedTypes }

            val viewData = mutableListOf<ViewHolderType>()

            hintViewDataProvider(selected.isEmpty()).let(viewData::add)

            commonViewDataMapper.mapSelectedHint(
                isEmpty = selected.isEmpty(),
            ).let(viewData::add)

            selected.map {
                recordTypeViewDataMapper.map(
                    recordType = it,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    checkStates = emptyList(),
                    isComplete = false,
                )
            }.let(viewData::addAll)

            DividerViewData(1)
                .takeUnless { available.isEmpty() }
                ?.let(viewData::add)

            available.map {
                recordTypeViewDataMapper.map(
                    recordType = it,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    checkStates = emptyList(),
                    isComplete = false,
                )
            }.let(viewData::addAll)

            ChangeRecordTagTypesViewData(
                selectedCount = selected.size,
                viewData = viewData,
            )
        } else {
            ChangeRecordTagTypesViewData(
                selectedCount = 0,
                viewData = recordTypeViewDataMapper.mapToEmpty(),
            )
        }
    }
}