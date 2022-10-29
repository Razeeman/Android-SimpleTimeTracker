package com.example.util.simpletimetracker.feature_change_category.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.ColorViewDataInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.feature_change_category.R
import com.example.util.simpletimetracker.feature_change_category.interactor.ChangeCategoryViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

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
) : ViewModel() {

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
    val flipColorChooser: LiveData<Boolean> = MutableLiveData()
    val flipTypesChooser: LiveData<Boolean> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(categoryId == 0L) }

    private val categoryId: Long get() = (extra as? ChangeTagData.Change)?.id.orZero()
    private var initialTypes: List<Long> = emptyList()
    private var newName: String = ""
    private var newColor: AppColor = AppColor(colorId = (0..ColorMapper.colorsNumber).random(), colorInt = "")
    private var newTypes: MutableList<Long> = mutableListOf()

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updateCategoryPreview()
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
                updateColors()
            }
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = newColor
        ).let { categoryViewDataMapper.mapActivityTag(it, isDarkTheme) }
    }

    private suspend fun loadCategoryPreviewViewData(): CategoryViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = newColor
        ).let { categoryViewDataMapper.mapActivityTag(it, isDarkTheme) }
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
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }
}
