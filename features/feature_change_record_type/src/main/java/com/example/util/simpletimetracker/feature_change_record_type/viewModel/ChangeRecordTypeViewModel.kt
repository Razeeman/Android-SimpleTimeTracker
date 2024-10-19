package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate.IconSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.iconSelection.viewModelDelegate.IconSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRecordTypeMediator
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.interactor.ChangeRecordTypeViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeAdditionalState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeCategoriesViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromChangeActivityParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.StandardDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.util.simpletimetracker.core.R as coreR

@HiltViewModel
class ChangeRecordTypeViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val timeMapper: TimeMapper,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val viewDataInteractor: ChangeRecordTypeViewDataInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val wearInteractor: WearInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val removeRecordTypeMediator: RemoveRecordTypeMediator,
    private val goalsViewModelDelegate: GoalsViewModelDelegate,
    private val colorSelectionViewModelDelegateImpl: ColorSelectionViewModelDelegateImpl,
    private val iconSelectionViewModelDelegateImpl: IconSelectionViewModelDelegateImpl,
) : ViewModel(),
    GoalsViewModelDelegate by goalsViewModelDelegate,
    ColorSelectionViewModelDelegate by colorSelectionViewModelDelegateImpl,
    IconSelectionViewModelDelegate by iconSelectionViewModelDelegateImpl {

    lateinit var extra: ChangeRecordTypeParams

    val recordType: LiveData<RecordTypeViewData> by lazy {
        return@lazy MutableLiveData<RecordTypeViewData>().let { initial ->
            viewModelScope.launch {
                initializeRecordTypeData()
                initial.value = loadRecordPreviewViewData()
            }
            initial
        }
    }
    val categories: LiveData<ChangeRecordTypeCategoriesViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeCategoriesViewData>().let { initial ->
            viewModelScope.launch {
                initializeSelectedCategories()
                initial.value = loadCategoriesViewData()
            }
            initial
        }
    }
    val chooserState: LiveData<ViewChooserStateDelegate.States> by lazy {
        return@lazy MutableLiveData(
            ViewChooserStateDelegate.States(
                current = ChangeRecordTypeChooserState.Closed,
                previous = ChangeRecordTypeChooserState.Closed,
            ),
        )
    }
    val additionalState: LiveData<ChangeRecordTypeAdditionalState> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeAdditionalState>().let { initial ->
            viewModelScope.launch {
                initial.value = loadAdditionalState()
            }
            initial
        }
    }
    val noteState: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadNoteState()
            }
            initial
        }
    }
    val archiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val duplicateButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val nameErrorMessage: LiveData<String> = MutableLiveData("")
    val archiveIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTypeId != 0L) }
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTypeId != 0L) }
    val statsIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTypeId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTypeId == 0L) }

    private val recordTypeId: Long get() = (extra as? ChangeRecordTypeParams.Change)?.id.orZero()
    private var initialCategories: Set<Long> = emptySet()
    private var newName: String = ""
    private var newCategories: MutableList<Long> = mutableListOf()
    private var newDefaultDuration: Long = 0
    private var newNote: String = ""

    init {
        colorSelectionViewModelDelegateImpl.attach(getColorSelectionDelegateParent())
        iconSelectionViewModelDelegateImpl.attach(getIconSelectionDelegateParent())
    }

    override fun onCleared() {
        (goalsViewModelDelegate as? ViewModelDelegate)?.clear()
        colorSelectionViewModelDelegateImpl.clear()
        iconSelectionViewModelDelegateImpl.clear()
        super.onCleared()
    }

    fun onVisible() = viewModelScope.launch {
        initializeSelectedCategories()
        updateCategoriesViewData()
        goalsViewModelDelegate.onGoalsVisible()
        // TODO think about how it can affect "newCategories" that was already selected.
        //  Or how to add tag already assigned to activity.
    }

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updateRecordPreviewViewData()
            }
        }
        viewModelScope.launch {
            val type = recordTypeInteractor.get(name)
            val error = if (type != null && type.id != recordTypeId) {
                resourceRepo.getString(R.string.change_record_message_name_exist)
            } else {
                ""
            }
            nameErrorMessage.set(error)
        }
    }

    fun onNoteChange(note: String) {
        if (note != newNote) {
            newNote = note
            updateNoteState()
        }
    }

    fun onColorChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.Color)
    }

    fun onIconChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.Icon)
    }

    fun onCategoryChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.Category)
    }

    fun onGoalTimeChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.GoalTime)
    }

    fun onAdditionalChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.Additional)
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            newCategories.addOrRemove(item.id)
            updateCategoriesViewData()
        }
    }

    fun onCategoryLongClick(item: CategoryViewData, sharedElements: Pair<Any, String>) {
        router.navigate(
            data = ChangeCategoryFromChangeActivityParams(
                ChangeTagData.Change(
                    transitionName = sharedElements.second,
                    id = item.id,
                    preview = ChangeTagData.Change.Preview(
                        name = item.name,
                        color = item.color,
                        icon = null,
                    ),
                ),
            ),
            sharedElements = mapOf(sharedElements),
        )
    }

    fun onAddCategoryClick() {
        val preselectedTypeId: Long? = recordTypeId.takeUnless { it == 0L }
        router.navigate(
            data = ChangeCategoryFromChangeActivityParams(
                ChangeTagData.New(preselectedTypeId),
            ),
        )
    }

    fun onArchiveClick() {
        archiveButtonEnabled.set(false)
        viewModelScope.launch {
            if (recordTypeId != 0L) {
                recordTypeInteractor.archive(recordTypeId)
                runningRecordInteractor.get(recordTypeId)?.let { runningRecord ->
                    removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
                }
                externalViewsInteractor.onTypeArchive()
                showArchivedMessage(R.string.change_record_type_archived)
                keyboardVisibility.set(false)
                router.back()
            }
        }
    }

    fun onDeleteClick() {
        router.navigate(
            StandardDialogParams(
                tag = DELETE_ALERT_DIALOG_TAG,
                title = resourceRepo.getString(R.string.change_record_type_delete_alert),
                message = resourceRepo.getString(R.string.archive_deletion_alert),
                btnPositive = resourceRepo.getString(R.string.ok),
                btnNegative = resourceRepo.getString(R.string.cancel),
            ),
        )
    }

    fun onStatisticsClick() = viewModelScope.launch {
        if (recordTypeId == 0L) return@launch
        val preview = recordType.value ?: return@launch

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.ACTIVITY,
            shift = 0,
            sharedElements = emptyMap(),
            itemId = recordTypeId,
            itemName = preview.name,
            itemIcon = preview.iconId,
            itemColor = preview.color,
        )
    }

    fun onSaveClick() {
        if (isNameEmpty()) return
        saveButtonEnabled.set(false)
        viewModelScope.launch {
            val addedId = saveRecordType()
            saveCategories(addedId)
            goalsViewModelDelegate.saveGoals(RecordTypeGoal.IdData.Type(addedId))
            externalViewsInteractor.onTypeAddOrChange(recordTypeId)
            keyboardVisibility.set(false)
            router.back()
        }
    }

    fun onBackPressed() {
        if (chooserState.value?.current !is ChangeRecordTypeChooserState.Closed) {
            onNewChooserState(ChangeRecordTypeChooserState.Closed)
        } else {
            router.back()
        }
    }

    fun onDuplicateClick() {
        if (isNameEmpty()) return
        duplicateButtonEnabled.set(false)
        viewModelScope.launch {
            val addedId = duplicateRecordType()
            recordTypeCategoryInteractor.addCategories(addedId, newCategories)
            goalsViewModelDelegate.saveGoals(RecordTypeGoal.IdData.Type(addedId))
            activityFilterInteractor.getByTypeId(recordTypeId).forEach { filter ->
                val newFilter = filter.copy(
                    selectedIds = (filter.selectedIds + addedId).toSet().toList(),
                )
                activityFilterInteractor.add(newFilter)
            }
            onSaveClick()
        }
    }

    fun onDefaultDurationClick() = viewModelScope.launch {
        DurationDialogParams(
            tag = DEFAULT_DURATION_DIALOG_TAG,
            value = DurationDialogParams.Value.DurationSeconds(newDefaultDuration),
        ).let(router::navigate)
    }

    fun onPositiveDialogClick(tag: String?) {
        when (tag) {
            DELETE_ALERT_DIALOG_TAG -> delete()
        }
    }

    fun onDurationSet(tag: String?, duration: Long, anchor: Any) {
        goalsViewModelDelegate.onGoalDurationSet(tag, duration, anchor)
        onDefaultDurationSet(tag, duration)
    }

    fun onDurationDisabled(tag: String?) {
        goalsViewModelDelegate.onGoalDurationDisabled(tag)
        onDefaultDurationSet(tag, 0)
    }

    private fun onDefaultDurationSet(tag: String?, duration: Long) {
        if (tag != DEFAULT_DURATION_DIALOG_TAG) return
        newDefaultDuration = duration
        updateAdditionalState()
    }

    private fun delete() {
        router.back() // Close dialog.
        deleteButtonEnabled.set(false)
        viewModelScope.launch {
            if (recordTypeId != 0L) {
                removeRunningRecordMediator.remove(recordTypeId)
                removeRecordTypeMediator.remove(recordTypeId)
                externalViewsInteractor.onTypeRemoveWithoutArchive()
                showMessage(R.string.archive_activity_deleted)
                keyboardVisibility.set(false)
                router.back()
            }
        }
    }

    private fun isNameEmpty(): Boolean {
        return if (newName.isEmpty()) {
            showMessage(coreR.string.change_record_message_choose_name)
            true
        } else {
            false
        }
    }

    private fun onNewChooserState(
        newState: ChangeRecordTypeChooserState,
    ) {
        val current = chooserState.value?.current
            ?: ChangeRecordTypeChooserState.Closed

        keyboardVisibility.set(false)
        if (current == newState) {
            chooserState.set(
                ViewChooserStateDelegate.States(
                    current = ChangeRecordTypeChooserState.Closed,
                    previous = current,
                ),
            )
        } else {
            chooserState.set(
                ViewChooserStateDelegate.States(
                    current = newState,
                    previous = current,
                ),
            )
        }
    }

    private suspend fun saveRecordType(): Long {
        val recordType = RecordType(
            id = recordTypeId,
            name = newName,
            icon = iconSelectionViewModelDelegateImpl.newIcon,
            color = colorSelectionViewModelDelegateImpl.newColor,
            defaultDuration = newDefaultDuration,
            note = newNote,
        )

        return recordTypeInteractor.add(recordType)
    }

    private suspend fun duplicateRecordType(): Long {
        // Copy will have a name like "type (2)",
        // if already exist - "type (3)" etc.
        val typeNames = recordTypeInteractor.getAll().map { it.name }
        var index = 2
        var name: String

        while (true) {
            name = "$newName ($index)"
            if (name in typeNames && index < 100) {
                index += 1
            } else {
                break
            }
        }

        val recordType = RecordType(
            name = name,
            icon = iconSelectionViewModelDelegateImpl.newIcon,
            color = colorSelectionViewModelDelegateImpl.newColor,
            defaultDuration = newDefaultDuration,
            note = newNote,
        )

        return recordTypeInteractor.add(recordType)
    }

    private suspend fun saveCategories(typeId: Long) {
        val addedCategories = newCategories.filterNot { it in initialCategories }
        val removedCategories = initialCategories.filterNot { it in newCategories }

        recordTypeCategoryInteractor.addCategories(typeId, addedCategories)
        recordTypeCategoryInteractor.removeCategories(typeId, removedCategories)
    }

    private suspend fun initializeRecordTypeData() {
        recordTypeInteractor.get(recordTypeId)?.let {
            newName = it.name
            newDefaultDuration = it.defaultDuration
            newNote = it.note
            iconSelectionViewModelDelegateImpl.newIcon = it.icon
            colorSelectionViewModelDelegateImpl.newColor = it.color
            goalsViewModelDelegate.initialize(RecordTypeGoal.IdData.Type(it.id))
            iconSelectionViewModelDelegateImpl.update()
            colorSelectionViewModelDelegateImpl.update()
            updateAdditionalState()
            updateNoteState()
        }
    }

    private suspend fun initializeSelectedCategories() {
        recordTypeCategoryInteractor.getCategories(recordTypeId)
            .let {
                newCategories = it.toMutableList()
                initialCategories = it
            }
    }

    private fun getColorSelectionDelegateParent(): ColorSelectionViewModelDelegate.Parent {
        return object : ColorSelectionViewModelDelegate.Parent {
            override suspend fun update() {
                updateRecordPreviewViewData()
                iconSelectionViewModelDelegateImpl.update()
            }
        }
    }

    private fun getIconSelectionDelegateParent(): IconSelectionViewModelDelegate.Parent {
        return object : IconSelectionViewModelDelegate.Parent {
            override fun keyboardVisibility(isVisible: Boolean) {
                keyboardVisibility.set(isVisible)
            }

            override suspend fun update() {
                updateRecordPreviewViewData()
            }

            override fun getColor(): AppColor {
                return colorSelectionViewModelDelegateImpl.newColor
            }
        }
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    private fun showArchivedMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showArchiveMessage(stringResId)
    }

    private suspend fun updateRecordPreviewViewData() {
        val data = loadRecordPreviewViewData()
        recordType.set(data)
    }

    private suspend fun loadRecordPreviewViewData(): RecordTypeViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return RecordType(
            name = newName,
            icon = iconSelectionViewModelDelegateImpl.newIcon,
            color = colorSelectionViewModelDelegateImpl.newColor,
            defaultDuration = newDefaultDuration,
            note = newNote,
        ).let { recordTypeViewDataMapper.map(it, isDarkTheme) }
    }

    private suspend fun updateCategoriesViewData() {
        val data = loadCategoriesViewData()
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(): ChangeRecordTypeCategoriesViewData {
        return viewDataInteractor.getCategoriesViewData(newCategories)
    }

    private fun updateAdditionalState() {
        val data = loadAdditionalState()
        additionalState.set(data)
    }

    private fun loadAdditionalState(): ChangeRecordTypeAdditionalState {
        return ChangeRecordTypeAdditionalState(
            isDuplicateVisible = extra is ChangeRecordTypeParams.Change,
            defaultDuration = if (newDefaultDuration > 0) {
                timeMapper.formatDuration(newDefaultDuration)
            } else {
                resourceRepo.getString(R.string.change_record_type_goal_time_disabled)
            },
        )
    }

    private fun updateNoteState() {
        val data = loadNoteState()
        noteState.set(data)
    }

    private fun loadNoteState(): String {
        return newNote
    }

    companion object {
        private const val DELETE_ALERT_DIALOG_TAG = "delete_alert_dialog_tag"
        private const val DEFAULT_DURATION_DIALOG_TAG = "default_duration_dialog_tag"
    }
}
