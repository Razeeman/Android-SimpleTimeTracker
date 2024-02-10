package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SnackBarMessageNavigationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.IconImageState
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.goals.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_record_type.goals.GoalsViewModelDelegateImpl
import com.example.util.simpletimetracker.feature_change_record_type.interactor.ChangeRecordTypeViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeMapper
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSelectorStateViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconStateViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeScrollViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromChangeActivityParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.util.simpletimetracker.core.R as coreR

@HiltViewModel
class ChangeRecordTypeViewModel @Inject constructor(
    private val router: Router,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val viewDataInteractor: ChangeRecordTypeViewDataInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val notificationGoalTimeInteractor: NotificationGoalTimeInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val changeRecordTypeMapper: ChangeRecordTypeMapper,
    private val snackBarMessageNavigationInteractor: SnackBarMessageNavigationInteractor,
    private val iconEmojiMapper: IconEmojiMapper,
    private val colorMapper: ColorMapper,
    private val goalsViewModelDelegate: GoalsViewModelDelegateImpl,
) : ViewModel(),
    GoalsViewModelDelegate by goalsViewModelDelegate {

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
    val icons: LiveData<ChangeRecordTypeIconStateViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeIconStateViewData>().let { initial ->
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
    val iconSelectorViewData: LiveData<ChangeRecordTypeIconSelectorStateViewData> by lazy {
        return@lazy MutableLiveData<ChangeRecordTypeIconSelectorStateViewData>().let { initial ->
            viewModelScope.launch { initial.value = loadIconSelectorViewData() }
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
    val chooserState: LiveData<ChangeRecordTypeChooserState> = MutableLiveData(
        ChangeRecordTypeChooserState(
            current = ChangeRecordTypeChooserState.State.Closed,
            previous = ChangeRecordTypeChooserState.State.Closed,
        ),
    )
    val deleteButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val saveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)
    val deleteIconVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTypeId != 0L) }
    val keyboardVisibility: LiveData<Boolean> by lazy { MutableLiveData(recordTypeId == 0L) }
    val iconsScrollPosition: LiveData<ChangeRecordTypeScrollViewData> = MutableLiveData()
    val expandIconTypeSwitch: LiveData<Unit> = MutableLiveData()

    private val recordTypeId: Long get() = (extra as? ChangeRecordTypeParams.Change)?.id.orZero()
    private var iconType: IconType = IconType.IMAGE
    private var iconImageState: IconImageState = IconImageState.Chooser
    private var iconSearch: String = ""
    private var iconSearchJob: Job? = null
    private var initialCategories: Set<Long> = emptySet()
    private var newName: String = ""
    private var newIconName: String = ""
    private var newCategories: MutableList<Long> = mutableListOf()
    private var newColor: AppColor = AppColor(colorId = (0..ColorMapper.colorsNumber).random(), colorInt = "")

    override fun onCleared() {
        (goalsViewModelDelegate as? ViewModelDelegate)?.clear()
        super.onCleared()
    }

    fun onVisible() = viewModelScope.launch {
        initializeSelectedCategories()
        updateCategoriesViewData()
        goalsViewModelDelegate.onVisible()
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
    }

    fun onColorChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.State.Color)
    }

    fun onIconChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.State.Icon)
    }

    fun onCategoryChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.State.Category)
    }

    fun onGoalTimeChooserClick() {
        onNewChooserState(ChangeRecordTypeChooserState.State.GoalTime)
    }

    fun onColorClick(item: ColorViewData) {
        viewModelScope.launch {
            if (item.colorId != newColor.colorId || newColor.colorInt.isNotEmpty()) {
                newColor = AppColor(colorId = item.colorId, colorInt = "")
                updateRecordPreviewViewData()
                updateIcons()
                updateColors()
            }
        }
    }

    fun onColorPaletteClick() {
        ColorSelectionDialogParams(
            preselectedColor = colorMapper.mapToColorInt(
                color = newColor,
                isDarkTheme = false, // Pass original, not darkened color.
            ),
        ).let(router::navigate)
    }

    fun onCustomColorSelected(colorInt: Int) {
        viewModelScope.launch {
            if (colorInt.toString() != newColor.colorInt) {
                newColor = AppColor(colorId = 0, colorInt = colorInt.toString())
                updateRecordPreviewViewData()
                updateIcons()
                updateColors()
            }
        }
    }

    fun onIconTypeClick(viewData: ButtonsRowViewData) {
        if (viewData !is ChangeRecordTypeIconSwitchViewData) return
        if (viewData.iconType == iconType) return
        viewModelScope.launch {
            keyboardVisibility.set(false)
            iconType = viewData.iconType
            updateIconsTypeViewData()
            updateIconSelectorViewData()
            updateIconCategories(selectedIndex = 0)
            updateIconsLoad()
            updateIcons()
        }
    }

    fun onIconCategoryClick(viewData: ChangeRecordTypeIconCategoryViewData) {
        if (viewData.getUniqueId() == 0L) {
            expandIconTypeSwitch.set(Unit)
        }
        (icons.value as? ChangeRecordTypeIconStateViewData.Icons)
            ?.items
            ?.indexOfFirst { (it as? ChangeRecordTypeIconCategoryInfoViewData)?.type == viewData.type }
            ?.let(::updateIconScrollPosition)
    }

    fun onIconClick(item: ChangeRecordTypeIconViewData) {
        viewModelScope.launch {
            if (item.iconName != newIconName) {
                newIconName = item.iconName
                updateRecordPreviewViewData()
            }
        }
    }

    fun onIconsScrolled(
        firstVisiblePosition: Int,
        lastVisiblePosition: Int,
    ) {
        val items = (icons.value as? ChangeRecordTypeIconStateViewData.Icons?)
            ?.items ?: return
        val infoItems = items.filterIsInstance<ChangeRecordTypeIconCategoryInfoViewData>()

        // Last image category has small number of icons, need to check if it is visible,
        // otherwise it would never be selected by the second check.
        infoItems
            .firstOrNull { it.isLast }
            ?.takeIf { items.indexOf(it) <= lastVisiblePosition }
            ?.let {
                updateIconCategories(it.getUniqueId())
                return
            }

        infoItems
            .lastOrNull { items.indexOf(it) <= firstVisiblePosition }
            ?.let { updateIconCategories(it.getUniqueId()) }
    }

    fun onIconImageSearchClicked() {
        val newState = when (iconImageState) {
            is IconImageState.Chooser -> IconImageState.Search
            is IconImageState.Search -> IconImageState.Chooser
        }
        iconImageState = newState

        if (iconImageState is IconImageState.Chooser) {
            keyboardVisibility.set(false)
            expandIconTypeSwitch.set(Unit)
        }
        viewModelScope.launch {
            updateIconSelectorViewData()
            updateIconCategories(selectedIndex = 0)
            updateIconsLoad()
            updateIcons()
        }
    }

    fun onIconImageSearch(search: String) {
        if (iconType != IconType.IMAGE) return

        if (search != iconSearch) {
            iconSearchJob?.cancel()
            iconSearchJob = viewModelScope.launch {
                iconSearch = search
                delay(500)
                updateIcons()
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

    fun onIconTextChange(text: String) {
        viewModelScope.launch {
            if (text != newIconName) {
                newIconName = text
                updateRecordPreviewViewData()
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

    fun onDeleteClick() {
        deleteButtonEnabled.set(false)
        viewModelScope.launch {
            if (recordTypeId != 0L) {
                recordTypeInteractor.archive(recordTypeId)
                notificationTypeInteractor.updateNotifications()
                runningRecordInteractor.get(recordTypeId)?.let { runningRecord ->
                    removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
                }
                showArchivedMessage(R.string.change_record_type_archived)
                keyboardVisibility.set(false)
                router.back()
            }
        }
    }

    fun onSaveClick() {
        if (newName.isEmpty()) {
            showMessage(coreR.string.change_record_message_choose_name)
            return
        }
        saveButtonEnabled.set(false)
        viewModelScope.launch {
            val addedId = saveRecordType()
            saveCategories(addedId)
            goalsViewModelDelegate.saveGoals(RecordTypeGoal.IdData.Type(addedId))
            notificationTypeInteractor.updateNotifications()
            notificationGoalTimeInteractor.checkAndReschedule(listOf(recordTypeId))
            widgetInteractor.updateWidgets()
            keyboardVisibility.set(false)
            router.back()
        }
    }

    fun onScrolled() {
        iconsScrollPosition.set(ChangeRecordTypeScrollViewData.NoScroll)
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

    private fun openEmojiSelectionDialog(item: EmojiViewData) {
        val params = changeRecordTypeMapper.mapEmojiSelectionParams(
            color = newColor,
            emojiCodes = item.emojiCodes,
        )

        router.navigate(params)
    }

    private suspend fun saveRecordType(): Long {
        val recordType = RecordType(
            id = recordTypeId,
            name = newName,
            icon = newIconName,
            color = newColor,
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
            newIconName = it.icon
            newColor = it.color
            goalsViewModelDelegate.initialize(RecordTypeGoal.IdData.Type(it.id))
            updateIcons()
            updateColors()
        }
    }

    private suspend fun initializeSelectedCategories() {
        recordTypeCategoryInteractor.getCategories(recordTypeId)
            .let {
                newCategories = it.toMutableList()
                initialCategories = it
            }
    }

    private suspend fun updateRecordPreviewViewData() {
        val data = loadRecordPreviewViewData()
        recordType.set(data)
    }

    private suspend fun loadRecordPreviewViewData(): RecordTypeViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()

        return RecordType(
            name = newName,
            icon = newIconName,
            color = newColor,
        ).let { recordTypeViewDataMapper.map(it, isDarkTheme) }
    }

    private suspend fun updateColors() {
        val data = loadColorsViewData()
        colors.set(data)
    }

    private suspend fun loadColorsViewData(): List<ViewHolderType> {
        return viewDataInteractor.getColorsViewData(newColor)
    }

    private suspend fun updateIcons() {
        val data = loadIconsViewData()
        icons.set(data)
    }

    private fun updateIconsLoad() {
        val items = listOf(LoaderViewData())
        val data = ChangeRecordTypeIconStateViewData.Icons(items)
        icons.set(data)
    }

    private suspend fun loadIconsViewData(): ChangeRecordTypeIconStateViewData {
        return viewDataInteractor.getIconsViewData(
            newColor = newColor,
            iconType = iconType,
            iconImageState = iconImageState,
            iconSearch = iconSearch,
        )
    }

    private fun updateIconCategories(selectedIndex: Long) {
        val data = loadIconCategoriesViewData(selectedIndex)
        iconCategories.set(data)
    }

    private fun loadIconCategoriesViewData(selectedIndex: Long = 0): List<ViewHolderType> {
        return viewDataInteractor.getIconCategoriesViewData(
            iconType = iconType,
            selectedIndex = selectedIndex,
        )
    }

    private fun updateIconsTypeViewData() {
        val data = loadIconsTypeViewData()
        iconsTypeViewData.set(data)
    }

    private fun loadIconsTypeViewData(): List<ViewHolderType> {
        return changeRecordTypeMapper.mapToIconSwitchViewData(iconType)
    }

    private suspend fun updateIconSelectorViewData() {
        val data = loadIconSelectorViewData()
        iconSelectorViewData.set(data)
    }

    private suspend fun loadIconSelectorViewData(): ChangeRecordTypeIconSelectorStateViewData {
        return changeRecordTypeMapper.mapToIconSelectorViewData(
            iconImageState = iconImageState,
            iconType = iconType,
            isDarkTheme = prefsInteractor.getDarkMode(),
        )
    }

    private suspend fun updateCategoriesViewData() {
        val data = loadCategoriesViewData()
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return viewDataInteractor.getCategoriesViewData(newCategories)
    }

    private fun updateIconScrollPosition(position: Int) {
        iconsScrollPosition.set(ChangeRecordTypeScrollViewData.ScrollTo(position))
    }

    private fun showMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showMessage(stringResId)
    }

    private fun showArchivedMessage(stringResId: Int) {
        snackBarMessageNavigationInteractor.showArchiveMessage(stringResId)
    }
}
