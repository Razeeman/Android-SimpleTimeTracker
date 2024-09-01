package com.example.util.simpletimetracker.feature_change_goals.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.domain.extension.addOrRemove
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.CheckExactAlarmPermissionInteractor
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.domain.extension.getDaily
import com.example.util.simpletimetracker.domain.extension.getMonthly
import com.example.util.simpletimetracker.domain.extension.getSession
import com.example.util.simpletimetracker.domain.extension.getWeekly
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_change_goals.mapper.GoalsViewDataMapper
import com.example.util.simpletimetracker.feature_change_goals.viewData.ChangeRecordTypeGoalsState
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
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

    override fun onDurationSet(tag: String?, duration: Long, anchor: Any) {
        onNewGoalDuration(tag, duration)
        checkExactAlarmPermissionInteractor.execute(anchor)
    }

    override fun onDurationDisabled(tag: String?) {
        onNewGoalDuration(tag, 0)
    }

    override fun onGoalTypeSelected(range: RecordTypeGoal.Range, position: Int) {
        val currentType = when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.session
            is RecordTypeGoal.Range.Daily -> newGoalsState.daily
            is RecordTypeGoal.Range.Weekly -> newGoalsState.weekly
            is RecordTypeGoal.Range.Monthly -> newGoalsState.monthly
        }
        val newType = goalsViewDataMapper.toGoalType(position)
        if (currentType::class.java == newType::class.java) return

        newGoalsState = when (range) {
            is RecordTypeGoal.Range.Session -> newGoalsState.copy(session = newType)
            is RecordTypeGoal.Range.Daily -> newGoalsState.copy(daily = newType)
            is RecordTypeGoal.Range.Weekly -> newGoalsState.copy(weekly = newType)
            is RecordTypeGoal.Range.Monthly -> newGoalsState.copy(monthly = newType)
        }
        updateGoalsViewData()
    }

    override fun onGoalCountChange(range: RecordTypeGoal.Range, count: String) {
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
                    duration = goalType.value.orZero(),
                ),
            ),
        )
    }

    override fun onDayOfWeekClick(data: DayOfWeekViewData) {
        val current = newGoalsState.daysOfWeek
        val new = current.toMutableList().apply { addOrRemove(data.dayOfWeek) }
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
            goalType: RecordTypeGoal.Type,
            goalRange: RecordTypeGoal.Range,
            daysOfWeek: List<DayOfWeek>,
        ) {
            if (goalType.value == 0L) {
                recordTypeGoalInteractor.remove(goalId)
            } else {
                RecordTypeGoal(
                    id = goalId,
                    idData = id,
                    range = goalRange,
                    type = goalType,
                    daysOfWeek = daysOfWeek,
                ).let {
                    recordTypeGoalInteractor.add(it)
                }
            }
        }

        processGoal(
            goalId = goals.getSession()?.id.orZero(),
            goalType = newGoalsState.session,
            goalRange = RecordTypeGoal.Range.Session,
            daysOfWeek = emptyList(),
        )
        processGoal(
            goalId = goals.getDaily()?.id.orZero(),
            goalType = newGoalsState.daily,
            goalRange = RecordTypeGoal.Range.Daily,
            daysOfWeek = newGoalsState.daysOfWeek,
        )
        processGoal(
            goalId = goals.getWeekly()?.id.orZero(),
            goalType = newGoalsState.weekly,
            goalRange = RecordTypeGoal.Range.Weekly,
            daysOfWeek = emptyList(),
        )
        processGoal(
            goalId = goals.getMonthly()?.id.orZero(),
            goalType = newGoalsState.monthly,
            goalRange = RecordTypeGoal.Range.Monthly,
            daysOfWeek = emptyList(),
        )
    }

    override suspend fun initialize(
        id: RecordTypeGoal.IdData,
    ) {
        val goals = getGoals(id)
        val defaultGoal = goalsViewDataMapper.getDefaultGoal()

        newGoalsState = ChangeRecordTypeGoalsState(
            session = goals.getSession()?.type ?: defaultGoal,
            daily = goals.getDaily()?.type ?: defaultGoal,
            weekly = goals.getWeekly()?.type ?: defaultGoal,
            monthly = goals.getMonthly()?.type ?: defaultGoal,
            daysOfWeek = goals.getDaily()?.daysOfWeek ?: DayOfWeek.values().toList(),
        )

        updateGoalsViewData()
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

    private suspend fun getGoals(id: RecordTypeGoal.IdData): List<RecordTypeGoal> {
        return when (id) {
            is RecordTypeGoal.IdData.Type -> recordTypeGoalInteractor.getByType(id.value)
            is RecordTypeGoal.IdData.Category -> recordTypeGoalInteractor.getByCategory(id.value)
        }
    }

    private fun updateGoalsViewData() = delegateScope.launch {
        val data = loadGoalsViewData()
        goalsViewData.set(data)
    }

    private suspend fun loadGoalsViewData(): ChangeRecordTypeGoalsViewData {
        return goalsViewDataMapper.mapGoalsState(
            goalsState = newGoalsState,
            isDarkTheme = prefsInteractor.getDarkMode(),
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
    }
}