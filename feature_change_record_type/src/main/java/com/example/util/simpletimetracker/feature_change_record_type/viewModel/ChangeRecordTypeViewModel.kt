package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.core.viewData.ColorViewData
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.extra.ChangeRecordTypeExtra
import com.example.util.simpletimetracker.feature_change_record_type.interactor.ChangeRecordTypeViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordTypeViewModel @Inject constructor(
    private val router: Router,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val changeRecordTypeViewDataInteractor: ChangeRecordTypeViewDataInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper
) : ViewModel() {

    lateinit var extra: ChangeRecordTypeExtra

    val recordType: LiveData<RecordTypeViewData> by lazy {
        return@lazy MutableLiveData<RecordTypeViewData>().let { initial ->
            viewModelScope.launch {
                initializeRecordTypeData()
                initial.value = loadRecordPreviewViewData()
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
    val icons: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadIconsViewData() }
            initial
        }
    }
    val categories: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch {
                initializeSelectedCategories()
                initial.value = loadCategoriesViewData()
            }
            initial
        }
    }
    val flipColorChooser: LiveData<Boolean> = MutableLiveData()
    val flipIconChooser: LiveData<Boolean> = MutableLiveData()
    val flipCategoryChooser: LiveData<Boolean> = MutableLiveData()
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(extra.id != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(extra.id == 0L) }

    private var initialCategories: List<Long> = emptyList()
    private var newName: String = ""
    private var newIconName: String = ""
    private var newCategories: MutableList<Long> = mutableListOf()
    private var newColorId: Int = (0..ColorMapper.colorsNumber).random()

    fun onNameChange(name: String) {
        viewModelScope.launch {
            if (name != newName) {
                newName = name
                updateRecordPreviewViewData()
            }
        }
    }

    fun onColorChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipColorChooser as MutableLiveData).value = flipColorChooser.value
            ?.flip().orTrue()

        if (flipIconChooser.value == true) {
            (flipIconChooser as MutableLiveData).value = false
        }
        if (flipCategoryChooser.value == true) {
            (flipCategoryChooser as MutableLiveData).value = false
        }
    }

    fun onIconChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipIconChooser as MutableLiveData).value = flipIconChooser.value
            ?.flip().orTrue()

        if (flipColorChooser.value == true) {
            (flipColorChooser as MutableLiveData).value = false
        }
        if (flipCategoryChooser.value == true) {
            (flipCategoryChooser as MutableLiveData).value = false
        }
    }

    fun onCategoryChooserClick() {
        (keyboardVisibility as MutableLiveData).value = false
        (flipCategoryChooser as MutableLiveData).value = flipCategoryChooser.value
            ?.flip().orTrue()

        if (flipColorChooser.value == true) {
            (flipColorChooser as MutableLiveData).value = false
        }
        if (flipIconChooser.value == true) {
            (flipIconChooser as MutableLiveData).value = false
        }
    }

    fun onColorClick(item: ColorViewData) {
        viewModelScope.launch {
            if (item.colorId != newColorId) {
                newColorId = item.colorId
                updateRecordPreviewViewData()
                updateIcons()
            }
        }
    }

    fun onIconClick(item: ChangeRecordTypeIconViewData) {
        viewModelScope.launch {
            if (item.iconName != newIconName) {
                newIconName = item.iconName
                updateRecordPreviewViewData()
            }
        }
    }

    fun onCategoryClick(item: CategoryViewData) {
        viewModelScope.launch {
            if (item.id in newCategories) {
                newCategories.remove(item.id)
            } else {
                newCategories.add(item.id)
            }
            updateCategoriesViewData()
        }
    }

    fun onDeleteClick() {
        (deleteButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            if (extra.id != 0L) {
                recordTypeInteractor.remove(extra.id)
                runningRecordInteractor.get(extra.id)?.let { runningRecord ->
                    recordInteractor.add(
                        typeId = runningRecord.id,
                        timeStarted = runningRecord.timeStarted
                    )
                    removeRunningRecordMediator.remove(extra.id)
                }
                showMessage(R.string.change_record_type_removed)
                (keyboardVisibility as MutableLiveData).value = false
                router.back()
            }
        }
    }

    fun onSaveClick() {
        if (newName.isEmpty()) {
            showMessage(R.string.change_record_message_choose_name)
            return
        }
        (saveButtonEnabled as MutableLiveData).value = false
        viewModelScope.launch {
            val addedId = saveRecordType()
            saveCategories(addedId)
            notificationTypeInteractor.checkAndShow(extra.id)
            widgetInteractor.updateWidgets()
            (keyboardVisibility as MutableLiveData).value = false
            router.back()
        }
    }

    private suspend fun saveRecordType(): Long {
        val recordType = RecordType(
            id = extra.id,
            name = newName,
            icon = newIconName,
            color = newColorId,
            goalTime = 0
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
        recordTypeInteractor.get(extra.id)
            ?.let {
                newName = it.name
                newIconName = it.icon
                newColorId = it.color
                updateIcons()
            }
    }

    private suspend fun initializeSelectedCategories() {
        recordTypeCategoryInteractor.getCategories(extra.id)
            .let {
                newCategories = it.toMutableList()
                initialCategories = it
            }
    }

    private fun updateRecordPreviewViewData() = viewModelScope.launch {
        (recordType as MutableLiveData).value = loadRecordPreviewViewData()
    }

    private suspend fun loadRecordPreviewViewData(): RecordTypeViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return RecordType(
            name = newName,
            icon = newIconName,
            color = newColorId,
            goalTime = 0
        ).let { recordTypeViewDataMapper.map(it, isDarkTheme) }
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

    private fun updateIcons() = viewModelScope.launch {
        (icons as MutableLiveData).value = loadIconsViewData()
    }

    private suspend fun loadIconsViewData(): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return iconMapper.availableIconsNames
            .map { (iconName, iconResId) ->
                ChangeRecordTypeIconViewData(
                    iconName = iconName,
                    iconResId = iconResId,
                    colorInt = newColorId
                        .let { colorMapper.mapToColorResId(it, isDarkTheme) }
                        .let(resourceRepo::getColor)
                )
            }
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        (categories as MutableLiveData).value = data
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return changeRecordTypeViewDataInteractor.getCategoriesViewData(newCategories)
    }

    private fun showMessage(stringResId: Int) {
        stringResId
            .let(resourceRepo::getString)
            .let { router.show(Notification.TOAST, ToastParams(it)) }
    }
}
