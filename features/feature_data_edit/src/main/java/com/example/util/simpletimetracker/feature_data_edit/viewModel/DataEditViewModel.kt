package com.example.util.simpletimetracker.feature_data_edit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DataEditViewModel @Inject constructor(
    private val router: Router,
    private val dateEditViewDataInteractor: DateEditViewDataInteractor,
) : ViewModel() {

    val selectedRecordsCountViewData: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch { initial.value = loadSelectedRecordsCountViewData() }
            initial
        }
    }

    private var filters: List<RecordsFilter> = emptyList()

    fun onSelectRecordsClick() {
        router.setResultListener(FILTER_TAG) {
            (it as? List<*>)?.filterIsInstance<RecordsFilter>()?.let(::onFilterSelected)
        }
        RecordsFilterParams(
            tag = FILTER_TAG,
            filters = filters.map(RecordsFilter::toParams),
        ).let(router::navigate)
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

    companion object {
        private const val FILTER_TAG = "date_edit_filter_tag"
    }
}
