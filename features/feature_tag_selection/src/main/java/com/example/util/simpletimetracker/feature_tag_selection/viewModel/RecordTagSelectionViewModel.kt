package com.example.util.simpletimetracker.feature_tag_selection.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_tag_selection.interactor.RecordTagSelectionViewDataInteractor
import com.example.util.simpletimetracker.feature_tag_selection.viewData.RecordTagSelectionViewState
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordTagSelectionViewModel @Inject constructor(
    private val viewDataInteractor: RecordTagSelectionViewDataInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val prefsInteractor: PrefsInteractor,
) : BaseViewModel() {

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
    val saveButtonVisibility: LiveData<Boolean> by lazySuspend { loadButtonVisibility() }
    val viewState: LiveData<RecordTagSelectionViewState> by lazySuspend { loadViewState() }
    val saveClicked: LiveData<Unit> = MutableLiveData()

    private var newComment: String = ""
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
                saveClicked()
            } else {
                updateViewData()
            }
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            saveClicked()
        }
    }

    fun onCommentChange(text: String) {
        if (newComment != text) {
            newComment = text
        }
    }

    private suspend fun saveClicked() {
        addRunningRecordMediator.startTimer(
            typeId = extra.typeId,
            tagIds = newCategoryIds,
            comment = newComment,
        )
        saveClicked.set(Unit)
    }

    private suspend fun loadButtonVisibility(): Boolean {
        val closeAfterOneTag = prefsInteractor.getRecordTagSelectionCloseAfterOne()
        val showTags = RecordTagSelectionParams.Field.Tags in extra.fields
        val showCommentInput = RecordTagSelectionParams.Field.Comment in extra.fields

        return when {
            showTags -> !closeAfterOneTag
            showCommentInput -> true
            else -> false
        }
    }

    private fun loadViewState(): RecordTagSelectionViewState {
        val fields = extra.fields.map {
            when (it) {
                is RecordTagSelectionParams.Field.Tags ->
                    RecordTagSelectionViewState.Field.Tags
                is RecordTagSelectionParams.Field.Comment ->
                    RecordTagSelectionViewState.Field.Comment
            }
        }
        return RecordTagSelectionViewState(fields)
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        viewData.set(data)
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return viewDataInteractor.getViewData(
            typeId = extra.typeId,
            selectedTags = newCategoryIds,
        )
    }
}
