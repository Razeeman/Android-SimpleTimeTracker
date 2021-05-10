package com.example.util.simpletimetracker.feature_dialogs.cardOrder.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.navigation.params.CardOrderDialogParams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

class CardOrderViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper
) : ViewModel() {

    lateinit var extra: CardOrderDialogParams

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordTypes()
            }
            initial
        }
    }

    private var types: List<RecordTypeViewData> = emptyList()

    fun onCardMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(types, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(types, i, i - 1)
            }
        }
    }

    fun onDismiss() {
        GlobalScope.launch {
            types
                .takeIf(List<RecordTypeViewData>::isNotEmpty)
                ?.mapIndexed { index, recordTypeViewData ->
                    recordTypeViewData.id to index.toLong()
                }
                ?.toMap()
                ?.let {
                    prefsInteractor.setCardOrder(CardOrder.MANUAL)
                    prefsInteractor.setCardOrderManual(it)
                }
        }
    }

    private suspend fun loadRecordTypes(): List<ViewHolderType> {
        val numberOfCards: Int = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        return recordTypeInteractor.getAll(extra.initialOrder)
            .filter { !it.hidden }
            .takeUnless { it.isEmpty() }
            ?.map { type -> recordTypeViewDataMapper.map(type, numberOfCards, isDarkTheme) }
            ?.also { types = it }
            ?: recordTypeViewDataMapper.mapToEmpty()
    }
}
