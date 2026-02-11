package com.example.util.simpletimetracker.feature_change_goals.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view.ButtonsRowViewData
import com.example.util.simpletimetracker.domain.recordType.extension.getDaily
import com.example.util.simpletimetracker.domain.recordType.extension.getMonthly
import com.example.util.simpletimetracker.domain.recordType.extension.getSession
import com.example.util.simpletimetracker.domain.recordType.extension.getWeekly
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_goals.mapper.GoalsViewDataMapper
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalSubtypeViewData
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
    override val notificationsHintVisible: LiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
    private var newGoalsState: ChangeRecordTypeGoalsState = goalsViewDataMapper.getDefaultGoalState()

    override fun onNotificationsHintClick() {
        router.execute(OpenSystemSettings.Notifications)
    }

    override fun onGoalDurationSet(tag: String?, duration: Long, anchor: Any) {
        if (tag !in tags) return
        onNewGoalDuration(tag, duration)
        checkExactAlarmPermissionInteractor.execute(anchor)
    }

    override fun onGoalDurationDisabled(tag: String?) {
        if (tag !in tags) return
        onNewGoalDuration(tag, 0)
    }

    override fun onGoalTypeSelected(range: RecordTypeGoal.Range, position: Int) {
        val newType = goalsViewDataMapper.toGoalType(position)
        if (newGoalsState.getCurrentState(range).type::class.java == newType::class.java) return
        newGoalsState = newGoalsState.change(range) { copy(type = newType) }
        updateGoalsViewData()
    }

    override fun onGoalSubTypeSelected(range: RecordTypeGoal.Range, viewData: ButtonsRowViewData) {
        if (viewData !is ChangeRecordTypeGoalSubtypeViewData) return
        val newSubType = viewData.subtype
        if (newGoalsState.getCurrentState(range).subtype::class.java == newSubType::class.java) return
        newGoalsState = newGoalsState.change(range) { copy(subtype = newSubType) }
        updateGoalsViewData()
    }

    override fun onGoalCountChange(range: RecordTypeGoal.Range, count: String) {
        val currentState = when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.session
            is RecordTypeGoal.Range.Daily -> newGoalsState.daily
            is RecordTypeGoal.Range.Weekly -> newGoalsState.weekly
            is RecordTypeGoal.Range.Monthly -> newGoalsState.monthly
        }
        val currentCount = (currentState.type as? RecordTypeGoal.Type.Count)
            ?.value ?: return
        val newCount = count.toLongOrNull()

        if (currentCount != newCount) {
            val newType = RecordTypeGoal.Type.Count(newCount.orZero())
            val newState = currentState.copy(type = newType)
            newGoalsState = when (range) {
                is RecordTypeGoal.Range.Session -> newGoalsState.copy(session = newState)
                is RecordTypeGoal.Range.Daily -> newGoalsState.copy(daily = newState)
                is RecordTypeGoal.Range.Weekly -> newGoalsState.copy(weekly = newState)
                is RecordTypeGoal.Range.Monthly -> newGoalsState.copy(monthly = newState)
            }
            updateGoalsViewData()
        }
    }

    override fun onGoalTimeClick(range: RecordTypeGoal.Range) {
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
                value = DurationDialogParams.Value.DurationSeconds(
                    duration = goalType.type.value.orZero(),
                ),
            ),
        )
    }

    override fun onDayOfWeekClick(data: DayOfWeekViewData) {
        val current = newGoalsState.daysOfWeek
        val new = current.toMutableSet().apply { addOrRemove(data.dayOfWeek) }
        newGoalsState = newGoalsState.copy(daysOfWeek = new)
        updateGoalsViewData()
    }

    override fun onGoalsVisible() {
        updateNotificationsHintVisible()
    }

    override suspend fun saveGoals(
        id: RecordTypeGoal.IdData,
    ) {
        val goals = getGoals(id)

        suspend fun processGoal(
            goalId: Long,
            state: ChangeRecordTypeGoalsState.GoalState,
            goalRange: RecordTypeGoal.Range,
            daysOfWeek: Set<DayOfWeek>,
        ) {
            val type = state.type
            val goalType = state.subtype
            if (type.value == 0L) {
                recordTypeGoalInteractor.remove(goalId)
            } else {
                RecordTypeGoal(
                    id = goalId,
                    idData = id,
                    range = goalRange,
                    type = type,
                    subtype = goalType,
                    daysOfWeek = daysOfWeek,
                ).let {
                    recordTypeGoalInteractor.add(it)
                }
            }
        }

        processGoal(
            goalId = goals.getSession()?.id.orZero(),
            state = newGoalsState.session,
            goalRange = RecordTypeGoal.Range.Session,
            daysOfWeek = emptySet(),
        )
        processGoal(
            goalId = goals.getDaily()?.id.orZero(),
            state = newGoalsState.daily,
            goalRange = RecordTypeGoal.Range.Daily,
            daysOfWeek = newGoalsState.daysOfWeek,
        )
        processGoal(
            goalId = goals.getWeekly()?.id.orZero(),
            state = newGoalsState.weekly,
            goalRange = RecordTypeGoal.Range.Weekly,
            daysOfWeek = emptySet(),
        )
        processGoal(
            goalId = goals.getMonthly()?.id.orZero(),
            state = newGoalsState.monthly,
            goalRange = RecordTypeGoal.Range.Monthly,
            daysOfWeek = emptySet(),
        )
    }

    override suspend fun initialize(
        id: RecordTypeGoal.IdData,
    ) {
        val goals = getGoals(id)
        val defaultGoal = goalsViewDataMapper.getDefaultGoal()

        fun mapState(
            goal: RecordTypeGoal,
        ): ChangeRecordTypeGoalsState.GoalState {
            return ChangeRecordTypeGoalsState.GoalState(
                type = goal.type,
                subtype = goal.subtype,
            )
        }

        newGoalsState = ChangeRecordTypeGoalsState(
            session = goals.getSession()?.let(::mapState) ?: defaultGoal,
            daily = goals.getDaily()?.let(::mapState) ?: defaultGoal,
            weekly = goals.getWeekly()?.let(::mapState) ?: defaultGoal,
            monthly = goals.getMonthly()?.let(::mapState) ?: defaultGoal,
            daysOfWeek = goals.getDaily()?.daysOfWeek ?: DayOfWeek.entries.toSet(),
        )

        updateGoalsViewData()
    }

    private fun onNewGoalDuration(tag: String?, duration: Long) {
        val range = when (tag) {
            SESSION_GOAL_TIME_DIALOG_TAG -> RecordTypeGoal.Range.Session
            DAILY_GOAL_TIME_DIALOG_TAG -> RecordTypeGoal.Range.Daily
            WEEKLY_GOAL_TIME_DIALOG_TAG -> RecordTypeGoal.Range.Weekly
            MONTHLY_GOAL_TIME_DIALOG_TAG -> RecordTypeGoal.Range.Monthly
            else -> return
        }
        val newType = RecordTypeGoal.Type.Duration(duration)
        newGoalsState = newGoalsState.change(range) { copy(type = newType) }
        updateGoalsViewData()
    }

    private suspend fun getGoals(id: RecordTypeGoal.IdData): List<RecordTypeGoal> {
        return when (id) {
            is RecordTypeGoal.IdData.Type -> recordTypeGoalInteractor.getByType(id.value)
            is RecordTypeGoal.IdData.Category -> recordTypeGoalInteractor.getByCategory(id.value)
            is RecordTypeGoal.IdData.Tag -> recordTypeGoalInteractor.getByTag(id.value)
        }
    }

    private fun ChangeRecordTypeGoalsState.getCurrentState(
        range: RecordTypeGoal.Range,
    ): ChangeRecordTypeGoalsState.GoalState {
        return when (range) {
            is RecordTypeGoal.Range.Session -> session
            is RecordTypeGoal.Range.Daily -> daily
            is RecordTypeGoal.Range.Weekly -> weekly
            is RecordTypeGoal.Range.Monthly -> monthly
        }
    }

    private fun ChangeRecordTypeGoalsState.change(
        range: RecordTypeGoal.Range,
        producer: ChangeRecordTypeGoalsState.GoalState.() -> ChangeRecordTypeGoalsState.GoalState,
    ): ChangeRecordTypeGoalsState {
        val currentState = getCurrentState(range)
        val newState = currentState.producer()
        return when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.copy(session = newState)
            is RecordTypeGoal.Range.Daily -> newGoalsState.copy(daily = newState)
            is RecordTypeGoal.Range.Weekly -> newGoalsState.copy(weekly = newState)
            is RecordTypeGoal.Range.Monthly -> newGoalsState.copy(monthly = newState)
        }
    }

    private fun updateGoalsViewData() = delegateScope.launch {
        val data = loadGoalsViewData()
        goalsViewData.set(data)
    }

    private suspend fun loadGoalsViewData(): ChangeRecordTypeGoalsViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()

        return goalsViewDataMapper.mapGoalsState(
            goalsState = newGoalsState,
            isDarkTheme = isDarkTheme,
            firstDayOfWeek = firstDayOfWeek,
        )
    }

    private fun updateNotificationsHintVisible() {
        notificationsHintVisible.set(loadNotificationsHintVisible())
    }

    private fun loadNotificationsHintVisible(): Boolean {
        return !permissionRepo.areNotificationsEnabled()
    }

    companion object {
        private const val SESSION_GOAL_TIME_DIALOG_TAG = "session_goal_time_dialog_tag"
        private const val DAILY_GOAL_TIME_DIALOG_TAG = "daily_goal_time_dialog_tag"
        private const val WEEKLY_GOAL_TIME_DIALOG_TAG = "weekly_goal_time_dialog_tag"
        private const val MONTHLY_GOAL_TIME_DIALOG_TAG = "monthly_goal_time_dialog_tag"

        private val tags = listOf(
            SESSION_GOAL_TIME_DIALOG_TAG,
            DAILY_GOAL_TIME_DIALOG_TAG,
            WEEKLY_GOAL_TIME_DIALOG_TAG,
            MONTHLY_GOAL_TIME_DIALOG_TAG,
        )
    }
}