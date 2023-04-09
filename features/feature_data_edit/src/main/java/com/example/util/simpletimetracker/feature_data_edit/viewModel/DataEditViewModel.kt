package com.example.util.simpletimetracker.feature_data_edit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditViewDataInteractor
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTypeSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DataEditViewModel @Inject constructor(
    private val router: Router,
    private val dateEditViewDataInteractor: DateEditViewDataInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
) : ViewModel() {

    val selectedRecordsCountViewData: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch { initial.value = loadSelectedRecordsCountViewData() }
            initial
        }
    }
    val changeActivityCheckbox: LiveData<DataEditChangeActivityState> by lazy {
        MutableLiveData<DataEditChangeActivityState>().let { initial ->
            viewModelScope.launch {
                initial.value = loadChangeActivityCheckbox()
            }
            initial
        }
    }

    private var filters: List<RecordsFilter> = emptyList()
    private var newTypeId: Long? = null

    fun onSelectRecordsClick() {
        router.setResultListener(FILTER_TAG) {
            (it as? List<*>)?.filterIsInstance<RecordsFilter>()?.let(::onFilterSelected)
        }
        RecordsFilterParams(
            tag = FILTER_TAG,
            filters = filters.map(RecordsFilter::toParams),
        ).let(router::navigate)
    }

    fun onChangeActivityClick() {
        if (newTypeId == null) {
            router.navigate(DataEditTypeSelectionDialogParams)
        } else {
            newTypeId = null
            updateChangeActivityCheckbox()
        }
    }

    fun onTypeSelected(typeId: Long) {
        newTypeId = typeId
        updateChangeActivityCheckbox()
    }

    private fun onFilterSelected(filters: List<RecordsFilter>) {
        this.filters = filters
        updateSelectedRecordsCountViewData()
    }

    private fun updateSelectedRecordsCountViewData() = viewModelScope.launch {
        val data = loadSelectedRecordsCountViewData()
        selectedRecordsCountViewData.set(data)
    }

    private suspend fun loadSelectedRecordsCountViewData(): String {
        return dateEditViewDataInteractor.getSelectedRecordsCount(filters)
    }

    private fun updateChangeActivityCheckbox() = viewModelScope.launch {
        val data = loadChangeActivityCheckbox()
        changeActivityCheckbox.set(data)
    }

    private suspend fun loadChangeActivityCheckbox(): DataEditChangeActivityState {
        // TODO move to interactor
        val type = newTypeId?.let { recordTypeInteractor.get(it) }
        return if (type == null) {
            DataEditChangeActivityState.Disabled
        } else {
            DataEditChangeActivityState.Enabled(
                recordTypeViewDataMapper.map(
                    recordType = type,
                    isDarkTheme = prefsInteractor.getDarkMode(),
                )
            )
        }
    }

    companion object {
        private const val FILTER_TAG = "date_edit_filter_tag"
    }
}
