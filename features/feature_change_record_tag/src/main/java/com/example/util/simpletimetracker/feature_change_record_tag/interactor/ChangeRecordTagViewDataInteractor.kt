package com.example.util.simpletimetracker.feature_change_record_tag.interactor

import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_change_record_tag.mapper.ChangeRecordTagMapper
import javax.inject.Inject

class ChangeRecordTagViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeRecordTagMapper: ChangeRecordTagMapper,
) {

    suspend fun getTypesViewData(selectedTypes: Set<Long>): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return recordTypeInteractor.getAll()
            .filter { !it.hidden }
            .takeUnless(List<RecordType>::isEmpty)
            ?.let { types ->
                val selected = types.filter { it.id in selectedTypes }
                val available = types.filter { it.id !in selectedTypes }
                selected to available
            }
            ?.let { (selected, available) ->
                val viewData = mutableListOf<ViewHolderType>()

                changeRecordTagMapper.mapHint(
                    nothingSelected = selected.isEmpty(),
                ).let(viewData::add)

                changeRecordTagMapper.mapSelectedTypesHint(
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

                viewData
            }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }
}