package com.example.util.simpletimetracker.feature_change_activity_filter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.core.mapper.ActivityFilterViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_activity_filter.R
import com.example.util.simpletimetracker.feature_change_activity_filter.interactor.ChangeActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.feature_change_activity_filter.mapper.ChangeActivityFilterMapper
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterTypeSwitchViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeActivityFilterViewModel @Inject constructor(
    private val router: Router,
    private val changeActivityFilterViewDataInteractor: ChangeActivityFilterViewDataInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val colorViewDataInteractor: ColorViewDataInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val activityFilterViewDataMapper: ActivityFilterViewDataMapper,
    private val changeActivityFilterMapper: ChangeActivityFilterMapper,
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
) : ViewModel() {

    lateinit var extra: ChangeActivityFilterParams

    val filterPreview: LiveData<ActivityFilterViewData> by lazy {
        return@lazy MutableLiveData<ActivityFilterViewData>().let { initial ->
            viewModelScope.launch {
                initializePreview()
                initial.value = loadActivityFilterPreviewViewData()
            }
            initial
        }
    }
    val colors: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadColorsViewData() }
            initial
        }
    }
    val filterTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeSelectedTypes()
                initial.value = loadTagTypeViewData()
            }
            initial
        }
    }
    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeSelectedTypes()
                initial.value = loadViewData()
            }
            initial
        }
    }
    val flipColorChooser: LiveData<Boolean> = MutableLiveData()
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(filterId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(filterId == 0L) }

    private val filterId: Long get() = (extra as? ChangeActivityFilterParams.Change)?.id.orZero()
    private val newSelectedIds: List<Long>
        get() = when (newType) {
            is ActivityFilter.Type.Activity -> newTypeIds
            is ActivityFilter.Type.Category -> newCategoryIds
        }
    private var newTypeIds: MutableList<Long> = mutableListOf()
    private var newCategoryIds: MutableList<Long> = mutableListOf()
    private var newType: ActivityFilter.Type = ActivityFilter.Type.Activity
    private var newName: String = ""
    private var newColor: AppColor = AppColor(colorId = (0..ColorMapper.colorsNumber).random(), colorInt = "")
    private var wasSelected: Boolean = true

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updateActivityFilterPreview()
            }
        }
    }

    fun onColorChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipColorChooser as MutableLiveData).value = flipColorChooser.value
            ?.flip().orTrue()

        if (flipTypesChooser.value == true) {
            (flipTypesChooser as MutableLiveData).value = false
        }
    }

    fun onTypeChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipTypesChooser as MutableLiveData).value = flipTypesChooser.value
            ?.flip().orTrue()

        if (flipColorChooser.value == true) {
            (flipColorChooser as MutableLiveData).value = false
        }
    }

    fun onColorClick(item: ColorViewData) {
        viewModelScope.launch {
            if (item.colorId != newColor.colorId || newColor.colorInt.isNotEmpty()) {
                newColor = AppColor(colorId = item.colorId, colorInt = "")
                updateActivityFilterPreview()
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
                updateActivityFilterPreview()
                updateColors()
            }
        }
    }

    fun onFilterTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChangeActivityFilterTypeSwitchViewData) return
        viewModelScope.launch {
            newType = viewData.type
            updateTagTypeViewData()
            updateViewData()
        }
    }

    fun onTypeClick(item: RecordTypeViewData) {
        viewModelScope.launch {
            newType = ActivityFilter.Type.Activity
            newTypeIds.addOrRemove(item.id)
            updateViewData()
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            newType = ActivityFilter.Type.Category
            newCategoryIds.addOrRemove(item.id)
            updateViewData()
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (filterId != 0L) {
                activityFilterInteractor.remove(filterId)
                showMessage(R.string.change_activity_filter_removed)
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
            ActivityFilter(
                id = filterId,
                selectedIds = newSelectedIds,
                type = newType,
                name = newName,
                color = newColor,
                selected = wasSelected,
            ).let {
                activityFilterInteractor.add(it)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    private suspend fun initializePreview() {
        if (extra is ChangeActivityFilterParams.Change) {
            activityFilterInteractor.get(filterId)?.let {
                newName = it.name
                newColor = it.color
                wasSelected = it.selected
                updateColors()
            }
        }
    }

    private suspend fun initializeSelectedTypes() {
        if (extra is ChangeActivityFilterParams.Change) {
            activityFilterInteractor.get(filterId)?.let {
                when (it.type) {
                    is ActivityFilter.Type.Activity -> {
                        newTypeIds = it.selectedIds.toMutableList()
                    }
                    is ActivityFilter.Type.Category -> {
                        newCategoryIds = it.selectedIds.toMutableList()
                    }
                }
                newType = it.type
            }
        }
    }

    private fun updateActivityFilterPreview() = viewModelScope.launch {
        (filterPreview as MutableLiveData).value = loadActivityFilterPreviewViewData()
    }

    private suspend fun loadActivityFilterPreviewViewData(): ActivityFilterViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return ActivityFilter(
            selectedIds = newSelectedIds,
            type = newType,
            name = newName,
            color = newColor,
            selected = true,
        ).let { activityFilterViewDataMapper.map(it, isDarkTheme) }
    }

    private fun updateColors() = viewModelScope.launch {
        val data = loadColorsViewData()
        colors.set(data)
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        return colorViewDataInteractor.getColorsViewData(newColor)
    }

    private fun updateTagTypeViewData() {
        filterTypeViewData.set(loadTagTypeViewData())
    }

    private fun loadTagTypeViewData(): List<ViewHolderType> {
        return changeActivityFilterMapper.mapToTypeSwitchViewData(newType)
    }

    private fun updateViewData() = viewModelScope.launch {
        val data = loadViewData()
        (viewData as MutableLiveData).value = data
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        return changeActivityFilterViewDataInteractor.getTypesViewData(newType, newSelectedIds)
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
