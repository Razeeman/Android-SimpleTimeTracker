package com.example.util.simpletimetracker.feature_data_edit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.repo.DataEditRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditChangeInteractor
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditViewDataInteractor
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditAddTagsState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTagSelectionDialogParams
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
    private val recordTagInteractor: RecordTagInteractor,
) : ViewModel() {

    val selectedRecordsCountViewData: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch { initial.value = loadSelectedRecordsCountViewData() }
            initial
        }
    }
    val changeActivityState: LiveData<DataEditChangeActivityState> by lazy {
        MutableLiveData(typeState)
    }
    val changeCommentState: LiveData<DataEditChangeCommentState> by lazy {
        MutableLiveData(commentState)
    }
    val addTagsState: LiveData<DataEditAddTagsState> by lazy {
        MutableLiveData(addTagState)
    }
    val changeButtonState: LiveData<DataEditChangeButtonState> by lazy {
        MutableLiveData<DataEditChangeButtonState>().let { initial ->
            viewModelScope.launch { initial.value = loadChangeButtonState() }
            initial
        }
    }
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private var filters: List<RecordsFilter> = emptyList()
    private var typeState: DataEditChangeActivityState = DataEditChangeActivityState.Disabled
    private var commentState: DataEditChangeCommentState = DataEditChangeCommentState.Disabled
    private var addTagState: DataEditAddTagsState = DataEditAddTagsState.Disabled

    private val changeButtonEnabled: Boolean
        get() = typeState is DataEditChangeActivityState.Enabled ||
            commentState is DataEditChangeCommentState.Enabled ||
            addTagState is DataEditAddTagsState.Enabled

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
        if (typeState is DataEditChangeActivityState.Disabled) {
            router.navigate(DataEditTypeSelectionDialogParams)
        } else {
            typeState = DataEditChangeActivityState.Disabled
            checkAddTagStateConsistency()
            updateChangeActivityState()
            updateChangeButtonState()
        }
    }

    fun onTypeSelected(typeId: Long) = viewModelScope.launch {
        typeState = dataEditViewDataInteractor.getChangeActivityState(typeId)
        checkAddTagStateConsistency()
        updateChangeActivityState()
        updateChangeButtonState()
    }

    fun onTypeDismissed() {
        updateChangeActivityState()
    }

    fun onChangeCommentClick() {
        if (commentState is DataEditChangeCommentState.Disabled) {
            commentState = dataEditViewDataInteractor.getChangeCommentState("")
        } else {
            commentState = DataEditChangeCommentState.Disabled
            keyboardVisibility.set(false)
        }
        updateChangeCommentState()
        updateChangeButtonState()
    }

    fun onAddTagsClick() {
        if (addTagState is DataEditAddTagsState.Disabled) {
            // Show tag selection with typed tags for changed activity (if it is selected)
            // or for filtered activity (if it is only one).
            // Otherwise show only general tags.
            val typeId = getTypeForTagSelection().orZero()
            router.navigate(DataEditTagSelectionDialogParams(typeId))
        } else {
            addTagState = DataEditAddTagsState.Disabled
            updateAddTagState()
            updateChangeButtonState()
        }
    }

    fun onTagsSelected(tagIds: List<Long>) = viewModelScope.launch {
        addTagState = dataEditViewDataInteractor.getAddTagState(tagIds)
        updateAddTagState()
        updateChangeButtonState()
    }

    fun onTagsDismissed() {
        updateAddTagState()
    }

    fun onCommentChange(text: String) {
        val state = commentState
        if (state is DataEditChangeCommentState.Enabled && state.viewData != text) {
            commentState = DataEditChangeCommentState.Enabled(text)
            updateChangeCommentState()
        }
    }

    fun onChangeClick() = viewModelScope.launch {
        changeButtonState.set(dataEditViewDataInteractor.getChangeButtonState(false))
        dataEditRepo.inProgress.set(true)
        dataEditChangeInteractor.changeData(
            typeState = typeState,
            commentState = commentState,
            addTagState = addTagState,
            filters = filters,
        )
        dataEditRepo.inProgress.set(false)
        showMessage(R.string.data_edit_success_message)
        router.back()
    }

    private fun onFilterSelected(filters: List<RecordsFilter>) {
        this.filters = filters
        checkAddTagStateConsistency()
        updateSelectedRecordsCountViewData()
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    private fun checkAddTagStateConsistency() = viewModelScope.launch {
        // If there are some tags selected to add,
        val tags = (addTagState as? DataEditAddTagsState.Enabled)
            ?.viewData
            ?: return@launch
        val allTags = recordTagInteractor.getAll().associateBy(RecordTag::id)
        // Find if there is a specific type selected by filter or change activity state,
        val typeId = getTypeForTagSelection().orZero()
        // Filter tags selected to add to have typed tags only for this selected activity.
        val newTags = tags.filter {
            val tag = allTags[it.id] ?: return@filter false
            tag.typeId == 0L || tag.typeId == typeId
        }

        addTagState = if (newTags.isNotEmpty()) {
            DataEditAddTagsState.Enabled(newTags)
        } else {
            DataEditAddTagsState.Disabled
        }
        updateAddTagState()
        updateChangeButtonState()
    }

    private fun getTypeForTagSelection(): Long? {
        return (typeState as? DataEditChangeActivityState.Enabled)
            ?.viewData?.id
            ?: filters
                .filterIsInstance<RecordsFilter.Activity>()
                .firstOrNull()
                ?.typeIds
                ?.takeIf { it.size == 1 }
                ?.firstOrNull()
    }

    private fun updateSelectedRecordsCountViewData() = viewModelScope.launch {
        val data = loadSelectedRecordsCountViewData()
        selectedRecordsCountViewData.set(data)
    }

    private suspend fun loadSelectedRecordsCountViewData(): String {
        return dataEditViewDataInteractor.getSelectedRecordsCount(filters)
    }

    private fun updateChangeActivityState() = viewModelScope.launch {
        changeActivityState.set(typeState)
    }

    private fun updateChangeCommentState() = viewModelScope.launch {
        changeCommentState.set(commentState)
    }

    private fun updateAddTagState() = viewModelScope.launch {
        addTagsState.set(addTagState)
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
