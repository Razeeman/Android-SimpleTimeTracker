package com.example.util.simpletimetracker.feature_data_edit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.repo.DataEditRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditChangeInteractor
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditViewDataInteractor
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTypeSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DataEditViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val dataEditRepo: DataEditRepo,
    private val dataEditViewDataInteractor: DateEditViewDataInteractor,
    private val dataEditChangeInteractor: DateEditChangeInteractor,
) : ViewModel() {

    val selectedRecordsCountViewData: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch { initial.value = loadSelectedRecordsCountViewData() }
            initial
        }
    }
    val changeActivityState: LiveData<DataEditChangeActivityState> by lazy {
        MutableLiveData<DataEditChangeActivityState>().let { initial ->
            viewModelScope.launch { initial.value = loadChangeActivityState() }
            initial
        }
    }
    val changeButtonState: LiveData<DataEditChangeButtonState> by lazy {
        MutableLiveData<DataEditChangeButtonState>().let { initial ->
            viewModelScope.launch { initial.value = loadChangeButtonState() }
            initial
        }
    }

    private var filters: List<RecordsFilter> = emptyList()
    private var newTypeId: Long? = null
    private val changeButtonEnabled: Boolean get() = newTypeId != null

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
            updateChangeActivityState()
            updateChangeButtonState()
        }
    }

    fun onTypeSelected(typeId: Long) {
        newTypeId = typeId
        updateChangeActivityState()
        updateChangeButtonState()
    }

    fun onChangeClick() = viewModelScope.launch {
        changeButtonState.set(dataEditViewDataInteractor.getChangeButtonState(false))
        dataEditRepo.inProgress.set(true)
        dataEditChangeInteractor.changeData(newTypeId, filters)
        dataEditRepo.inProgress.set(false)
        showMessage(R.string.data_edit_success_message)
        router.back()
    }

    private fun onFilterSelected(filters: List<RecordsFilter>) {
        this.filters = filters
        updateSelectedRecordsCountViewData()
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    private fun updateSelectedRecordsCountViewData() = viewModelScope.launch {
        val data = loadSelectedRecordsCountViewData()
        selectedRecordsCountViewData.set(data)
    }

    private suspend fun loadSelectedRecordsCountViewData(): String {
        return dataEditViewDataInteractor.getSelectedRecordsCount(filters)
    }

    private fun updateChangeActivityState() = viewModelScope.launch {
        val data = loadChangeActivityState()
        changeActivityState.set(data)
    }

    private suspend fun loadChangeActivityState(): DataEditChangeActivityState {
        return dataEditViewDataInteractor.getChangeActivityState(newTypeId)
    }

    private fun updateChangeButtonState() = viewModelScope.launch {
        val data = loadChangeButtonState()
        changeButtonState.set(data)
    }

    private suspend fun loadChangeButtonState(): DataEditChangeButtonState {
        return dataEditViewDataInteractor.getChangeButtonState(changeButtonEnabled)
    }

    companion object {
        private const val FILTER_TAG = "date_edit_filter_tag"
    }
}
