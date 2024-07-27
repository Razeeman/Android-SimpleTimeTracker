package com.example.util.simpletimetracker.feature_settings.viewModel.delegate

import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.SettingsBlock
import com.example.util.simpletimetracker.domain.extension.flip
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.PomodoroStopInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordsContainerUpdateInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.CardTagOrder
import com.example.util.simpletimetracker.domain.model.WidgetTransparencyPercent
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsDisplayViewDataInteractor
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewModel.SettingsViewModel
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardSizeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.TypesSelectionDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsDisplayViewModelDelegate @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val settingsDisplayViewDataInteractor: SettingsDisplayViewDataInteractor,
    private val pomodoroStopInteractor: PomodoroStopInteractor,
    private val recordsContainerUpdateInteractor: RecordsContainerUpdateInteractor,
) : ViewModelDelegate() {

    private var parent: SettingsParent? = null
    private var isCollapsed: Boolean = true

    fun init(parent: SettingsParent) {
        this.parent = parent
    }

    suspend fun getViewData(): List<ViewHolderType> {
        return settingsDisplayViewDataInteractor.execute(
            isCollapsed = isCollapsed,
        )
    }

    fun onBlockClicked(block: SettingsBlock) {
        when (block) {
            SettingsBlock.DisplayCollapse -> onCollapseClick()
            SettingsBlock.DisplayUntrackedIgnoreShort -> onIgnoreShortUntrackedClicked()
            SettingsBlock.DisplayUntrackedRangeStart -> onUntrackedRangeStartClicked()
            SettingsBlock.DisplayUntrackedRangeEnd -> onUntrackedRangeEndClicked()
            SettingsBlock.DisplayCardSize -> onChangeCardSizeClick()
            SettingsBlock.DisplaySortActivities -> onCardOrderManualClick()
            SettingsBlock.DisplaySortCategories -> onCategoryOrderManualClick()
            SettingsBlock.DisplaySortTags -> onTagOrderManualClick()
            SettingsBlock.DisplayUntrackedInRecords -> onShowUntrackedInRecordsClicked()
            SettingsBlock.DisplayUntrackedInStatistics -> onShowUntrackedInStatisticsClicked()
            SettingsBlock.DisplayUntrackedRangeCheckbox -> onUntrackedRangeClicked()
            SettingsBlock.DisplayCalendarView -> onShowRecordsCalendarClicked()
            SettingsBlock.DisplayCalendarButtonOnRecordsTab -> onShowCalendarButtonOnRecordsTabClicked()
            SettingsBlock.DisplayReverseOrder -> onReverseOrderInCalendarClicked()
            SettingsBlock.DisplayShowActivityFilters -> onShowActivityFiltersClicked()
            SettingsBlock.DisplayEnablePomodoroMode -> onEnablePomodoroModeClicked()
            SettingsBlock.DisplayEnableRepeatButton -> onEnableRepeatButtonClicked()
            SettingsBlock.DisplayPomodoroModeActivities -> onPomodoroModeActivitiesClicked()
            SettingsBlock.DisplayAllowMultipleActivityFilters -> onAllowMultipleActivityFiltersClicked()
            SettingsBlock.DisplayGoalsOnSeparateTabs -> onShowGoalsSeparatelyClicked()
            SettingsBlock.DisplayNavBarAtTheBottom -> onShowNavBarAtTheBottomClicked()
            SettingsBlock.DisplayMilitaryFormat -> onUseMilitaryTimeClicked()
            SettingsBlock.DisplayMonthDayFormat -> onUseMonthDayTimeClicked()
            SettingsBlock.DisplayProportionalFormat -> onUseProportionalMinutesClicked()
            SettingsBlock.DisplayShowSeconds -> onShowSecondsClicked()
            else -> {
                // Do nothing
            }
        }
    }

    fun onSpinnerPositionSelected(block: SettingsBlock, position: Int) {
        when (block) {
            SettingsBlock.DisplayDaysInCalendar -> onDaysInCalendarSelected(position)
            SettingsBlock.DisplayWidgetBackground -> onWidgetTransparencySelected(position)
            SettingsBlock.DisplaySortActivities -> onRecordTypeOrderSelected(position)
            SettingsBlock.DisplaySortCategories -> onCategoryOrderSelected(position)
            SettingsBlock.DisplaySortTags -> onTagOrderSelected(position)
            else -> {
                // Do nothing
            }
        }
    }

    fun onDurationSet(tag: String?, duration: Long) {
        onDurationSetDelegate(tag, duration)
    }

    fun onDurationDisabled(tag: String?) {
        onDurationDisabledDelegate(tag)
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) {
        onDateTimeSetDelegate(timestamp, tag)
    }

    fun onTypesSelected(typeIds: List<Long>, tag: String?) {
        onTypesSelectedDelegate(typeIds, tag)
    }

    private fun onCollapseClick() = delegateScope.launch {
        isCollapsed = isCollapsed.flip()
        parent?.updateContent()
    }

    private fun onDaysInCalendarSelected(position: Int) {
        delegateScope.launch {
            val currentValue = prefsInteractor.getDaysInCalendar()
            val newValue = settingsMapper.toDaysInCalendar(position)
            if (newValue == currentValue) return@launch
            prefsInteractor.setDaysInCalendar(newValue)
            parent?.updateContent()
            recordsContainerUpdateInteractor.sendCalendarDaysUpdated()
        }
    }

    private fun onWidgetTransparencySelected(position: Int) {
        delegateScope.launch {
            val currentValue = prefsInteractor.getWidgetBackgroundTransparencyPercent()
                .let(::WidgetTransparencyPercent)
            val newValue = settingsMapper.toWidgetTransparency(position)
            if (newValue == currentValue) return@launch
            prefsInteractor.setWidgetBackgroundTransparencyPercent(newValue.value)
            parent?.updateContent()
            widgetInteractor.updateWidgets()
        }
    }

    private fun onRecordTypeOrderSelected(position: Int) {
        onOrderSelected(
            type = CardOrderDialogParams.Type.RecordType(
                order = settingsMapper.toCardOrder(position),
            ),
        )
    }

    private fun onCategoryOrderSelected(position: Int) {
        onOrderSelected(
            type = CardOrderDialogParams.Type.Category(
                order = settingsMapper.toCardOrder(position),
            ),
        )
    }

    private fun onTagOrderSelected(position: Int) {
        onOrderSelected(
            type = CardOrderDialogParams.Type.Tag(
                order = settingsMapper.toCardTagOrder(position),
            ),
        )
    }

    private fun onCardOrderManualClick() {
        openOrderDialog(
            type = CardOrderDialogParams.Type.RecordType(
                order = CardOrder.MANUAL,
            ),
        )
    }

    private fun onCategoryOrderManualClick() {
        openOrderDialog(
            type = CardOrderDialogParams.Type.Category(
                order = CardOrder.MANUAL,
            ),
        )
    }

    private fun onTagOrderManualClick() {
        openOrderDialog(
            type = CardOrderDialogParams.Type.Tag(
                order = CardTagOrder.MANUAL,
            ),
        )
    }

    private fun onShowUntrackedInRecordsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowUntrackedInRecords()
            prefsInteractor.setShowUntrackedInRecords(newValue)
            parent?.updateContent()
        }
    }

    private fun onShowUntrackedInStatisticsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowUntrackedInStatistics()
            prefsInteractor.setShowUntrackedInStatistics(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            parent?.updateContent()
        }
    }

    private fun onIgnoreShortUntrackedClicked() {
        delegateScope.launch {
            DurationDialogParams(
                tag = SettingsViewModel.IGNORE_SHORT_UNTRACKED_DIALOG_TAG,
                value = DurationDialogParams.Value.Duration(
                    duration = prefsInteractor.getIgnoreShortUntrackedDuration(),
                ),
            ).let(router::navigate)
        }
    }

    private fun onUntrackedRangeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUntrackedRangeEnabled()
            prefsInteractor.setUntrackedRangeEnabled(newValue)
            parent?.updateContent()
        }
    }

    private fun onUntrackedRangeStartClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.UNTRACKED_RANGE_START_DIALOG_TAG,
                timestamp = prefsInteractor.getUntrackedRangeStart(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onUntrackedRangeEndClicked() {
        delegateScope.launch {
            parent?.openDateTimeDialog(
                tag = SettingsViewModel.UNTRACKED_RANGE_END_DIALOG_TAG,
                timestamp = prefsInteractor.getUntrackedRangeEnd(),
                useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat(),
            )
        }
    }

    private fun onShowRecordsCalendarClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowRecordsCalendar()
            prefsInteractor.setShowRecordsCalendar(newValue)
            parent?.updateContent()
            recordsContainerUpdateInteractor.sendShowCalendarUpdated()
        }
    }

    private fun onShowCalendarButtonOnRecordsTabClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowCalendarButtonOnRecordsTab()
            prefsInteractor.setShowCalendarButtonOnRecordsTab(newValue)
            parent?.updateContent()
            recordsContainerUpdateInteractor.sendShowCalendarSwitchUpdated()
        }
    }

    private fun onReverseOrderInCalendarClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getReverseOrderInCalendar()
            prefsInteractor.setReverseOrderInCalendar(newValue)
            parent?.updateContent()
        }
    }

    private fun onShowActivityFiltersClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowActivityFilters()
            prefsInteractor.setShowActivityFilters(newValue)
            parent?.updateContent()
        }
    }

    private fun onAllowMultipleActivityFiltersClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getAllowMultipleActivityFilters()
            prefsInteractor.setAllowMultipleActivityFilters(newValue)
            parent?.updateContent()
        }
    }

    private fun onEnableRepeatButtonClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getEnableRepeatButton()
            prefsInteractor.setEnableRepeatButton(newValue)
            notificationTypeInteractor.updateNotifications()
            parent?.updateContent()
        }
    }

    private fun onEnablePomodoroModeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getEnablePomodoroMode()
            prefsInteractor.setEnablePomodoroMode(newValue)
            if (!newValue) pomodoroStopInteractor.stop()
            parent?.updateContent()
        }
    }

    private fun onPomodoroModeActivitiesClicked() = delegateScope.launch {
        TypesSelectionDialogParams(
            tag = SettingsViewModel.SELECT_ACTIVITIES_TO_AUTOSTART_POMODORO,
            title = resourceRepo.getString(
                R.string.select_activities_to_autostart_pomodoro_title,
            ),
            subtitle = resourceRepo.getString(
                R.string.select_activities_to_autostart_pomodoro_hint,
            ),
            selectedTypeIds = prefsInteractor.getAutostartPomodoroActivities(),
            isMultiSelectAvailable = true,
        ).let(router::navigate)
    }

    private fun onTypesSelectedDelegate(typeIds: List<Long>, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.SELECT_ACTIVITIES_TO_AUTOSTART_POMODORO -> {
                prefsInteractor.setAutostartPomodoroActivities(typeIds)
            }
        }
    }

    private fun onShowGoalsSeparatelyClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowGoalsSeparately()
            prefsInteractor.setShowGoalsSeparately(newValue)
            parent?.updateContent()
            router.restartApp()
        }
    }

    private fun onShowNavBarAtTheBottomClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getIsNavBarAtTheBottom()
            prefsInteractor.setIsNavBarAtTheBottom(newValue)
            parent?.updateContent()
            router.restartApp()
        }
    }

    private fun onUseMilitaryTimeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUseMilitaryTimeFormat()
            prefsInteractor.setUseMilitaryTimeFormat(newValue)
            notificationTypeInteractor.updateNotifications()
            parent?.updateContent()
        }
    }

    private fun onUseMonthDayTimeClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUseMonthDayTimeFormat()
            prefsInteractor.setUseMonthDayTimeFormat(newValue)
            parent?.updateContent()
        }
    }

    private fun onUseProportionalMinutesClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getUseProportionalMinutes()
            prefsInteractor.setUseProportionalMinutes(newValue)
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            parent?.updateContent()
        }
    }

    private fun onShowSecondsClicked() {
        delegateScope.launch {
            val newValue = !prefsInteractor.getShowSeconds()
            prefsInteractor.setShowSeconds(newValue)
            parent?.updateContent()
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
        }
    }

    private fun onChangeCardSizeClick() {
        router.navigate(CardSizeDialogParams)
    }

    private fun onDurationSetDelegate(tag: String?, duration: Long) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_UNTRACKED_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortUntrackedDuration(duration)
                parent?.updateContent()
            }
        }
    }

    private fun onDurationDisabledDelegate(tag: String?) {
        when (tag) {
            SettingsViewModel.IGNORE_SHORT_UNTRACKED_DIALOG_TAG -> delegateScope.launch {
                prefsInteractor.setIgnoreShortUntrackedDuration(0)
                parent?.updateContent()
            }
        }
    }

    private fun onDateTimeSetDelegate(timestamp: Long, tag: String?) = delegateScope.launch {
        when (tag) {
            SettingsViewModel.UNTRACKED_RANGE_START_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setUntrackedRangeStart(newValue)
                parent?.updateContent()
            }

            SettingsViewModel.UNTRACKED_RANGE_END_DIALOG_TAG -> {
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive = true)
                prefsInteractor.setUntrackedRangeEnd(newValue)
                parent?.updateContent()
            }
        }
    }

    private fun onOrderSelected(
        type: CardOrderDialogParams.Type,
    ) {
        delegateScope.launch {
            when (type) {
                is CardOrderDialogParams.Type.RecordType -> {
                    val currentOrder = prefsInteractor.getCardOrder()
                    val newOrder = type.order
                    if (newOrder == currentOrder) return@launch
                    if (newOrder == CardOrder.MANUAL) openOrderDialog(type.copy(order = currentOrder))
                    prefsInteractor.setCardOrder(newOrder)
                }
                is CardOrderDialogParams.Type.Category -> {
                    val currentOrder = prefsInteractor.getCategoryOrder()
                    val newOrder = type.order
                    if (newOrder == currentOrder) return@launch
                    if (newOrder == CardOrder.MANUAL) openOrderDialog(type.copy(order = currentOrder))
                    prefsInteractor.setCategoryOrder(newOrder)
                }
                is CardOrderDialogParams.Type.Tag -> {
                    val currentOrder = prefsInteractor.getTagOrder()
                    val newOrder = type.order
                    if (newOrder == currentOrder) return@launch
                    if (newOrder == CardTagOrder.MANUAL) openOrderDialog(type.copy(order = currentOrder))
                    prefsInteractor.setTagOrder(newOrder)
                }
            }
            parent?.updateContent()
        }
    }

    private fun openOrderDialog(
        type: CardOrderDialogParams.Type,
    ) {
        router.navigate(CardOrderDialogParams(type))
    }
}