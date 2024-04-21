package com.example.util.simpletimetracker.feature_change_category.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.colorSelection.ColorSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_category.R
import com.example.util.simpletimetracker.feature_change_category.interactor.ChangeCategoryViewDataInteractor
import com.example.util.simpletimetracker.feature_change_category.viewData.ChangeCategoryTypesViewData
import com.example.util.simpletimetracker.feature_change_record_type.goals.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_record_type.goals.GoalsViewModelDelegateImpl
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeCategoryViewModel @Inject constructor(
    private val router: Router,
    private val changeCategoryViewDataInteractor: ChangeCategoryViewDataInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val goalsViewModelDelegate: GoalsViewModelDelegateImpl,
    private val widgetInteractor: WidgetInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val colorSelectionViewModelDelegateImpl: ColorSelectionViewModelDelegateImpl,
) : ViewModel(),
    GoalsViewModelDelegate by goalsViewModelDelegate,
    ColorSelectionViewModelDelegate by colorSelectionViewModelDelegateImpl {

    lateinit var extra: ChangeTagData

    val categoryPreview: LiveData<CategoryViewData> by lazy {
        return@lazy MutableLiveData<CategoryViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadCategoryViewData() }
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
    val chooserState: LiveData<ChangeRecordTypeChooserState> = MutableLiveData(
        ChangeRecordTypeChooserState(
            current = ChangeRecordTypeChooserState.State.Closed,
            previous = ChangeRecordTypeChooserState.State.Closed,
        ),
    )
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId != 0L) }
    val statsIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId == 0L) }

    private val categoryId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var initialTypes: Set<Long> = emptySet()
    private var newName: String = ""
    private var newTypes: MutableList<Long> = mutableListOf()

    init {
        colorSelectionViewModelDelegateImpl.attach(getColorSelectionDelegateParent())
    }

    override fun onCleared() {
        (goalsViewModelDelegate as? ViewModelDelegate)?.clear()
        super.onCleared()
    }

    fun onVisible() = viewModelScope.launch {
        goalsViewModelDelegate.onVisible()
    }

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updateCategoryPreview()
            }
        }
    }

    fun onColorChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.State.Color)
    }

    fun onTypeChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.State.Type)
    }

    fun onGoalTimeChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.State.GoalTime)
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
                notificationGoalTimeInteractor.cancel(RecordTypeGoal.IdData.Category(categoryId))
                widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
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
                name = newName,
                color = colorSelectionViewModelDelegateImpl.newColor,
            ).let {
                val addedId = categoryInteractor.add(it)
                saveTypes(addedId)
                goalsViewModelDelegate.saveGoals(RecordTypeGoal.IdData.Category(addedId))
                val typeIds = (initialTypes + newTypes).toSet().toList()
                notificationGoalTimeInteractor.checkAndReschedule(typeIds)
                widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onBackPressed() {
        if (chooserState.value?.current !is ChangeRecordTypeChooserState.State.Closed) {
            onNewChooserState(ChangeRecordTypeChooserState.State.Closed)
        } else {
            router.back()
        }
    }

    private suspend fun saveTypes(categoryId: Long) {
        val addedTypes = newTypes.filterNot { it in initialTypes }
        val removedTypes = initialTypes.filterNot { it in newTypes }

        recordTypeCategoryInteractor.addTypes(categoryId, addedTypes)
        recordTypeCategoryInteractor.removeTypes(categoryId, removedTypes)
    }

    // TODO refactor choosers, same logic everywhere
    private fun onNewChooserState(
        newState: ChangeRecordTypeChooserState.State,
    ) {
        val current = chooserState.value?.current
            ?: ChangeRecordTypeChooserState.State.Closed
        keyboardVisibility.set(false)
        if (current == newState) {
            chooserState.set(
                ChangeRecordTypeChooserState(
                    current = ChangeRecordTypeChooserState.State.Closed,
                    previous = current,
                ),
            )
        } else {
            chooserState.set(
                ChangeRecordTypeChooserState(
                    current = newState,
                    previous = current,
                ),
            )
        }
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

    private suspend fun loadCategoryViewData(): CategoryViewData {
        categoryInteractor.get(categoryId)
            ?.let {
                newName = it.name
                colorSelectionViewModelDelegateImpl.newColor = it.color
                goalsViewModelDelegate.initialize(RecordTypeGoal.IdData.Category(it.id))
                colorSelectionViewModelDelegateImpl.update()
            }
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = colorSelectionViewModelDelegateImpl.newColor,
        ).let { categoryViewDataMapper.mapCategory(it, isDarkTheme) }
    }

    private suspend fun loadCategoryPreviewViewData(): CategoryViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = colorSelectionViewModelDelegateImpl.newColor,
        ).let { categoryViewDataMapper.mapCategory(it, isDarkTheme) }
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        val data = loadTypesViewData()
        (types as MutableLiveData).value = data
    }

    private suspend fun loadTypesViewData(): ChangeCategoryTypesViewData {
        return changeCategoryViewDataInteractor.getTypesViewData(newTypes)
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }
}
