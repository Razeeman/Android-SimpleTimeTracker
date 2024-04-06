package com.example.util.simpletimetracker.feature_change_activity_filter.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.delegates.ColorSelectionViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.ColorSelectionViewModelDelegateImpl
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.ActivityFilterViewDataMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_activity_filter.R
import com.example.util.simpletimetracker.feature_change_activity_filter.interactor.ChangeActivityFilterViewDataInteractor
import com.example.util.simpletimetracker.feature_change_activity_filter.mapper.ChangeActivityFilterMapper
import com.example.util.simpletimetracker.feature_change_activity_filter.viewData.ChangeActivityFilterTypeSwitchViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeActivityFilterViewModel @Inject constructor(
    private val router: Router,
    private val changeActivityFilterViewDataInteractor: ChangeActivityFilterViewDataInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val activityFilterViewDataMapper: ActivityFilterViewDataMapper,
    private val changeActivityFilterMapper: ChangeActivityFilterMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val colorSelectionViewModelDelegateImpl: ColorSelectionViewModelDelegateImpl,
) : ViewModel(),
    ColorSelectionViewModelDelegate by colorSelectionViewModelDelegateImpl {

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
    private var wasSelected: Boolean = true

    init {
        colorSelectionViewModelDelegateImpl.attach(getColorSelectionDelegateParent())
    }

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
                color = colorSelectionViewModelDelegateImpl.newColor,
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
                colorSelectionViewModelDelegateImpl.newColor = it.color
                wasSelected = it.selected
                colorSelectionViewModelDelegateImpl.update()
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

    private fun getColorSelectionDelegateParent(): ColorSelectionViewModelDelegate.Parent {
        return object : ColorSelectionViewModelDelegate.Parent {
            override suspend fun update() {
                updateActivityFilterPreview()
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
            color = colorSelectionViewModelDelegateImpl.newColor,
            selected = true,
        ).let { activityFilterViewDataMapper.map(it, isDarkTheme) }
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
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }
}
