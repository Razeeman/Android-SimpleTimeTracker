package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.core.viewData.CategoryViewData
import com.example.util.simpletimetracker.core.viewData.ColorViewData
import com.example.util.simpletimetracker.core.viewData.EmojiViewData
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.extension.orTrue
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.interactor.ChangeRecordTypeViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeMapper
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeScrollViewData
import com.example.util.simpletimetracker.navigation.Notification
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.ToastParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangeRecordTypeViewModel @Inject constructor(
    private val router: Router,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val viewDataInteractor: ChangeRecordTypeViewDataInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeRecordTypeMapper: ChangeRecordTypeMapper,
    private val resourceRepo: ResourceRepo,
    private val iconEmojiMapper: IconEmojiMapper
) : ViewModel() {

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
    val iconCategories: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData<List<ViewHolderType>>().let { initial ->
            viewModelScope.launch { initial.value = loadIconCategoriesViewData() }
            initial
        }
    }
    val iconsTypeViewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData(loadIconsTypeViewData())
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
    val goalTimeViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadGoalTimeViewData()
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
    val iconsScrollPosition: LiveData<ChangeRecordTypeScrollViewData> = MutableLiveData()

    private var iconType: IconType = IconType.IMAGE
    private var initialCategories: List<Long> = emptyList()
    private var newName: String = ""
    private var newIconName: String = ""
    private var newCategories: MutableList<Long> = mutableListOf()
    private var newColorId: Int = (0..ColorMapper.colorsNumber).random()
    private var newGoalTime: Long = 0L

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

    fun onIconTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChangeRecordTypeIconSwitchViewData) return
        viewModelScope.launch {
            iconType = viewData.iconType
            updateIconsTypeViewData()
            updateIconCategories()
            updateIcons()
        }
    }

    fun onIconCategoryClick(viewData: ChangeRecordTypeIconCategoryViewData) {
        icons.value
            ?.indexOfFirst { (it as? ChangeRecordTypeIconCategoryInfoViewData)?.type == viewData.type }
            ?.let { ChangeRecordTypeScrollViewData.ScrollTo(it) }
            ?.let { iconsScrollPosition.set(it) }
    }

    fun onIconClick(item: ChangeRecordTypeIconViewData) {
        viewModelScope.launch {
            if (item.iconName != newIconName) {
                newIconName = item.iconName
                updateRecordPreviewViewData()
            }
        }
    }

    fun onEmojiClick(item: EmojiViewData) {
        if (iconEmojiMapper.hasSkinToneVariations(item.emojiCodes)) {
            openEmojiSelectionDialog(item)
        } else {
            viewModelScope.launch {
                if (item.emojiText != newIconName) {
                    newIconName = item.emojiText
                    updateRecordPreviewViewData()
                }
            }
        }
    }

    fun onEmojiSelected(emojiText: String) {
        viewModelScope.launch {
            if (emojiText != newIconName) {
                newIconName = emojiText
                updateRecordPreviewViewData()
            }
        }
    }

    fun onGoalTimeClick() {
        router.navigate(
            Screen.DURATION_DIALOG,
            DurationDialogParams(
                tag = GOAL_TIME_DIALOG_TAG,
                duration = newGoalTime
            )
        )
    }

    fun onDurationSet(tag: String?, duration: Long) {
        when (tag) {
            GOAL_TIME_DIALOG_TAG -> viewModelScope.launch {
                newGoalTime = duration
                updateGoalTimeViewData()
            }
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            GOAL_TIME_DIALOG_TAG -> viewModelScope.launch {
                newGoalTime = 0
                updateGoalTimeViewData()
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
                        timeStarted = runningRecord.timeStarted,
                        comment = runningRecord.comment
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
            notificationGoalTimeInteractor.checkAndReschedule(extra.id)
            widgetInteractor.updateWidgets()
            (keyboardVisibility as MutableLiveData).value = false
            router.back()
        }
    }

    fun onScrolled() {
        iconsScrollPosition.set(ChangeRecordTypeScrollViewData.NoScroll)
    }

    private fun openEmojiSelectionDialog(item: EmojiViewData) {
        val params = changeRecordTypeMapper.mapEmojiSelectionParams(
            colorId = newColorId,
            emojiCodes = item.emojiCodes
        )

        router.navigate(
            Screen.EMOJI_SELECTION,
            params
        )
    }

    private suspend fun saveRecordType(): Long {
        val recordType = RecordType(
            id = extra.id,
            name = newName,
            icon = newIconName,
            color = newColorId,
            goalTime = newGoalTime
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
                newGoalTime = it.goalTime
                updateIcons()
                updateGoalTimeViewData()
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
        return viewDataInteractor.getColorsViewData()
    }

    private fun updateIcons() = viewModelScope.launch {
        val data = loadIconsViewData()
        (icons as MutableLiveData).value = data
    }

    private suspend fun loadIconsViewData(): List<ViewHolderType> {
        return viewDataInteractor.getIconsViewData(newColorId, iconType)
    }

    private fun updateIconCategories() = viewModelScope.launch {
        val data = loadIconCategoriesViewData()
        (iconCategories as MutableLiveData).value = data
    }

    private fun loadIconCategoriesViewData(): List<ViewHolderType> {
        return viewDataInteractor.getIconCategoriesViewData(iconType)
    }

    private fun updateIconsTypeViewData() {
        (iconsTypeViewData as MutableLiveData).value = loadIconsTypeViewData()
    }

    private fun loadIconsTypeViewData(): List<ViewHolderType> {
        return changeRecordTypeMapper.mapToIconSwitchViewData(iconType)
    }

    private fun updateCategoriesViewData() = viewModelScope.launch {
        val data = loadCategoriesViewData()
        (categories as MutableLiveData).value = data
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return viewDataInteractor.getCategoriesViewData(newCategories)
    }

    private fun updateGoalTimeViewData() {
        val data = loadGoalTimeViewData()
        (goalTimeViewData as MutableLiveData).value = data
    }

    private fun loadGoalTimeViewData(): String {
        return newGoalTime.let(changeRecordTypeMapper::toGoalTimeViewData)
    }

    private fun showMessage(stringResId: Int) {
        stringResId
            .let(resourceRepo::getString)
            .let { router.show(Notification.TOAST, ToastParams(it)) }
    }

    companion object {
        private const val GOAL_TIME_DIALOG_TAG = "goal_time_dialog_tag"
    }
}
