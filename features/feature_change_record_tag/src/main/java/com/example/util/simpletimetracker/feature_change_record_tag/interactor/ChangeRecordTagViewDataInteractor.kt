package com.example.util.simpletimetracker.feature_change_record_tag.interactor

import com.example.util.simpletimetracker.core.mapper.CommonViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_change_record_tag.mapper.ChangeRecordTagMapper
import com.example.util.simpletimetracker.feature_change_record_tag.viewData.ChangeRecordTagTypesViewData
import javax.inject.Inject

class ChangeRecordTagViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeRecordTagMapper: ChangeRecordTagMapper,
    private val commonViewDataMapper: CommonViewDataMapper,
) {

    suspend fun getTypesViewData(
        selectedTypes: Set<Long>,
    ): ChangeRecordTagTypesViewData {
        return getViewData(
            selectedTypes = selectedTypes,
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
            hintViewDataProvider = {
                changeRecordTagMapper.mapDefaultTypeHint()
            },
        )
    }

    private suspend fun getViewData(
        selectedTypes: Set<Long>,
        hintViewDataProvider: (nothingSelected: Boolean) -> ViewHolderType,
    ): ChangeRecordTagTypesViewData {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val data = recordTypeInteractor.getAll()
            .filter { !it.hidden }

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
                    isChecked = null,
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
                    isChecked = null,
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