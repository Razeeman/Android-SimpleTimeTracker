package com.example.util.simpletimetracker.feature_dialogs.cardOrder.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CardOrderViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper
) : ViewModel() {

    val recordTypes: LiveData<List<ViewHolderType>> by lazy {
        updateRecordTypes()
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }

    private var numberOfCards: Int = runBlocking { prefsInteractor.getNumberOfCards() }
    private var types: List<RecordType> = emptyList()

    fun onCardMoved(from: Int, to: Int) {
        // TODO update inner order
    }

    fun onDismiss() {
        // TODO save prefs
    }

    private fun updateRecordTypes() = viewModelScope.launch {
        if (types.isEmpty()) types = loadRecordTypes()
        (recordTypes as MutableLiveData).value = types
            .map { type -> recordTypeViewDataMapper.map(type, numberOfCards) }
    }

    private suspend fun loadRecordTypes(): List<RecordType> {
        return recordTypeInteractor.getAll().filter { !it.hidden }
    }
}
