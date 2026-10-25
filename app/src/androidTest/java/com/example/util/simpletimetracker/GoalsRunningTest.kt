package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.GoalsTestUtils.checkRunningGoal
import com.example.util.simpletimetracker.GoalsTestUtils.checkRunningMark
import com.example.util.simpletimetracker.GoalsTestUtils.checkRunningLimitMark
import com.example.util.simpletimetracker.GoalsTestUtils.checkNoRunningGoal
import com.example.util.simpletimetracker.GoalsTestUtils.durationInSeconds
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_running_records.R as runningRecordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GoalsRunningTest : BaseUiTest() {

    @Test
    fun noGoals() {
        val noGoals = "noGoals"

        // Add data
        testUtils.addActivity(noGoals)
        testUtils.addRunningRecord(noGoals)

        // No goals
        scrollTo(noGoals)
        checkNoRunningGoal(noGoals)
        checkRunningMark(noGoals, isVisible = false)
    }

    @Test
    fun sessionGoal() {
        val sessionGoal = getString(coreR.string.change_record_type_session_goal_time).lowercase()
        val currentTime = Calendar.getInstance().timeInMillis

        val sessionGoalNotFinished = "sessionGoalNotFinished"
        val sessionGoalFinished = "sessionGoalFinished"

        // Add data
        testUtils.addActivity(
            sessionGoalNotFinished,
            goals = listOf(GoalsTestUtils.getSessionDurationGoal(durationInSeconds)),
        )
        testUtils.addRecord(sessionGoalNotFinished)
        testUtils.addRunningRecord(sessionGoalNotFinished)

        testUtils.addActivity(
            sessionGoalFinished,
            goals = listOf(GoalsTestUtils.getSessionDurationGoal(durationInSeconds)),
        )
        testUtils.addRunningRecord(
            typeName = sessionGoalFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
        )

        // Session goal not finished
        scrollTo(sessionGoalNotFinished)
        checkRunningGoal(sessionGoalNotFinished, "$sessionGoal 9$minuteString")
        checkRunningMark(sessionGoalNotFinished, isVisible = false)

        // Session goal finished
        scrollTo(sessionGoalFinished)
        checkRunningGoal(sessionGoalFinished, sessionGoal)
        checkRunningMark(sessionGoalFinished, isVisible = true)
    }

    @Test
    fun dailyGoal() {
        val dailyGoal = getString(coreR.string.change_record_type_daily_goal_time).lowercase()
        val currentTime = Calendar.getInstance().timeInMillis

        val goalTimeNotFinished = "goalTimeNotFinished"
        val goalTimeFinished = "goalTimeFinished"
        val goalCountNotFinished = "goalCountNotFinished"
        val goalCountFinished = "goalCountFinished"

        // Add data
        testUtils.addActivity(
            goalTimeNotFinished,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(durationInSeconds)),
        )
        testUtils.addRecord(
            typeName = goalTimeNotFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(5),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(goalTimeNotFinished)

        testUtils.addActivity(
            goalTimeFinished,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(durationInSeconds)),
        )
        testUtils.addRecord(
            typeName = goalTimeFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(goalTimeFinished)

        testUtils.addActivity(
            goalCountNotFinished,
            goals = listOf(GoalsTestUtils.getDailyCountGoal(5)),
        )
        testUtils.addRecord(goalCountNotFinished)
        testUtils.addRunningRecord(goalCountNotFinished)

        testUtils.addActivity(
            goalCountFinished,
            goals = listOf(GoalsTestUtils.getDailyCountGoal(3)),
        )
        testUtils.addRecord(goalCountFinished)
        testUtils.addRecord(goalCountFinished)
        testUtils.addRunningRecord(goalCountFinished)

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        checkRunningGoal(goalTimeNotFinished, "$dailyGoal 4$minuteString")
        checkRunningMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkRunningGoal(goalTimeFinished, dailyGoal)
        checkRunningMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkRunningGoal(goalCountNotFinished, "$dailyGoal 3")
        checkRunningMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkRunningGoal(goalCountFinished, dailyGoal)
        checkRunningMark(goalCountFinished, isVisible = true)
    }

    @Test
    fun allGoals() {
        val dailyGoal = getString(coreR.string.change_record_type_daily_goal_time).lowercase()
        val currentTime = Calendar.getInstance().timeInMillis

        val allGoalTimesPresent = "allGoalTimesPresent"
        val allGoalCountsPresent = "allGoalCountsPresent"

        // Add data
        testUtils.addActivity(
            name = allGoalTimesPresent,
            goals = listOf(
                GoalsTestUtils.getSessionDurationGoal(durationInSeconds),
                GoalsTestUtils.getDailyDurationGoal(2 * durationInSeconds),
                GoalsTestUtils.getWeeklyDurationGoal(3 * durationInSeconds),
                GoalsTestUtils.getMonthlyDurationGoal(4 * durationInSeconds),
                GoalsTestUtils.getYearlyDurationGoal(5 * durationInSeconds),
            ),
        )
        testUtils.addRecord(
            typeName = allGoalTimesPresent,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(5),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(allGoalTimesPresent)

        testUtils.addActivity(
            name = allGoalCountsPresent,
            goals = listOf(
                GoalsTestUtils.getDailyCountGoal(10),
                GoalsTestUtils.getWeeklyCountGoal(10),
                GoalsTestUtils.getMonthlyCountGoal(10),
                GoalsTestUtils.getYearlyCountGoal(10),
            ),
        )
        testUtils.addRecord(allGoalCountsPresent)
        testUtils.addRecord(allGoalCountsPresent)
        testUtils.addRunningRecord(allGoalCountsPresent)

        // All goal times
        scrollTo(allGoalTimesPresent)
        checkRunningGoal(allGoalTimesPresent, "$dailyGoal 14$minuteString")
        checkRunningMark(allGoalTimesPresent, isVisible = false)

        // All goal counts
        scrollTo(allGoalCountsPresent)
        checkRunningGoal(allGoalCountsPresent, "$dailyGoal 7")
        checkRunningMark(allGoalCountsPresent, isVisible = false)
    }

    @Test
    fun otherGoals() {
        val goalTime = "goalTime"
        val goalCount = "goalCount"

        // Add data
        testUtils.addActivity(
            goalTime,
            goals = listOf(
                GoalsTestUtils.getWeeklyDurationGoal(durationInSeconds),
                GoalsTestUtils.getMonthlyDurationGoal(durationInSeconds),
                GoalsTestUtils.getYearlyDurationGoal(durationInSeconds),
            ),
        )
        testUtils.addRunningRecord(goalTime)

        testUtils.addActivity(
            goalCount,
            goals = listOf(
                GoalsTestUtils.getWeeklyCountGoal(10),
                GoalsTestUtils.getMonthlyCountGoal(10),
                GoalsTestUtils.getYearlyCountGoal(10),
            ),
        )
        testUtils.addRunningRecord(goalCount)

        // Weekly, monthly, and yearly goals are not present
        scrollTo(goalTime)
        checkNoRunningGoal(goalTime)
        checkRunningMark(goalTime, isVisible = false)

        scrollTo(goalCount)
        checkNoRunningGoal(goalCount)
        checkRunningMark(goalCount, isVisible = false)
    }

    @Test
    fun multipleDailyDurationGoalsAdvanceToNextTarget() {
        val typeName = "multipleDailyGoals"
        val daily = getString(coreR.string.change_record_type_daily_goal_time).lowercase()
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()
        val currentTime = System.currentTimeMillis()

        testUtils.addActivity(
            name = typeName,
            // Store the larger target first to verify selection by target, rather than insertion order.
            goals = listOf(
                GoalsTestUtils.getDailyDurationGoal(2 * durationInSeconds),
                GoalsTestUtils.getDailyDurationGoal(durationInSeconds),
            ),
        )
        fun addMinutes(minutes: Long) {
            testUtils.addRecord(
                typeName = typeName,
                timeStarted = currentTime - TimeUnit.MINUTES.toMillis(minutes),
                timeEnded = currentTime,
            )
        }
        addMinutes(5)
        testUtils.addRunningRecord(typeName, timeStarted = currentTime - TimeUnit.SECONDS.toMillis(1))

        scrollTo(typeName)
        checkRunningGoal(typeName, "$goal · $daily 4$minuteString")
        checkRunningMark(typeName, isVisible = false)

        // Reaching ten minutes moves progress to the twenty minute target.
        NavUtils.openRecordsScreen()
        addMinutes(5)
        NavUtils.openRunningRecordsScreen()
        scrollTo(typeName)
        checkRunningGoal(typeName, "$goal · $daily 9$minuteString")
        checkRunningMark(typeName, isVisible = false)

        // The completion mark appears only after both targets are reached.
        NavUtils.openRecordsScreen()
        addMinutes(10)
        NavUtils.openRunningRecordsScreen()
        scrollTo(typeName)
        checkRunningGoal(typeName, "$goal · $daily")
        checkRunningMark(typeName, isVisible = true)
    }

    @Test
    fun dailyGoalAndLimitHaveSeparateRowsAndRespectLimitBoundary() {
        val typeName = "dailyGoalAndLimit"
        val daily = getString(coreR.string.change_record_type_daily_goal_time).lowercase()
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()
        val limit = getString(coreR.string.change_record_type_limit_time_hint).lowercase()

        testUtils.addActivity(
            name = typeName,
            goals = listOf(
                GoalsTestUtils.getDailyCountGoal(2),
                GoalsTestUtils.getDailyCountGoal(3).copy(subtype = RecordTypeGoal.Subtype.Limit),
            ),
        )
        testUtils.addRunningRecord(typeName)

        scrollTo(typeName)
        checkRunningGoal(typeName, "$goal · $daily 1 Record")
        checkRunningMark(typeName, isVisible = false)
        checkRunningGoal(typeName, "$limit · $daily 2 Records", isSecondRow = true)
        checkRunningMark(typeName, isVisible = false, isSecondRow = true)

        NavUtils.openRecordsScreen()
        testUtils.addRecord(typeName)
        NavUtils.openRunningRecordsScreen()
        scrollTo(typeName)
        checkRunningGoal(typeName, "$goal · $daily")
        checkRunningMark(typeName, isVisible = true)
        checkRunningGoal(typeName, "$limit · $daily 1 Record", isSecondRow = true)
        checkRunningMark(typeName, isVisible = false, isSecondRow = true)

        // The running record counts toward the total: two saved records reach the limit of three.
        NavUtils.openRecordsScreen()
        testUtils.addRecord(typeName)
        NavUtils.openRunningRecordsScreen()
        scrollTo(typeName)
        checkRunningGoal(typeName, "$limit · $daily 0 Records", isSecondRow = true)
        checkRunningMark(typeName, isVisible = false, isSecondRow = true)
        checkRunningMark(typeName, isVisible = true)

        // Only exceeding the limit shows its cross; the goal remains complete.
        NavUtils.openRecordsScreen()
        testUtils.addRecord(typeName)
        NavUtils.openRunningRecordsScreen()
        scrollTo(typeName)
        checkRunningGoal(typeName, "$goal · $daily")
        checkRunningMark(typeName, isVisible = true)
        checkRunningGoal(typeName, "$limit · $daily", isSecondRow = true)
        checkRunningLimitMark(typeName)
    }

    private fun scrollTo(typeName: String) {
        tryAction {
            scrollRecyclerToView(
                runningRecordsR.id.rvRunningRecordsList,
                allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(typeName))),
            )
        }
    }
}
