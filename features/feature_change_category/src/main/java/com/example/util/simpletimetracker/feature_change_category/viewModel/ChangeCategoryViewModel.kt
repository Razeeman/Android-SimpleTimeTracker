package com.example.util.simpletimetracker.feature_change_category.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.trimIfNotBlank
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.ViewChooserStateDelegate
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.ChartFilterType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_category.R
import com.example.util.simpletimetracker.feature_change_category.interactor.ChangeCategoryViewDataInteractor
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryTypesViewData
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryChooserState
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryFieldsState
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeCategoryViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val changeCategoryViewDataInteractor: ChangeCategoryViewDataInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val goalsViewModelDelegate: GoalsViewModelDelegate,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
    private val colorSelectionViewModelDelegateImpl: ColorSelectionViewModelDelegateImpl,
) : BaseViewModel(),
    GoalsViewModelDelegate by goalsViewModelDelegate,
    ColorSelectionViewModelDelegate by colorSelectionViewModelDelegateImpl {

    lateinit var extra: ChangeTagData

    val categoryPreview: LiveData<CategoryViewData> by lazy {
        return@lazy MutableLiveData<CategoryViewData>().let { initial ->
            viewModelScope.launch {
                initializeData()
                initial.value = loadCategoryViewData()
            }
            initial
        }
    }
    val types: LiveData<ChangeCategoryTypesViewData> by lazy {
        return@lazy MutableLiveData<ChangeCategoryTypesViewData>().let { initial ->
            viewModelScope.launch {
                initializeSelectedTypes()
                initial.value = loadTypesViewData()
            }
            initial
        }
    }
    val chooserState: LiveData<ChangeCategoryFieldsState> by lazy {
        return@lazy MutableLiveData<ChangeCategoryFieldsState>(
            ChangeCategoryFieldsState(
                chooserState = ViewChooserStateDelegate.States(
                    current = ChangeCategoryChooserState.Closed,
                    previous = ChangeCategoryChooserState.Closed,
                ),
                additionalFieldsVisible = false,
            ),
        ).also { viewModelScope.launch { initializeChooserState() } }
    }
    val noteState: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadNoteState()
            }
            initial
        }
    }
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val nameErrorMessage: LiveData<String> = MutableLiveData("")
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId != 0L) }
    val statsIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId == 0L) }

    private val categoryId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var initialTypes: Set<Long> = emptySet()
    private var newName: String = ""
    private var newTypes: MutableList<Long> = mutableListOf()
    private var newNote: String = ""

    init {
        colorSelectionViewModelDelegateImpl.attach(getColorSelectionDelegateParent())
    }

    override fun onCleared() {
        (goalsViewModelDelegate as? ViewModelDelegate)?.clear()
        colorSelectionViewModelDelegateImpl.clear()
        super.onCleared()
    }

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updateCategoryPreview()
            }
        }
        viewModelScope.launch {
            val items = categoryInteractor.get(name).filter { it.id != categoryId }
            val error = if (items.isNotEmpty()) {
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
        onNewChooserState(ChangeCategoryChooserState.Color)
    }

    fun onTypeChooserClick() {
        onNewChooserState(ChangeCategoryChooserState.Type)
    }

    fun onGoalTimeChooserClick() {
        onNewChooserState(ChangeCategoryChooserState.GoalTime)
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            if (item.id in newTypes) {
                newTypes.remove(item.id)
            } else {
                newTypes.add(item.id)
            }
            updateTypesViewData()
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (categoryId != 0L) {
                categoryInteractor.remove(categoryId)
                externalViewsInteractor.onCategoryRemove(categoryId)
                showMessage(R.string.change_category_removed)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onStatisticsClick() = viewModelScope.launch {
        if (categoryId == 0L) return@launch
        val preview = categoryPreview.value ?: return@launch

        statisticsDetailNavigationInteractor.navigate(
            transitionName = "",
            filterType = ChartFilterType.CATEGORY,
            shift = 0,
            sharedElements = emptyMap(),
            itemId = categoryId,
            itemName = preview.name,
            itemIcon = null,
            itemColor = preview.color,
        )
    }

    fun onMoreFieldsClick() = viewModelScope.launch {
        val newValue = !prefsInteractor.getCategoryAdditionalFieldsShown()
        prefsInteractor.setCategoryAdditionalFieldsShown(newValue)

        val currentState = chooserState.value ?: return@launch
        val newState = ChangeCategoryFieldsState(
            chooserState = ViewChooserStateDelegate.States(
                current = currentState.chooserState.current,
                previous = currentState.chooserState.current,
            ),
            additionalFieldsVisible = newValue,
        )
        chooserState.set(newState)
    }

    fun onSaveClick() {
        if (newName.isEmpty()) {
            showMessage(R.string.change_category_message_choose_name)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            // Zero id creates new record
            Category(
                id = categoryId,
                name = newName.trimIfNotBlank(),
                color = colorSelectionViewModelDelegateImpl.newColor,
                note = newNote,
            ).let {
                val addedId = categoryInteractor.add(it)
                saveTypes(addedId)
                goalsViewModelDelegate.saveGoals(RecordTypeGoal.IdData.Category(addedId))
                val typeIds = (initialTypes + newTypes).toSet().toList()
                externalViewsInteractor.onCategoryAddOrChange(typeIds)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onBackPressed() {
        if (chooserState.value?.chooserState?.current !is ChangeCategoryChooserState.Closed) {
            onNewChooserState(ChangeCategoryChooserState.Closed)
        } else {
            router.back()
        }
    }

    fun onDurationSet(tag: String?, duration: Long, anchor: Any) {
        goalsViewModelDelegate.onGoalDurationSet(tag, duration, anchor)
    }

    fun onDurationDisabled(tag: String?) {
        goalsViewModelDelegate.onGoalDurationDisabled(tag)
    }

    private suspend fun saveTypes(categoryId: Long) {
        val addedTypes = newTypes.filterNot { it in initialTypes }
        val removedTypes = initialTypes.filterNot { it in newTypes }

        recordTypeCategoryInteractor.addTypes(categoryId, addedTypes)
        recordTypeCategoryInteractor.removeTypes(categoryId, removedTypes)
    }

    private fun onNewChooserState(
        newState: ChangeCategoryChooserState,
    ) {
        val currentState = chooserState.value ?: return
        val current = currentState.chooserState.current

        val newChooserState = if (current == newState) {
            ViewChooserStateDelegate.States(
                current = ChangeCategoryChooserState.Closed,
                previous = current,
            )
        } else {
            ViewChooserStateDelegate.States(
                current = newState,
                previous = current,
            )
        }

        keyboardVisibility.set(false)
        chooserState.set(currentState.copy(chooserState = newChooserState))
    }

    private suspend fun initializeChooserState() {
        val current = chooserState.value ?: return
        val newState = current.copy(
            additionalFieldsVisible = prefsInteractor.getCategoryAdditionalFieldsShown(),
        )
        chooserState.set(newState)
    }

    private suspend fun initializeSelectedTypes() {
        when (extra) {
            is ChangeTagData.Change -> {
                recordTypeCategoryInteractor.getTypes(categoryId).let {
                    newTypes = it.toMutableList()
                    initialTypes = it
                }
            }
            is ChangeTagData.New -> {
                val preselectedTypeId: Long? = (extra as? ChangeTagData.New)?.preselectedTypeId
                newTypes = listOfNotNull(preselectedTypeId).toMutableList()
            }
        }
    }

    private fun getColorSelectionDelegateParent(): ColorSelectionViewModelDelegate.Parent {
        return object : ColorSelectionViewModelDelegate.Parent {
            override suspend fun update() {
                updateCategoryPreview()
            }
        }
    }

    private fun updateCategoryPreview() = viewModelScope.launch {
        (categoryPreview as MutableLiveData).value = loadCategoryPreviewViewData()
    }

    private suspend fun initializeData() {
        categoryInteractor.get(categoryId)
            ?.let {
                newName = it.name
                newNote = it.note
                colorSelectionViewModelDelegateImpl.newColor = it.color
                goalsViewModelDelegate.initialize(RecordTypeGoal.IdData.Category(it.id))
                colorSelectionViewModelDelegateImpl.update()
                updateNoteState()
            }
    }

    private suspend fun loadCategoryViewData(): CategoryViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = colorSelectionViewModelDelegateImpl.newColor,
            note = newNote,
        ).let { categoryViewDataMapper.mapCategory(it, isDarkTheme) }
    }

    private suspend fun loadCategoryPreviewViewData(): CategoryViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = colorSelectionViewModelDelegateImpl.newColor,
            note = newNote,
        ).let { categoryViewDataMapper.mapCategory(it, isDarkTheme) }
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        val data = loadTypesViewData()
        (types as MutableLiveData).value = data
    }

    private suspend fun loadTypesViewData(): ChangeCategoryTypesViewData {
        return changeCategoryViewDataInteractor.getTypesViewData(newTypes)
    }

    private fun updateNoteState() {
        val data = loadNoteState()
        noteState.set(data)
    }

    private fun loadNoteState(): String {
        return newNote
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }
}
