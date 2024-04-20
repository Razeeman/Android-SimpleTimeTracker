package com.example.util.simpletimetracker.feature_change_category.interactor

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_category.mapper.ChangeCategoryMapper
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryTypesViewData
import javax.inject.Inject

class ChangeCategoryViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeCategoryMapper: ChangeCategoryMapper,
) {

    suspend fun getTypesViewData(selectedTypes: List<Long>): ChangeCategoryTypesViewData {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val types = recordTypeInteractor.getAll()
            .filter { !it.hidden }

        return if (types.isNotEmpty()) {
            val selected = types.filter { it.id in selectedTypes }
            val available = types.filter { it.id !in selectedTypes }
            val viewData = mutableListOf<ViewHolderType>()

            changeCategoryMapper.mapSelectedTypesHint(
                isEmpty = selected.isEmpty(),
            ).let(viewData::add)

            selected.map {
                recordTypeViewDataMapper.map(
                    recordType = it,
                    numberOfCards = numberOfCards,
                    isDarkTheme = isDarkTheme,
                    isChecked = null,
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
                )
            }.let(viewData::addAll)

            ChangeCategoryTypesViewData(
                selectedCount = selected.size,
                viewData = viewData,
            )
        } else {
            ChangeCategoryTypesViewData(
                selectedCount = 0,
                viewData = recordTypeViewDataMapper.mapToEmpty(),
            )
        }
    }
}