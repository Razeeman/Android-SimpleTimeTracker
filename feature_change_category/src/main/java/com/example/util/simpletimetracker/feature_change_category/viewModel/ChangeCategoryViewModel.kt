package com.example.util.simpletimetracker.feature_change_category.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.core.viewData.ColorViewData
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.feature_change_category.R
import com.example.util.simpletimetracker.feature_change_category.extra.ChangeCategoryExtra
import com.example.util.simpletimetracker.feature_change_category.interactor.ChangeCategoryViewDataInteractor
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeCategoryViewModel @Inject constructor(
    private val router: Router,
    private val changeCategoryViewDataInteractor: ChangeCategoryViewDataInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val resourceRepo: ResourceRepo
) : ViewModel() {

    lateinit var extra: ChangeCategoryExtra

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
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(extra.id != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(extra.id == 0L) }

    private var initialTypes: List<Long> = emptyList()
    private var newName: String = ""
    private var newColorId: Int = (0..ColorMapper.colorsNumber).random()
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
            if (item.colorId != newColorId) {
                newColorId = item.colorId
                updateCategoryPreview()
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
            if (extra.id != 0L) {
                categoryInteractor.remove(extra.id)
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
                id = extra.id,
                name = newName,
                color = newColorId
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
            id = extra.id,
            name = newName,
            color = newColorId
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
        recordTypeCategoryInteractor.getTypes(extra.id)
            .let {
                newTypes = it.toMutableList()
                initialTypes = it
            }
    }

    private fun updateCategoryPreview() = viewModelScope.launch {
        (categoryPreview as MutableLiveData).value = loadCategoryPreviewViewData()
    }

    private suspend fun loadCategoryViewData(): CategoryViewData {
        categoryInteractor.get(extra.id)
            ?.let {
                newName = it.name
                newColorId = it.color
            }
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = newColorId
        ).let { categoryViewDataMapper.map(it, isDarkTheme) }
    }

    private suspend fun loadCategoryPreviewViewData(): CategoryViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return Category(
            name = newName,
            color = newColorId
        ).let { categoryViewDataMapper.map(it, isDarkTheme) }
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return ColorMapper.getAvailableColors(isDarkTheme)
            .mapIndexed { colorId, colorResId ->
                colorId to resourceRepo.getColor(colorResId)
            }
            .map { (colorId, colorInt) ->
                ColorViewData(
                    colorId = colorId,
                    colorInt = colorInt
                )
            }
    }

    private fun updateTypesViewData() = viewModelScope.launch {
        val data = loadTypesViewData()
        (types as MutableLiveData).value = data
    }

    private suspend fun loadTypesViewData(): List<ViewHolderType> {
        return changeCategoryViewDataInteractor.getTypesViewData(newTypes)
    }

    private fun showMessage(stringResId: Int) {
        stringResId
            .let(resourceRepo::getString)
            .let { router.show(Notification.TOAST, ToastParams(it)) }
    }
}
