package com.example.util.simpletimetracker.feature_change_record_type.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.extension.getDaily
import com.example.util.simpletimetracker.domain.extension.getMonthly
import com.example.util.simpletimetracker.domain.extension.getSession
import com.example.util.simpletimetracker.domain.extension.getWeekly
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.IconType
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData
import com.example.util.simpletimetracker.feature_change_record_type.R
import com.example.util.simpletimetracker.feature_change_record_type.interactor.ChangeRecordTypeViewDataInteractor
import com.example.util.simpletimetracker.feature_change_record_type.mapper.ChangeRecordTypeMapper
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeChooserState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryInfoViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconCategoryViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconStateViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconSwitchViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeIconViewData
import com.example.util.simpletimetracker.feature_change_record_type.viewData.ChangeRecordTypeScrollViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromChangeActivityParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeTagData
import com.example.util.simpletimetracker.navigation.params.screen.ColorSelectionDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeRecordTypeViewModel @Inject constructor(
    private val router: Router,
    private val permissionRepo: PermissionRepo,
    private val removeRunningRecordMediator: RemoveRunningRecordMediator,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
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
    private val iconEmojiMapper: IconEmojiMapper,
    private val colorMapper: ColorMapper,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
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
    val goalsViewData: LiveData<ChangeRecordTypeGoalsViewData> by lazy {
        return@lazy MutableLiveData(loadGoalsViewData())
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
    val notificationsHintVisible: LiveData<Boolean> by lazy {
        MutableLiveData(false)
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
    private var initialCategories: List<Long> = emptyList()
    private var newName: String = ""
    private var newIconName: String = ""
    private var newCategories: MutableList<Long> = mutableListOf()
    private var newColor: AppColor = AppColor(colorId = (0..ColorMapper.colorsNumber).random(), colorInt = "")
    private var newGoalsState: ChangeRecordTypeGoalsState = changeRecordTypeMapper.getDefaultGoalState()

    fun onVisible() = viewModelScope.launch {
        initializeSelectedCategories()
        updateCategoriesViewData()
        updateNotificationsHintVisible()
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
            if (viewData.iconType != IconType.TEXT) {
                icons.set(ChangeRecordTypeIconStateViewData.Icons(emptyList()))
            }
            iconType = viewData.iconType
            updateIconsTypeViewData()
            updateIconCategories(selectedIndex = 0)
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

    fun onGoalTypeSelected(range: RecordTypeGoal.Range, position: Int) {
        val currentType = when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.session
            is RecordTypeGoal.Range.Daily -> newGoalsState.daily
            is RecordTypeGoal.Range.Weekly -> newGoalsState.weekly
            is RecordTypeGoal.Range.Monthly -> newGoalsState.monthly
        }
        val newType = changeRecordTypeMapper.toGoalType(position)
        if (currentType::class.java == newType::class.java) return

        newGoalsState = when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.copy(session = newType)
            is RecordTypeGoal.Range.Daily -> newGoalsState.copy(daily = newType)
            is RecordTypeGoal.Range.Weekly -> newGoalsState.copy(weekly = newType)
            is RecordTypeGoal.Range.Monthly -> newGoalsState.copy(monthly = newType)
        }
        updateGoalsViewData()
    }

    fun onGoalTimeClick(range: RecordTypeGoal.Range) {
        val tag = when (range) {
            is RecordTypeGoal.Range.Session -> SESSION_GOAL_TIME_DIALOG_TAG
            is RecordTypeGoal.Range.Daily -> DAILY_GOAL_TIME_DIALOG_TAG
            is RecordTypeGoal.Range.Weekly -> WEEKLY_GOAL_TIME_DIALOG_TAG
            is RecordTypeGoal.Range.Monthly -> MONTHLY_GOAL_TIME_DIALOG_TAG
        }
        val goalType = when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.session
            is RecordTypeGoal.Range.Daily -> newGoalsState.daily
            is RecordTypeGoal.Range.Weekly -> newGoalsState.weekly
            is RecordTypeGoal.Range.Monthly -> newGoalsState.monthly
        }

        router.navigate(
            DurationDialogParams(
                tag = tag,
                duration = goalType.value.orZero(),
            ),
        )
    }

    fun onGoalCountChange(range: RecordTypeGoal.Range, count: String) {
        val currentGoal = when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.session
            is RecordTypeGoal.Range.Daily -> newGoalsState.daily
            is RecordTypeGoal.Range.Weekly -> newGoalsState.weekly
            is RecordTypeGoal.Range.Monthly -> newGoalsState.monthly
        }
        val currentCount = (currentGoal as? RecordTypeGoal.Type.Count)
            ?.value ?: return
        val newCount = count.toLongOrNull()

        if (currentCount != newCount) {
            val newType = RecordTypeGoal.Type.Count(newCount.orZero())
            newGoalsState = when (range) {
                is RecordTypeGoal.Range.Session -> newGoalsState.copy(session = newType)
                is RecordTypeGoal.Range.Daily -> newGoalsState.copy(daily = newType)
                is RecordTypeGoal.Range.Weekly -> newGoalsState.copy(weekly = newType)
                is RecordTypeGoal.Range.Monthly -> newGoalsState.copy(monthly = newType)
            }
            updateGoalsViewData()
        }
    }

    fun onDurationSet(tag: String?, duration: Long, anchor: Any) {
        onNewGoalDuration(tag, duration)
        checkExactAlarmPermissionInteractor.execute(anchor)
    }

    fun onDurationDisabled(tag: String?) {
        onNewGoalDuration(tag, 0)
    }

    fun onNotificationsHintClick() {
        router.execute(OpenSystemSettings.Notifications)
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
                showMessage(R.string.change_record_type_archived)
                keyboardVisibility.set(false)
                router.back()
            }
        }
    }

    fun onSaveClick() {
        if (newName.isEmpty()) {
            showMessage(R.string.change_record_message_choose_name)
            return
        }
        saveButtonEnabled.set(false)
        viewModelScope.launch {
            val addedId = saveRecordType()
            saveCategories(addedId)
            saveGoals(addedId)
            notificationTypeInteractor.updateNotifications()
            notificationGoalTimeInteractor.checkAndReschedule(recordTypeId)
            widgetInteractor.updateWidgets()
            keyboardVisibility.set(false)
            router.back()
        }
    }

    fun onScrolled() {
        iconsScrollPosition.set(ChangeRecordTypeScrollViewData.NoScroll)
    }

    private fun onNewGoalDuration(tag: String?, duration: Long) {
        val newType = RecordTypeGoal.Type.Duration(duration)

        when (tag) {
            SESSION_GOAL_TIME_DIALOG_TAG -> {
                newGoalsState = newGoalsState.copy(session = newType)
            }
            DAILY_GOAL_TIME_DIALOG_TAG -> {
                newGoalsState = newGoalsState.copy(daily = newType)
            }
            WEEKLY_GOAL_TIME_DIALOG_TAG -> {
                newGoalsState = newGoalsState.copy(weekly = newType)
            }
            MONTHLY_GOAL_TIME_DIALOG_TAG -> {
                newGoalsState = newGoalsState.copy(monthly = newType)
            }
        }

        updateGoalsViewData()
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

    private suspend fun saveGoals(typeId: Long) {
        val goals = recordTypeGoalInteractor.getByType(typeId)

        suspend fun processGoal(
            goalId: Long,
            goalType: RecordTypeGoal.Type?,
            goalRange: RecordTypeGoal.Range,
        ) {
            if (goalType == null || goalType.value == 0L) {
                recordTypeGoalInteractor.remove(goalId)
            } else {
                RecordTypeGoal(
                    id = goalId,
                    typeId = typeId,
                    range = goalRange,
                    type = goalType,
                ).let {
                    recordTypeGoalInteractor.add(it)
                }
            }
        }

        processGoal(
            goalId = goals.getSession()?.id.orZero(),
            goalType = newGoalsState.session,
            goalRange = RecordTypeGoal.Range.Session,
        )
        processGoal(
            goalId = goals.getDaily()?.id.orZero(),
            goalType = newGoalsState.daily,
            goalRange = RecordTypeGoal.Range.Daily,
        )
        processGoal(
            goalId = goals.getWeekly()?.id.orZero(),
            goalType = newGoalsState.weekly,
            goalRange = RecordTypeGoal.Range.Weekly,
        )
        processGoal(
            goalId = goals.getMonthly()?.id.orZero(),
            goalType = newGoalsState.monthly,
            goalRange = RecordTypeGoal.Range.Monthly,
        )
    }

    private suspend fun initializeRecordTypeData() {
        recordTypeInteractor.get(recordTypeId)?.let {
            val goals = recordTypeGoalInteractor.getByType(it.id)
            val defaultGoal = changeRecordTypeMapper.getDefaultGoal()

            newName = it.name
            newIconName = it.icon
            newColor = it.color
            newGoalsState = ChangeRecordTypeGoalsState(
                session = goals.getSession()?.type ?: defaultGoal,
                daily = goals.getDaily()?.type ?: defaultGoal,
                weekly = goals.getWeekly()?.type ?: defaultGoal,
                monthly = goals.getMonthly()?.type ?: defaultGoal,
            )
            updateIcons()
            updateColors()
            updateGoalsViewData()
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

    private suspend fun loadIconsViewData(): ChangeRecordTypeIconStateViewData {
        return viewDataInteractor.getIconsViewData(newColor, iconType)
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

    private suspend fun updateCategoriesViewData() {
        val data = loadCategoriesViewData()
        categories.set(data)
    }

    private suspend fun loadCategoriesViewData(): List<ViewHolderType> {
        return viewDataInteractor.getCategoriesViewData(newCategories)
    }

    private fun updateGoalsViewData() {
        val data = loadGoalsViewData()
        goalsViewData.set(data)
    }

    private fun loadGoalsViewData(): ChangeRecordTypeGoalsViewData {
        return changeRecordTypeMapper.mapGoalsState(newGoalsState)
    }

    private fun updateNotificationsHintVisible() {
        notificationsHintVisible.set(loadNotificationsHintVisible())
    }

    private fun loadNotificationsHintVisible(): Boolean {
        return !permissionRepo.areNotificationsEnabled()
    }

    private fun updateIconScrollPosition(position: Int) {
        iconsScrollPosition.set(ChangeRecordTypeScrollViewData.ScrollTo(position))
    }

    private fun showMessage(stringResId: Int) {
        val params = SnackBarParams(
            message = resourceRepo.getString(stringResId),
            duration = SnackBarParams.Duration.Short,
            margins = SnackBarParams.Margins(
                bottom = resourceRepo.getDimenInDp(R.dimen.button_height),
            ),
        )
        router.show(params)
    }

    companion object {
        private const val SESSION_GOAL_TIME_DIALOG_TAG = "session_goal_time_dialog_tag"
        private const val DAILY_GOAL_TIME_DIALOG_TAG = "daily_goal_time_dialog_tag"
        private const val WEEKLY_GOAL_TIME_DIALOG_TAG = "weekly_goal_time_dialog_tag"
        private const val MONTHLY_GOAL_TIME_DIALOG_TAG = "monthly_goal_time_dialog_tag"
    }
}
