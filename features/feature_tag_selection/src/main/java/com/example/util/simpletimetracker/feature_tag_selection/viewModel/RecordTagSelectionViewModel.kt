package com.example.util.simpletimetracker.feature_tag_selection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_tag_selection.interactor.RecordTagSelectionViewDataInteractor
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordTagSelectionViewModel @Inject constructor(
    private val viewDataInteractor: RecordTagSelectionViewDataInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val prefsInteractor: PrefsInteractor,
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
    val saveButtonVisibility: LiveData<Boolean> by lazy {
        return@lazy MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = !prefsInteractor.getRecordTagSelectionCloseAfterOne()
            }
            initial
        }
    }
    val tagSelected: LiveData<Unit> = MutableLiveData()

    private var newCategoryIds: MutableList<Long> = mutableListOf()

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            when (item) {
                is CategoryViewData.Record.Tagged -> {
                    newCategoryIds.addOrRemove(item.id)
                }
                is CategoryViewData.Record.Untagged -> {
                    newCategoryIds.clear()
                }
                else -> return@launch
            }
            if (prefsInteractor.getRecordTagSelectionCloseAfterOne()) {
                tagSelected()
            } else {
                updateViewData()
            }
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            tagSelected()
        }
    }

    private suspend fun tagSelected() {
        addRunningRecordMediator.startTimer(extra.typeId, newCategoryIds, "")
        tagSelected.set(Unit)
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return viewDataInteractor.getViewData(extra.typeId, newCategoryIds)
    }
}
