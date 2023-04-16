package com.example.util.simpletimetracker.feature_data_edit.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_data_edit.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DataEditTypeSelectionViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
) : ViewModel() {

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }

    private var types: List<RecordType> = emptyList()

    private suspend fun getCache(): List<RecordType> {
        return types.takeUnless { it.isEmpty() } ?: recordTypeInteractor.getAll()
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        val typesViewData = getCache().map { type ->
            recordTypeViewDataMapper.map(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
            )
        }

        if (typesViewData.isNotEmpty()) {
            typesViewData.let(result::addAll)
        } else {
            HintViewData(resourceRepo.getString(R.string.record_types_empty)).let(result::add)
        }

        return result
    }
}
