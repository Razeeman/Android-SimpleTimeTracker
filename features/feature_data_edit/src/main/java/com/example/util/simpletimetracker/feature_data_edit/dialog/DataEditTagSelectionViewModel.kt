package com.example.util.simpletimetracker.feature_data_edit.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.RecordTagViewDataInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTagSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DataEditTagSelectionViewModel @Inject constructor(
    private val recordTagViewDataInteractor: RecordTagViewDataInteractor,
) : ViewModel() {

    lateinit var extra: DataEditTagSelectionDialogParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }
    val tagSelected: LiveData<List<Long>> = MutableLiveData()

    private var selectedIds: MutableList<Long> = mutableListOf()

    fun onTagClick(item: CategoryViewData) {
        viewModelScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    selectedIds.addOrRemove(item.id)
                }
                is CategoryViewData.Record.Untagged -> {
                    selectedIds.clear()
                }
                else -> return@launch
            }
            updateViewData()
        }
    }

    fun onSaveClick() {
        tagSelected.set(selectedIds)
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val result: MutableList<ViewHolderType> = mutableListOf()

        recordTagViewDataInteractor.getViewData(
            selectedTags = selectedIds,
            typeId = extra.typeId,
            multipleChoiceAvailable = true,
            showAddButton = false,
            showArchived = true,
            showUntaggedButton = false,
        ).data.let(result::addAll)

        return result
    }
}
