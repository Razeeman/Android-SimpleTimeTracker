package com.example.util.simpletimetracker.feature_dialogs.typesSelection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_dialogs.typesSelection.interactor.TypesSelectionViewDataInteractor
import com.example.util.simpletimetracker.feature_dialogs.typesSelection.model.TypesSelectionCacheHolder
import com.example.util.simpletimetracker.feature_dialogs.typesSelection.viewData.TypesSelectionDialogViewData
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TypesSelectionViewModel @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val typesSelectionViewDataInteractor: TypesSelectionViewDataInteractor,
) : ViewModel() {

    lateinit var extra: TypesSelectionDialogParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }
    val viewState: LiveData<TypesSelectionDialogViewData> by lazy {
        MutableLiveData(loadViewState())
    }
    val onDataSelected: LiveData<List<Long>> = MutableLiveData()
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var initialized: Boolean = false
    private var viewDataCache: List<TypesSelectionCacheHolder> = emptyList()
    private var dataIdsSelected: MutableList<Long> = mutableListOf()

    fun onRecordTypeClick(item: RecordTypeViewData) {
        if (extra.isMultiSelectAvailable) {
            dataIdsSelected.addOrRemove(item.id)
            updateViewData()
        } else {
            onDataSelected.set(listOf(item.id))
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        if (extra.isMultiSelectAvailable) {
            dataIdsSelected.addOrRemove(item.id)
            updateViewData()
        } else {
            onDataSelected.set(listOf(item.id))
        }
    }

    fun onShowAllClick() {
        dataIdsSelected.addAll(viewDataCache.map(TypesSelectionCacheHolder::id))
        updateViewData()
    }

    fun onHideAllClick() {
        dataIdsSelected.clear()
        updateViewData()
    }

    fun onSaveClick() {
        saveButtonEnabled.set(false)
        viewModelScope.launch {
            onDataSelected.set(dataIdsSelected)
        }
    }

    private fun loadViewState(): TypesSelectionDialogViewData {
        return TypesSelectionDialogViewData(
            title = extra.title,
            subtitle = extra.subtitle,
            isButtonsVisible = extra.isMultiSelectAvailable,
        )
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val types = recordTypeInteractor.getAll()

        if (!initialized) {
            viewDataCache = typesSelectionViewDataInteractor.loadCache(
                extra = extra,
                types = types,
            )
            val viewDataIds = viewDataCache.map { it.id }
            dataIdsSelected = extra
                .selectedTypeIds
                // Remove non existent ids.
                .filter { it in viewDataIds }
                .toMutableList()
            initialized = true
        }

        return typesSelectionViewDataInteractor.getViewData(
            extra = extra,
            types = types,
            dataIdsSelected = dataIdsSelected,
            viewDataCache = viewDataCache,
        )
    }
}
