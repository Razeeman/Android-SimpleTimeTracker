package com.example.util.simpletimetracker.feature_change_goals.delegate

import androidx.lifecycle.LiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.extension.value
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.mapper.GoalsViewDataMapper
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalSubtypeViewData
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.action.OpenSystemSettings
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoalsViewModelDelegateImpl @Inject constructor(
    private val router: Router,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val goalsViewDataMapper: GoalsViewDataMapper,
    private val checkExactAlarmPermissionInteractor: CheckExactAlarmPermissionInteractor,
    private val permissionRepo: PermissionRepo,
    private val prefsInteractor: PrefsInteractor,
) : GoalsViewModelDelegate, ViewModelDelegate() {

    override val goalsViewData: LiveData<ChangeRecordTypeGoalsViewData> by lazySuspend {
        loadGoalsViewData()
    }

    private var newGoalsState: ChangeRecordTypeGoalsState = goalsViewDataMapper.getDefaultGoalState()
    private var notificationsHintVisible: Boolean = false

    // Unsaved goals all have an id of 0, so negative keys uniquely identify them in the UI.
    private var nextNewGoalKey = -1L

    override fun onNotificationsHintClick() {
        router.execute(OpenSystemSettings.Notifications)
    }

    override fun onGoalDurationSet(tag: String?, duration: Long, anchor: Any) {
        val key = getGoalKeyFromTag(tag) ?: return
        changeGoal(key) { copy(type = RecordTypeGoal.Type.Duration(duration)) }
        checkExactAlarmPermissionInteractor.execute(anchor)
    }

    override fun onGoalDurationDisabled(tag: String?) {
        val key = getGoalKeyFromTag(tag) ?: return
        changeGoal(key) { copy(type = RecordTypeGoal.Type.Duration(0)) }
    }

    override fun onGoalAdd() {
        val key = nextNewGoalKey
        nextNewGoalKey -= 1
        val newGoal = goalsViewDataMapper.getDefaultGoal(key)
        newGoalsState = newGoalsState.copy(data = newGoalsState.data + newGoal)
        updateGoalsViewData()
    }

    override fun onGoalScrollHandled(key: Long) {
        changeGoal(key) {
            if (requestScroll) copy(requestScroll = false) else this
        }
    }

    override fun onGoalRemove(key: Long) {
        if (newGoalsState.data.none { it.key == key }) return
        val newGoals = newGoalsState.data.filterNot { it.key == key }
        newGoalsState = newGoalsState.copy(data = newGoals)
        updateGoalsViewData()
    }

    override fun onGoalRangeSelected(key: Long, position: Int) {
        val newRange = goalsViewDataMapper.toGoalRange(position)
        changeGoal(key) {
            if (range::class.java == newRange::class.java) return@changeGoal this
            val needToResetType = newRange is RecordTypeGoal.Range.Session && type is RecordTypeGoal.Type.Count
            val newType = if (needToResetType) RecordTypeGoal.Type.Duration(0) else type
            copy(range = newRange, type = newType)
        }
    }

    override fun onGoalTypeSelected(key: Long, position: Int) {
        changeGoal(key) {
            if (range is RecordTypeGoal.Range.Session) return@changeGoal this
            val newType = goalsViewDataMapper.toGoalType(position)
            if (type::class.java == newType::class.java) {
                this
            } else {
                copy(type = newType)
            }
        }
    }

    override fun onGoalSubTypeSelected(key: Long, viewData: ButtonsRowViewData) {
        if (viewData !is ChangeRecordTypeGoalSubtypeViewData) return
        changeGoal(key) {
            if (subtype::class.java == viewData.subtype::class.java) {
                this
            } else {
                copy(subtype = viewData.subtype)
            }
        }
    }

    override fun onGoalCountChange(key: Long, count: String) {
        changeGoal(key) {
            val currentCount = (type as? RecordTypeGoal.Type.Count)?.value
                ?: return@changeGoal this
            val newCount = count.toLongOrNull().orZero()
            if (currentCount == newCount) {
                this
            } else {
                copy(type = RecordTypeGoal.Type.Count(newCount))
            }
        }
    }

    override fun onGoalTimeClick(key: Long) {
        val goal = newGoalsState.data.find { it.key == key } ?: return
        if (goal.type !is RecordTypeGoal.Type.Duration) return
        val data = DurationDialogParams(
            tag = getDurationDialogTag(key),
            value = DurationDialogParams.Value.DurationSeconds(duration = goal.type.value),
        )
        router.navigate(data)
    }

    override fun onDayOfWeekClick(key: Long, data: DayOfWeekViewData) {
        changeGoal(key) {
            val newDays = daysOfWeek.addOrRemove(data.dayOfWeek)
            copy(daysOfWeek = newDays)
        }
    }

    override fun onGoalsVisible() {
        val visible = !permissionRepo.areNotificationsEnabled()
        if (notificationsHintVisible == visible) return
        notificationsHintVisible = visible
        updateGoalsViewData()
    }

    override suspend fun saveGoals(id: RecordTypeGoal.IdData): List<Long> {
        val storedGoalIds = getGoals(id)
            .map(RecordTypeGoal::id)
            .filterNot { it == 0L }.toSet()
        val keptGoalIds = newGoalsState.data
            .filter { it.type.value > 0L }
            .map(ChangeRecordTypeGoalsState.GoalState::id)
            .filterNot { it == 0L }
            .toSet()
        val removedGoalIds = (storedGoalIds - keptGoalIds).sorted()

        newGoalsState.data.filter { it.type.value > 0L }.forEach { state ->
            val data = RecordTypeGoal(
                id = state.id,
                idData = id,
                range = state.range,
                type = state.type,
                subtype = state.subtype,
                daysOfWeek = state.daysOfWeek
                    .takeIf { state.range is RecordTypeGoal.Range.Daily }
                    .orEmpty(),
            )
            recordTypeGoalInteractor.add(data)
        }
        removedGoalIds.forEach { recordTypeGoalInteractor.remove(it) }

        return removedGoalIds
    }

    override suspend fun initialize(id: RecordTypeGoal.IdData) {
        val goals = getGoals(id).let(::sortGoals).map { goal ->
            ChangeRecordTypeGoalsState.GoalState(
                key = goal.id,
                id = goal.id,
                range = goal.range,
                type = goal.type,
                subtype = goal.subtype,
                daysOfWeek = goal.daysOfWeek
                    .takeIf { goal.range is RecordTypeGoal.Range.Daily }
                    ?: DayOfWeek.entries.toSet(),
                requestScroll = false,
            )
        }
        newGoalsState = ChangeRecordTypeGoalsState(goals)
        nextNewGoalKey = -1L
        updateGoalsViewData()
    }

    private fun changeGoal(
        key: Long,
        producer: ChangeRecordTypeGoalsState.GoalState.() -> ChangeRecordTypeGoalsState.GoalState,
    ) {
        val index = newGoalsState.data.indexOfFirst { it.key == key }
        if (index == -1) return
        val current = newGoalsState.data[index]
        val changed = current.producer()
        if (current == changed) return
        val newData = newGoalsState.data.toMutableList().apply { set(index, changed) }
        newGoalsState = newGoalsState.copy(data = newData)
        updateGoalsViewData()
    }

    private suspend fun getGoals(id: RecordTypeGoal.IdData): List<RecordTypeGoal> {
        return when (id) {
            is RecordTypeGoal.IdData.Type -> recordTypeGoalInteractor.getByType(id.value)
            is RecordTypeGoal.IdData.Category -> recordTypeGoalInteractor.getByCategory(id.value)
            is RecordTypeGoal.IdData.Tag -> recordTypeGoalInteractor.getByTag(id.value)
        }
    }

    private fun sortGoals(goals: List<RecordTypeGoal>): List<RecordTypeGoal> {
        val rangeOrder = listOf(
            RecordTypeGoal.Range.Session,
            RecordTypeGoal.Range.Daily,
            RecordTypeGoal.Range.Weekly,
            RecordTypeGoal.Range.Monthly,
        )
        return goals.sortedWith(
            compareBy<RecordTypeGoal> { rangeOrder.indexOf(it.range) }
                .thenBy {
                    when (it.type) {
                        is RecordTypeGoal.Type.Duration -> 0
                        is RecordTypeGoal.Type.Count -> 1
                    }
                }
                .thenBy(RecordTypeGoal::value)
                .thenBy(RecordTypeGoal::id),
        )
    }

    private fun getGoalKeyFromTag(tag: String?): Long? {
        if (tag?.startsWith(DURATION_DIALOG_TAG_PREFIX) != true) return null
        return tag.removePrefix(DURATION_DIALOG_TAG_PREFIX).toLongOrNull()
            ?.takeIf { key -> newGoalsState.data.any { it.key == key } }
    }

    private fun getDurationDialogTag(key: Long): String {
        return "$DURATION_DIALOG_TAG_PREFIX$key"
    }

    private fun updateGoalsViewData() = delegateScope.launch {
        goalsViewData.set(loadGoalsViewData())
    }

    private suspend fun loadGoalsViewData(): ChangeRecordTypeGoalsViewData {
        return goalsViewDataMapper.mapGoalsState(
            goalsState = newGoalsState,
            notificationsHintVisible = notificationsHintVisible,
            isDarkTheme = prefsInteractor.getDarkMode(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
    }

    companion object {
        private const val DURATION_DIALOG_TAG_PREFIX = "goal_time_dialog_tag:"
    }
}