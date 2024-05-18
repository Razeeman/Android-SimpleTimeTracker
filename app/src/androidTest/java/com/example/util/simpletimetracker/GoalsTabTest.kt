package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.GoalsTestUtils.addRecords
import com.example.util.simpletimetracker.GoalsTestUtils.checkNoStatisticsGoal
import com.example.util.simpletimetracker.GoalsTestUtils.checkStatisticsGoal
import com.example.util.simpletimetracker.GoalsTestUtils.checkStatisticsMark
import com.example.util.simpletimetracker.GoalsTestUtils.checkStatisticsPercent
import com.example.util.simpletimetracker.GoalsTestUtils.durationInSeconds
import com.example.util.simpletimetracker.GoalsTestUtils.getDailyCountGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getDailyDurationGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getMonthlyCountGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getMonthlyDurationGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getSessionDurationGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getWeeklyCountGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getWeeklyDurationGoal
import com.example.util.simpletimetracker.domain.extension.value
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.selectTabAtPosition
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_goals.R as goalsR
import com.example.util.simpletimetracker.feature_main.R as mainR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GoalsTabTest : BaseUiTest() {

    override fun setUp() {
        super.setUp()
        runBlocking { prefsInteractor.setShowGoalsSeparately(true) }
    }

    @Test
    fun goalsAdded() {
        val type = "type"

        val goals = listOf(
            listOf(getSessionDurationGoal(durationInSeconds)),
            listOf(getDailyDurationGoal(durationInSeconds)),
            listOf(getDailyCountGoal(10)),
            listOf(getWeeklyDurationGoal(durationInSeconds)),
            listOf(getWeeklyCountGoal(10)),
            listOf(getMonthlyDurationGoal(durationInSeconds)),
            listOf(getMonthlyCountGoal(10)),
            listOf(
                getSessionDurationGoal(durationInSeconds),
                getDailyCountGoal(10),
                getWeeklyDurationGoal(2 * durationInSeconds),
                getMonthlyCountGoal(20),
            ),
        )

        fun checkGoal(goal: RecordTypeGoal) {
            val layout = when (goal.range) {
                is RecordTypeGoal.Range.Session -> changeRecordTypeR.id.layoutChangeRecordTypeGoalSession
                is RecordTypeGoal.Range.Daily -> changeRecordTypeR.id.layoutChangeRecordTypeGoalDaily
                is RecordTypeGoal.Range.Weekly -> changeRecordTypeR.id.layoutChangeRecordTypeGoalWeekly
                is RecordTypeGoal.Range.Monthly -> changeRecordTypeR.id.layoutChangeRecordTypeGoalMonthly
            }
            val field = when (goal.type) {
                is RecordTypeGoal.Type.Duration -> changeRecordTypeR.id.tvChangeRecordTypeGoalDurationValue
                is RecordTypeGoal.Type.Count -> changeRecordTypeR.id.etChangeRecordTypeGoalCountValue
            }
            val value = when (goal.type) {
                is RecordTypeGoal.Type.Duration -> timeMapper.formatDuration(goal.value)
                is RecordTypeGoal.Type.Count -> goal.value.toString()
            }

            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(layout)),
                    withId(field),
                    withText(value),
                ),
            )
        }

        testUtils.addActivity(type)
        Thread.sleep(1000)

        // Check
        goals.forEach { goalsToAdd ->
            // Add goals
            tryAction { longClickOnView(withText(type)) }
            clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)
            goalsToAdd.forEach { goal ->
                NavUtils.addGoalToActivity(goal)
                checkGoal(goal)
            }
            clickOnViewWithText(coreR.string.change_record_type_save)

            // Check goals saved
            longClickOnView(withText(type))
            clickOnViewWithText(coreR.string.change_record_type_goal_time_hint)
            goalsToAdd.forEach { goal ->
                checkGoal(goal)
                NavUtils.disableGoalOnActivity(goal)
            }
            clickOnViewWithText(coreR.string.change_record_type_save)
        }
    }

    @Test
    fun noGoals() {
        val noGoals = "noGoals"

        // Add data
        testUtils.addActivity(noGoals)
        NavUtils.openGoalsScreen()

        // No goals
        checkViewIsDisplayed(allOf(withText(R.string.no_data), isCompletelyDisplayed()))
    }

    @Test
    fun noGoalsInStatistics() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val someGoals = "someGoals"

        // Add data
        testUtils.addActivity(
            someGoals,
            goals = listOf(
                getDailyDurationGoal(durationInSeconds),
            ),
        )

        // Goals on separate tab
        NavUtils.openGoalsScreen()
        tryAction { checkStatisticsGoal(someGoals, "0$minuteString", "$goal - 10$minuteString") }

        // No goals in statistics
        onView(withId(mainR.id.mainTabs)).perform(selectTabAtPosition(3))
        Thread.sleep(1000)
        checkNoStatisticsGoal(someGoals)
    }

    @Test
    fun allGoals() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val goalDailyTimeNotFinished = "goalDailyTimeNotFinished"
        val goalDailyTimeFinished = "goalDailyTimeFinished"
        val goalDailyCountNotFinished = "goalDailyCountNotFinished"
        val goalDailyCountFinished = "goalDailyCountFinished"
        val goalWeeklyTimeNotFinished = "goalWeeklyTimeNotFinished"
        val goalWeeklyTimeFinished = "goalWeeklyTimeFinished"
        val goalWeeklyCountNotFinished = "goalWeeklyCountNotFinished"
        val goalWeeklyCountFinished = "goalWeeklyCountFinished"
        val goalMonthlyTimeNotFinished = "goalMonthlyTimeNotFinished"
        val goalMonthlyTimeFinished = "goalMonthlyTimeFinished"
        val goalMonthlyCountNotFinished = "goalMonthlyCountNotFinished"
        val goalMonthlyCountFinished = "goalMonthlyCountFinished"

        // Add data
        // Daily
        testUtils.addActivity(
            goalDailyTimeNotFinished,
            goals = listOf(getDailyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalDailyTimeNotFinished)

        testUtils.addActivity(
            goalDailyTimeFinished,
            goals = listOf(getDailyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalDailyTimeFinished)

        testUtils.addActivity(
            goalDailyCountNotFinished,
            goals = listOf(getDailyCountGoal(4)),
        )
        addRecords(testUtils, goalDailyCountNotFinished)

        testUtils.addActivity(
            goalDailyCountFinished,
            goals = listOf(getDailyCountGoal(3)),
        )
        testUtils.addRecord(goalDailyCountFinished)
        testUtils.addRecord(goalDailyCountFinished)
        addRecords(testUtils, goalDailyCountFinished)

        // Weekly
        testUtils.addActivity(
            goalWeeklyTimeNotFinished,
            goals = listOf(getWeeklyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalWeeklyTimeNotFinished)

        testUtils.addActivity(
            goalWeeklyTimeFinished,
            goals = listOf(getWeeklyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalWeeklyTimeFinished)

        testUtils.addActivity(
            goalWeeklyCountNotFinished,
            goals = listOf(getWeeklyCountGoal(4)),
        )
        addRecords(testUtils, goalWeeklyCountNotFinished)

        testUtils.addActivity(
            goalWeeklyCountFinished,
            goals = listOf(getWeeklyCountGoal(3)),
        )
        testUtils.addRecord(goalWeeklyCountFinished)
        testUtils.addRecord(goalWeeklyCountFinished)
        addRecords(testUtils, goalWeeklyCountFinished)

        // Monthly
        testUtils.addActivity(
            goalMonthlyTimeNotFinished,
            goals = listOf(getMonthlyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalMonthlyTimeNotFinished)

        testUtils.addActivity(
            goalMonthlyTimeFinished,
            goals = listOf(getMonthlyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalMonthlyTimeFinished)

        testUtils.addActivity(
            goalMonthlyCountNotFinished,
            goals = listOf(getMonthlyCountGoal(4)),
        )
        addRecords(testUtils, goalMonthlyCountNotFinished)

        testUtils.addActivity(
            goalMonthlyCountFinished,
            goals = listOf(getMonthlyCountGoal(3)),
        )
        testUtils.addRecord(goalMonthlyCountFinished)
        testUtils.addRecord(goalMonthlyCountFinished)
        addRecords(testUtils, goalMonthlyCountFinished)

        // Open tab
        NavUtils.openGoalsScreen()

        // Daily
        // Goal time not finished
        scrollTo(goalDailyTimeNotFinished)
        checkStatisticsGoal(goalDailyTimeNotFinished, "10$minuteString", "$goal - 40$minuteString")
        checkStatisticsPercent(goalDailyTimeNotFinished, "25%")
        checkStatisticsMark(goalDailyTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalDailyTimeFinished)
        checkStatisticsGoal(goalDailyTimeFinished, "10$minuteString", "$goal - 10$minuteString")
        checkStatisticsMark(goalDailyTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalDailyCountNotFinished)
        checkStatisticsGoal(goalDailyCountNotFinished, "1 Record", "$goal - 4 Records")
        checkStatisticsPercent(goalDailyCountNotFinished, "25%")
        checkStatisticsMark(goalDailyCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalDailyCountFinished)
        checkStatisticsGoal(goalDailyCountFinished, "3 Records", "$goal - 3 Records")
        checkStatisticsMark(goalDailyCountFinished, isVisible = true)

        // Weekly
        // Goal time not finished
        scrollTo(goalWeeklyTimeNotFinished)
        checkStatisticsGoal(goalWeeklyTimeNotFinished, "20$minuteString", "$goal - 40$minuteString")
        checkStatisticsPercent(goalWeeklyTimeNotFinished, "50%")
        checkStatisticsMark(goalWeeklyTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalWeeklyTimeFinished)
        checkStatisticsGoal(goalWeeklyTimeFinished, "20$minuteString", "$goal - 10$minuteString")
        checkStatisticsMark(goalWeeklyTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalWeeklyCountNotFinished)
        checkStatisticsGoal(goalWeeklyCountNotFinished, "2 Records", "$goal - 4 Records")
        checkStatisticsPercent(goalWeeklyCountNotFinished, "50%")
        checkStatisticsMark(goalWeeklyCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalWeeklyCountFinished)
        checkStatisticsGoal(goalWeeklyCountFinished, "4 Records", "$goal - 3 Records")
        checkStatisticsMark(goalWeeklyCountFinished, isVisible = true)

        // Monthly
        // Goal time not finished
        scrollTo(goalMonthlyTimeNotFinished)
        checkStatisticsGoal(goalMonthlyTimeNotFinished, "30$minuteString", "$goal - 40$minuteString")
        checkStatisticsPercent(goalMonthlyTimeNotFinished, "75%")
        checkStatisticsMark(goalMonthlyTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalMonthlyTimeFinished)
        checkStatisticsGoal(goalMonthlyTimeFinished, "30$minuteString", "$goal - 10$minuteString")
        checkStatisticsMark(goalMonthlyTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalMonthlyCountNotFinished)
        checkStatisticsGoal(goalMonthlyCountNotFinished, "3 Records", "$goal - 4 Records")
        checkStatisticsPercent(goalMonthlyCountNotFinished, "75%")
        checkStatisticsMark(goalMonthlyCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalMonthlyCountFinished)
        checkStatisticsGoal(goalMonthlyCountFinished, "5 Records", "$goal - 3 Records")
        checkStatisticsMark(goalMonthlyCountFinished, isVisible = true)
    }

    private fun scrollTo(
        typeName: String,
        additionalMatcher: Matcher<View>? = null,
    ) {
        val matchers = listOfNotNull(
            withId(baseR.id.viewStatisticsGoalItem),
            hasDescendant(withText(typeName)),
            additionalMatcher,
        )

        tryAction { scrollRecyclerToView(goalsR.id.rvGoalsList, allOf(matchers)) }
    }
}
