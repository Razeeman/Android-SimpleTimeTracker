package com.example.util.simpletimetracker.feature_tag_selection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_tag_selection.interactor.RecordTagSelectionViewDataInteractor
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class RecordTagSelectionViewModel @Inject constructor(
    private val viewDataInteractor: RecordTagSelectionViewDataInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator
) : ViewModel() {

    lateinit var extra: RecordTagSelectionParams

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initial.value = listOf(LoaderViewData())
                initial.value = loadViewData()
            }
            initial
        }
    }
    val tagSelected: LiveData<Unit> = MutableLiveData()

    fun onCategoryClick(item: CategoryViewData) {
        if (item !is CategoryViewData.Record) return

        val tagId = when (item) {
            is CategoryViewData.Record.Tagged -> item.id
            is CategoryViewData.Record.General -> item.id
            is CategoryViewData.Record.Untagged -> 0L
        }

        viewModelScope.launch {
            addRunningRecordMediator.startTimer(extra.typeId, tagId)
            tagSelected.set(Unit)
        }
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return viewDataInteractor.getViewData(extra.typeId)
    }
}
