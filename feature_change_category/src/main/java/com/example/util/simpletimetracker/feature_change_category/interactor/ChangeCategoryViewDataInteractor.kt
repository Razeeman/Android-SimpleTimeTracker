package com.example.util.simpletimetracker.feature_change_category.interactor

import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.divider.DividerViewData
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_category.mapper.ChangeCategoryMapper
import javax.inject.Inject

class ChangeCategoryViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeCategoryMapper: ChangeCategoryMapper
) {

    suspend fun getTypesViewData(selectedTypes: List<Long>): List<ViewHolderType> {
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

                changeCategoryMapper.mapSelectedTypesHint(
                    isEmpty = selected.isEmpty()
                ).let(viewData::add)

                selected.map {
                    recordTypeViewDataMapper.map(
                        recordType = it,
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                DividerViewData
                    .takeUnless { available.isEmpty() }
                    ?.let(viewData::add)

                available.map {
                    recordTypeViewDataMapper.map(
                        recordType = it,
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme
                    )
                }.let(viewData::addAll)

                viewData
            }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }
}