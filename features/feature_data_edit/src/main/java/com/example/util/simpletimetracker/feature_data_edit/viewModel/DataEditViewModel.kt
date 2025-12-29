package com.example.util.simpletimetracker.feature_data_edit.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.RecordFilterInteractor
import com.example.util.simpletimetracker.core.repo.DataEditRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orEmpty
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.record.model.RecordsFilter
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_data_edit.R
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditChangeInteractor
import com.example.util.simpletimetracker.feature_data_edit.interactor.DateEditViewDataInteractor
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditAddTagsState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeActivityState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeButtonState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditChangeCommentState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditDeleteRecordsState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditRecordsCountState
import com.example.util.simpletimetracker.feature_data_edit.model.DataEditRemoveTagsState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditDuplicateTypeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTagSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditTypeSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsFilterResultParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataEditViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val dataEditRepo: DataEditRepo,
    private val dataEditViewDataInteractor: DateEditViewDataInteractor,
    private val dataEditChangeInteractor: DateEditChangeInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val recordFilterInteractor: RecordFilterInteractor,
) : ViewModel() {

    val selectedRecordsCountViewData: LiveData<DataEditRecordsCountState> by lazy {
        return@lazy MutableLiveData<DataEditRecordsCountState>().let { initial ->
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
    val removeTagsState: LiveData<DataEditRemoveTagsState> by lazy {
        MutableLiveData(removeTagState)
    }
    val deleteRecordsState: LiveData<DataEditDeleteRecordsState> by lazy {
        MutableLiveData(deleteState)
    }
    val changeButtonState: LiveData<DataEditChangeButtonState> by lazy {
        MutableLiveData<DataEditChangeButtonState>().let { initial ->
            viewModelScope.launch { initial.value = loadChangeButtonState() }
            initial
        }
    }
    val deleteTodayRecordsButtonText: LiveData<String> = MutableLiveData()
    val disableButtons: LiveData<Unit> = SingleLiveEvent<Unit>()
    val keyboardVisibility: LiveData<Boolean> = MutableLiveData(false)

    private var filters: List<RecordsFilter> = emptyList()
    private var todayFilters: List<RecordsFilter> = emptyList()
    private var todayFiltersInitial: List<RecordsFilter> = emptyList()
    private var typeState: DataEditChangeActivityState = DataEditChangeActivityState.Disabled
    private var commentState: DataEditChangeCommentState = DataEditChangeCommentState.Disabled
    private var addTagState: DataEditAddTagsState = DataEditAddTagsState.Disabled
    private var removeTagState: DataEditRemoveTagsState = DataEditRemoveTagsState.Disabled
    private var deleteState: DataEditDeleteRecordsState = DataEditDeleteRecordsState.Disabled

    private val changeButtonEnabled: Boolean
        get() = selectedRecordsCountViewData.value?.count.orZero() != 0 && (
            typeState is DataEditChangeActivityState.Enabled ||
                commentState is DataEditChangeCommentState.Enabled ||
                addTagState is DataEditAddTagsState.Enabled ||
                removeTagState is DataEditRemoveTagsState.Enabled ||
                deleteState is DataEditDeleteRecordsState.Enabled
            )

    fun initialize() = viewModelScope.launch {
        initializeTodayFilters()
        updateDeleteTodayRecordsButtonText()
    }

    fun onSelectRecordsClick() {
        onSelectRecordsClick(FILTER_TAG, filters)
    }

    fun onViewTodayRecordsClick() {
        onSelectRecordsClick(TODAY_FILTER_TAG, todayFilters)
    }

    fun onChangeActivityClick() {
        if (typeState is DataEditChangeActivityState.Disabled) {
            router.navigate(DataEditTypeSelectionDialogParams)
        } else {
            typeState = DataEditChangeActivityState.Disabled
            checkTagStateConsistency()
            updateChangeActivityState()
            updateChangeButtonState()
        }
    }

    fun onTypeSelected(typeId: Long) = viewModelScope.launch {
        typeState = dataEditViewDataInteractor.getChangeActivityState(typeId)
        checkTagStateConsistency()
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
            viewModelScope.launch {
                openTagSelection(ADD_TAGS_TAG, showValueSelection = true)
            }
        } else {
            addTagState = DataEditAddTagsState.Disabled
            updateAddTagState()
            updateChangeButtonState()
        }
    }

    fun onRemoveTagsClick() {
        if (removeTagState is DataEditRemoveTagsState.Disabled) {
            viewModelScope.launch {
                openTagSelection(REMOVE_TAGS_TAG, showValueSelection = false)
            }
        } else {
            removeTagState = DataEditRemoveTagsState.Disabled
            updateRemoveTagState()
            updateChangeButtonState()
        }
    }

    fun onDeleteRecordsClick() {
        if (deleteState is DataEditDeleteRecordsState.Disabled) {
            deleteState = DataEditDeleteRecordsState.Enabled

            typeState = DataEditChangeActivityState.Disabled
            commentState = DataEditChangeCommentState.Disabled
            addTagState = DataEditAddTagsState.Disabled
            removeTagState = DataEditRemoveTagsState.Disabled

            updateChangeActivityState()
            updateChangeCommentState()
            updateAddTagState()
            updateRemoveTagState()
        } else {
            deleteState = DataEditDeleteRecordsState.Disabled
        }
        updateDeleteRecordsState()
        updateChangeButtonState()
    }

    fun onTagsSelected(tag: String, tagIds: List<RecordBase.Tag>) = viewModelScope.launch {
        when (tag) {
            ADD_TAGS_TAG -> {
                addTagState = dataEditViewDataInteractor.getTagState(tagIds)
                    .takeUnless { it.isEmpty() }
                    ?.let { DataEditAddTagsState.Enabled(tagIds, it) }
                    ?: DataEditAddTagsState.Disabled
                updateAddTagState()
                updateChangeButtonState()
            }
            REMOVE_TAGS_TAG -> {
                removeTagState = dataEditViewDataInteractor.getTagState(tagIds)
                    .takeUnless { it.isEmpty() }
                    ?.let(DataEditRemoveTagsState::Enabled)
                    ?: DataEditRemoveTagsState.Disabled
                updateRemoveTagState()
                updateChangeButtonState()
            }
        }
    }

    fun onTagsDismissed() {
        updateAddTagState()
        updateRemoveTagState()
    }

    fun onCommentChange(text: String) {
        val state = commentState
        if (state is DataEditChangeCommentState.Enabled && state.viewData != text) {
            commentState = DataEditChangeCommentState.Enabled(text)
            updateChangeCommentState()
        }
    }

    fun onChangeClick() {
        showAlert(tag = CHANGE_ALERT_DIALOG_TAG)
    }

    fun onDuplicateClick() {
        router.navigate(DataEditDuplicateTypeDialogParams)
    }

    fun onDeleteTodayRecordsClick() {
        showAlert(tag = DELETE_TODAY_RECORDS_ALERT_DIALOG_TAG)
    }

    fun onDeleteAllRecordsClick() {
        showAlert(tag = DELETE_RECORDS_ALERT_DIALOG_TAG)
    }

    fun onDeleteDataClick() {
        showAlert(tag = DELETE_DATA_ALERT_DIALOG_TAG)
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            CHANGE_ALERT_DIALOG_TAG -> onChangeConfirmed()
            DELETE_TODAY_RECORDS_ALERT_DIALOG_TAG -> onDeleteTodayRecordsConfirmed()
            DELETE_RECORDS_ALERT_DIALOG_TAG -> onDeleteRecordsConfirmed()
            DELETE_DATA_ALERT_DIALOG_TAG -> onDeleteDataConfirmed()
        }
    }

    fun onFilterSelected(result: RecordsFilterResultParams) {
        when (result.tag) {
            FILTER_TAG -> filters = result.filters
            TODAY_FILTER_TAG -> todayFilters = result.filters
        }
        // Update is on dismiss.
    }

    fun onFilterDismissed(tag: String) {
        when (tag) {
            FILTER_TAG -> {
                checkTagStateConsistency()
                updateSelectedRecordsCountViewData()
            }
            TODAY_FILTER_TAG -> {
                updateDeleteTodayRecordsButtonText()
            }
        }
    }

    private fun onSelectRecordsClick(
        tag: String,
        filters: List<RecordsFilter>,
    ) {
        RecordsFilterParams(
            tag = tag,
            title = resourceRepo.getString(R.string.chart_filter_hint),
            flags = RecordsFilterParams.Flags(
                dateSelectionAvailable = true,
                untrackedSelectionAvailable = false,
                multitaskSelectionAvailable = false,
                duplicationsSelectionAvailable = true,
                favouriteSelectionAvailable = true,
                addRunningRecords = false,
            ),
            filters = filters.map(RecordsFilter::toParams),
            defaultLastDaysNumber = 7,
        ).let(router::navigate)
    }

    private fun showAlert(tag: String) {
        router.navigate(
            StandardDialogParams(
                tag = tag,
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    private fun onChangeConfirmed() {
        doDataEditWork {
            dataEditChangeInteractor.changeData(
                typeState = typeState,
                commentState = commentState,
                addTagState = addTagState,
                removeTagState = removeTagState,
                deleteRecordsState = deleteState,
                filters = filters,
            )
        }
    }

    private fun onDeleteTodayRecordsConfirmed() {
        doDataEditWork {
            val ids = getTodayRecords().map { it.id }
            dataEditChangeInteractor.deleteTodayRecords(ids)
        }
    }

    private fun onDeleteRecordsConfirmed() {
        doDataEditWork {
            dataEditChangeInteractor.deleteAllRecords()
        }
    }

    private fun onDeleteDataConfirmed() {
        doDataEditWork {
            dataEditChangeInteractor.deleteAllData()
        }
    }

    private fun doDataEditWork(
        work: suspend () -> Unit,
    ) = viewModelScope.launch {
        disableButtons.set(Unit)
        dataEditRepo.inProgress.set(true)
        work.invoke()
        dataEditRepo.inProgress.set(false)
        showMessage(R.string.data_edit_success_message)
        delay(100) // wait for dialog to close.
        router.back()
    }

    private suspend fun openTagSelection(
        tag: String,
        showValueSelection: Boolean,
    ) {
        // Show tag selection with typed tags for changed activity (if it is selected)
        // or for filtered activities.
        // Otherwise show only general tags.
        val typeIds = getTypeForTagSelection()
        val params = DataEditTagSelectionDialogParams(
            tag = tag,
            typeIds = typeIds,
            showValueSelection = showValueSelection,
        )
        router.navigate(params)
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    private fun checkTagStateConsistency() = viewModelScope.launch {
        val tagsToAdd = (addTagState as? DataEditAddTagsState.Enabled)?.viewData
        val tagsDataToAdd = (addTagState as? DataEditAddTagsState.Enabled)?.tags
        val tagsToRemove = (removeTagState as? DataEditRemoveTagsState.Enabled)?.viewData

        // If there are some tags selected to add or remove.
        if (tagsToAdd == null && tagsToRemove == null) return@launch

        val typeToTags = recordTypeToTagInteractor.getAll()
        val typeForTagSelection = getTypeForTagSelection()

        if (tagsToAdd != null) {
            val newTags = dataEditViewDataInteractor.filterTags(
                typesForTagSelection = typeForTagSelection,
                tags = tagsToAdd,
                typesToTags = typeToTags,
            )
            val newTagsIds = newTags.map { it.id }
            val newTagsData = tagsDataToAdd.orEmpty().filter { it.tagId in newTagsIds }
            addTagState = if (newTags.isNotEmpty() && newTagsData.isNotEmpty()) {
                DataEditAddTagsState.Enabled(newTagsData, newTags)
            } else {
                DataEditAddTagsState.Disabled
            }
            updateAddTagState()
        }

        // If there are some tags selected to remove.
        if (tagsToRemove != null) {
            val newTags = dataEditViewDataInteractor.filterTags(
                typesForTagSelection = typeForTagSelection,
                tags = tagsToRemove,
                typesToTags = typeToTags,
            )
            removeTagState = if (newTags.isNotEmpty()) {
                DataEditRemoveTagsState.Enabled(newTags)
            } else {
                DataEditRemoveTagsState.Disabled
            }
            updateRemoveTagState()
        }

        updateChangeButtonState()
    }

    private suspend fun getTypeForTagSelection(): List<Long> {
        return (typeState as? DataEditChangeActivityState.Enabled)
            ?.viewData?.id?.let(::listOf)
            ?: dataEditViewDataInteractor.getSelectedTypeIds(filters)
    }

    private suspend fun getTodayRecords(): List<Record> {
        return recordFilterInteractor
            .getByFilter(todayFilters)
            .filterIsInstance<Record>()
    }

    private suspend fun initializeTodayFilters() {
        val filter = RecordsFilter.Date(range = RangeLength.Day, position = 0)
        val todayRange = recordFilterInteractor.getRange(filter)
        val todayFilter = RecordsFilter.Date(
            range = RangeLength.Custom(todayRange),
            position = 0,
        )
        todayFilters = listOf(todayFilter)
        todayFiltersInitial = todayFilters
    }

    private fun updateDeleteTodayRecordsButtonText() = viewModelScope.launch {
        deleteTodayRecordsButtonText.set(loadDeleteTodayRecordsButtonText())
    }

    private suspend fun loadDeleteTodayRecordsButtonText(): String {
        val recordsCounts = getTodayRecords().size
        val recordsHint = resourceRepo.getQuantityString(
            R.plurals.statistics_detail_times_tracked,
            recordsCounts,
        )
        val todayHint = if (todayFilters == todayFiltersInitial) {
            resourceRepo.getString(R.string.title_today)
        } else {
            null
        }

        return listOfNotNull(
            resourceRepo.getString(R.string.archive_dialog_delete),
            todayHint,
            "$recordsCounts $recordsHint",
        ).joinToString(separator = " - ")
    }

    private fun updateSelectedRecordsCountViewData() = viewModelScope.launch {
        val data = loadSelectedRecordsCountViewData()
        selectedRecordsCountViewData.set(data)
        updateChangeButtonState()
    }

    private suspend fun loadSelectedRecordsCountViewData(): DataEditRecordsCountState {
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

    private fun updateRemoveTagState() = viewModelScope.launch {
        removeTagsState.set(removeTagState)
    }

    private fun updateDeleteRecordsState() = viewModelScope.launch {
        deleteRecordsState.set(deleteState)
    }

    private fun updateChangeButtonState() = viewModelScope.launch {
        val data = loadChangeButtonState()
        changeButtonState.set(data)
    }

    private suspend fun loadChangeButtonState(): DataEditChangeButtonState {
        return dataEditViewDataInteractor.getChangeButtonState(changeButtonEnabled)
    }

    companion object {
        private const val FILTER_TAG = "DATA_EDIT_FILTER_TAG"
        private const val TODAY_FILTER_TAG = "DATA_EDIT_TODAY_FILTER_TAG"
        private const val ADD_TAGS_TAG = "DATA_EDIT_ADD_TAGS_TAG"
        private const val REMOVE_TAGS_TAG = "DATA_EDIT_REMOVE_TAGS_TAG"
        private const val CHANGE_ALERT_DIALOG_TAG = "DATA_EDIT_CHANGE_ALERT_DIALOG_TAG"
        private const val DELETE_TODAY_RECORDS_ALERT_DIALOG_TAG = "DELETE_TODAY_RECORDS_ALERT_DIALOG_TAG"
        private const val DELETE_RECORDS_ALERT_DIALOG_TAG = "DATA_EDIT_DELETE_RECORDS_ALERT_DIALOG_TAG"
        private const val DELETE_DATA_ALERT_DIALOG_TAG = "DATA_EDIT_DELETE_DATA_ALERT_DIALOG_TAG"
    }
}
