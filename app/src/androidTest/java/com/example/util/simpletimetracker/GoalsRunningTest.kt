package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.GoalsTestUtils.durationInSeconds
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
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
        checkNoGoal(noGoals)
        checkGoalMark(noGoals, isVisible = false)
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
        checkGoal(sessionGoalNotFinished, "$sessionGoal 9$minuteString")
        checkGoalMark(sessionGoalNotFinished, isVisible = false)

        // Session goal finished
        scrollTo(sessionGoalFinished)
        checkGoal(sessionGoalFinished, sessionGoal)
        checkGoalMark(sessionGoalFinished, isVisible = true)
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
        checkGoal(goalTimeNotFinished, "$dailyGoal 4$minuteString")
        checkGoalMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkGoal(goalTimeFinished, dailyGoal)
        checkGoalMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkGoal(goalCountNotFinished, "$dailyGoal 3")
        checkGoalMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkGoal(goalCountFinished, dailyGoal)
        checkGoalMark(goalCountFinished, isVisible = true)
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
            ),
        )
        testUtils.addRecord(allGoalCountsPresent)
        testUtils.addRecord(allGoalCountsPresent)
        testUtils.addRunningRecord(allGoalCountsPresent)

        // All goal times
        scrollTo(allGoalTimesPresent)
        checkGoal(allGoalTimesPresent, "$dailyGoal 14$minuteString")
        checkGoalMark(allGoalTimesPresent, isVisible = false)

        // All goal counts
        scrollTo(allGoalCountsPresent)
        checkGoal(allGoalCountsPresent, "$dailyGoal 7")
        checkGoalMark(allGoalCountsPresent, isVisible = false)
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
            ),
        )
        testUtils.addRunningRecord(goalTime)

        testUtils.addActivity(
            goalCount,
            goals = listOf(
                GoalsTestUtils.getWeeklyCountGoal(10),
                GoalsTestUtils.getMonthlyCountGoal(10),
            ),
        )
        testUtils.addRunningRecord(goalCount)

        // Weekly and monthly goals are not present
        scrollTo(goalTime)
        checkNoGoal(goalTime)
        checkGoalMark(goalTime, isVisible = false)

        scrollTo(goalCount)
        checkNoGoal(goalCount)
        checkGoalMark(goalCount, isVisible = false)
    }

    private fun checkGoal(typeName: String, goal: String) {
        allOf(
            isDescendantOfA(withId(R.id.viewRunningRecordItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvRunningRecordItemGoalTime),
            withSubstring(goal),
        ).let(::checkViewIsDisplayed)
    }

    private fun checkNoGoal(typeName: String) {
        allOf(
            isDescendantOfA(withId(R.id.viewRunningRecordItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvRunningRecordItemGoalTime),
        ).let(::checkViewIsNotDisplayed)
    }

    private fun checkGoalMark(typeName: String, isVisible: Boolean) {
        allOf(
            isDescendantOfA(withId(R.id.viewRunningRecordItem)),
            hasSibling(withText(typeName)),
            withId(R.id.ivRunningRecordItemGoalTimeCheck),
        ).let {
            if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it)
        }
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
