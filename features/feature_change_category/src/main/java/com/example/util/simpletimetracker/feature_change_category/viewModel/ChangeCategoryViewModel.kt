package com.example.util.simpletimetracker.feature_change_category.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_category.R
import com.example.util.simpletimetracker.feature_change_category.interactor.ChangeCategoryViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_type.goals.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_record_type.goals.GoalsViewModelDelegateImpl
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeCategoryViewModel @Inject constructor(
    private val router: Router,
    private val changeCategoryViewDataInteractor: ChangeCategoryViewDataInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val colorViewDataInteractor: ColorViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val goalsViewModelDelegate: GoalsViewModelDelegateImpl,
    private val widgetInteractor: WidgetInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
) : ViewModel(),
    GoalsViewModelDelegate by goalsViewModelDelegate {

    lateinit var extra: ChangeTagData

    val categoryPreview: LiveData<CategoryViewData> by lazy {
        return@lazy MutableLiveData<CategoryViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadCategoryViewData() }
            initial
        }
    }
    val colors: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadColorsViewData() }
            initial
        }
    }
    val types: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
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
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId == 0L) }

    private val categoryId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var initialTypes: Set<Long> = emptySet()
    private var newName: String = ""
    private var newColor: AppColor = AppColor(colorId = (0..ColorMapper.colorsNumber).random(), colorInt = "")
    private var newTypes: MutableList<Long> = mutableListOf()

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

    fun onColorClick(item: ColorViewData) {
        viewModelScope.launch {
            if (item.colorId != newColor.colorId || newColor.colorInt.isNotEmpty()) {
                newColor = AppColor(colorId = item.colorId, colorInt = "")
                updateCategoryPreview()
                updateColors()
            }
        }
    }

    fun onColorPaletteClick() {
        ColorSelectionDialogParams(
            preselectedColor = colorMapper.mapToColorInt(
                color = newColor,
                isDarkTheme = false // Pass original, not darkened color.
            )
        ).let(router::navigate)
    }

    fun onCustomColorSelected(colorInt: Int) {
        viewModelScope.launch {
            if (colorInt.toString() != newColor.colorInt) {
                newColor = AppColor(colorId = 0, colorInt = colorInt.toString())
                updateCategoryPreview()
                updateColors()
            }
        }
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

    fun onSaveClick() {
        if (newName.isEmpty()) {
            showMessage(R.string.change_category_message_choose_name)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            Category(
                id = categoryId,
                name = newName,
                color = newColor
            ).let {
                val addedId = saveCategory()
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

    private suspend fun saveCategory(): Long {
        val category = Category(
            id = categoryId,
            name = newName,
            color = newColor
        )

        return categoryInteractor.add(category)
    }

    private suspend fun saveTypes(categoryId: Long) {
        val addedTypes = newTypes.filterNot { it in initialTypes }
        val removedTypes = initialTypes.filterNot { it in newTypes }

        recordTypeCategoryInteractor.addTypes(categoryId, addedTypes)
        recordTypeCategoryInteractor.removeTypes(categoryId, removedTypes)
    }

    private fun onNewChooserState(
        newState: ChangeRecordTypeChooserState.State,
    ) {
        val current = chooserState.value?.current ?: ChangeRecordTypeChooserState.State.Closed
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

    private fun updateCategoryPreview() = viewModelScope.launch {
        (categoryPreview as MutableLiveData).value = loadCategoryPreviewViewData()
    }

    private suspend fun loadCategoryViewData(): CategoryViewData {
        categoryInteractor.get(categoryId)
            ?.let {
                newName = it.name
                newColor = it.color
                goalsViewModelDelegate.initialize(RecordTypeGoal.IdData.Category(it.id))
                updateColors()
            }
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = newColor
        ).let { categoryViewDataMapper.mapCategory(it, isDarkTheme) }
    }

    private suspend fun loadCategoryPreviewViewData(): CategoryViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = newColor
        ).let { categoryViewDataMapper.mapCategory(it, isDarkTheme) }
    }

    private fun updateColors() = viewModelScope.launch {
        val data = loadColorsViewData()
        colors.set(data)
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        return colorViewDataInteractor.getColorsViewData(newColor)
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        val data = loadTypesViewData()
        (types as MutableLiveData).value = data
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return changeCategoryViewDataInteractor.getTypesViewData(newTypes)
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            duration = SnackBarParams.Duration.Short,
            margins = SnackBarParams.Margins(
                bottom = resourceRepo.getDimenInDp(R.dimen.button_height),
            )
        )
        router.show(params)
    }
}
