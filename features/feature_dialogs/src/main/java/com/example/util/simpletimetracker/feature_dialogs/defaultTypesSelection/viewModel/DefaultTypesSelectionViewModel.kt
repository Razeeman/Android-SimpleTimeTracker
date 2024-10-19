package com.example.util.simpletimetracker.feature_dialogs.defaultTypesSelection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.info.InfoViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.defaultTypesSelection.interactor.GetDefaultRecordTypesInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DefaultTypesSelectionViewModel @Inject constructor(
    private val router: Router,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val getDefaultRecordTypesInteractor: GetDefaultRecordTypesInteractor,
    private val resourceRepo: ResourceRepo,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) : ViewModel() {

    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadRecordTypesViewData()
            }
            initial
        }
    }
    val close: LiveData<Unit> = MutableLiveData()
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(false)

    private var recordTypes: List<RecordType> = emptyList()
    private var typeIdsSelected: MutableList<Long> = mutableListOf()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        typeIdsSelected.addOrRemove(item.id)
        updateSaveButtonEnabled()
        updateRecordTypesViewData()
    }

    fun onShowAllClick() {
        typeIdsSelected.addAll(recordTypes.map { it.id })
        updateSaveButtonEnabled()
        updateRecordTypesViewData()
    }

    fun onHideAllClick() {
        typeIdsSelected.clear()
        updateSaveButtonEnabled()
        updateRecordTypesViewData()
    }

    fun onSaveClick() {
        if (typeIdsSelected.size == 0) return

        saveButtonEnabled.set(false)
        viewModelScope.launch {
            recordTypes.filter { it.id in typeIdsSelected }.forEach {
                // Remove ids for correct adding in database.
                recordTypeInteractor.add(it.copy(id = 0))
            }
            if (!prefsInteractor.hasCardOrder()) {
                prefsInteractor.setCardOrder(CardOrder.COLOR)
            }
            externalViewsInteractor.onDefaultTypesAdd()
            close.set(Unit)
        }
    }

    fun onHideClick() {
        router.navigate(
            StandardDialogParams(
                tag = HIDE_ALERT_DIALOG_TAG,
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            HIDE_ALERT_DIALOG_TAG -> hide()
        }
    }

    private fun hide() {
        viewModelScope.launch {
            prefsInteractor.setDefaultTypesHidden(true)
            close.set(Unit)
        }
    }

    private fun updateSaveButtonEnabled() {
        saveButtonEnabled.set(typeIdsSelected.size != 0)
    }

    private fun updateRecordTypesViewData() = viewModelScope.launch {
        val data = loadRecordTypesViewData()
        types.set(data)
    }

    private suspend fun loadRecordTypesViewData(): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()

        if (recordTypes.isEmpty()) recordTypes = loadRecordTypes()

        fun map(type: RecordType): ViewHolderType {
            return recordTypeViewDataMapper.map(
                recordType = type,
                numberOfCards = numberOfCards,
                isDarkTheme = isDarkTheme,
                isChecked = null,
                isComplete = false,
            )
        }

        val selected = recordTypes
            .filter { it.id in typeIdsSelected }
            .map(::map)
        val available = recordTypes
            .filter { it.id !in typeIdsSelected }
            .map(::map)

        val result = mutableListOf<ViewHolderType>()

        if (selected.isNotEmpty()) {
            result += InfoViewData(resourceRepo.getString(R.string.something_selected))
            result += selected
        } else {
            result += InfoViewData(resourceRepo.getString(R.string.nothing_selected))
        }
        if (available.isNotEmpty()) {
            result += DividerViewData(0)
        }
        result += available

        return result
    }

    private fun loadRecordTypes(): List<RecordType> {
        return getDefaultRecordTypesInteractor.execute()
    }

    companion object {
        private const val HIDE_ALERT_DIALOG_TAG = "hide_alert_dialog_tag"
    }
}
