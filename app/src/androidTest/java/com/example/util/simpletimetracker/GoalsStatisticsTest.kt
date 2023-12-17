package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.GoalsTestUtils.addRecords
import com.example.util.simpletimetracker.GoalsTestUtils.durationInSeconds
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.scrollRecyclerInPagerToView
import com.example.util.simpletimetracker.utils.scrollToBottom
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GoalsStatisticsTest : BaseUiTest() {

    @Test
    fun dailyGoal() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val goalTimeNotFinished = "goalTimeNotFinished"
        val goalTimeFinished = "goalTimeFinished"
        val goalCountNotFinished = "goalCountNotFinished"
        val goalCountFinished = "goalCountFinished"
        val otherGoals = "otherDailyGoals"

        // Add data
        testUtils.addActivity(
            goalTimeNotFinished,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalTimeNotFinished)

        testUtils.addActivity(
            goalTimeFinished,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalTimeFinished)

        testUtils.addActivity(
            goalCountNotFinished,
            goals = listOf(GoalsTestUtils.getDailyCountGoal(4)),
        )
        addRecords(testUtils, goalCountNotFinished)

        testUtils.addActivity(
            goalCountFinished,
            goals = listOf(GoalsTestUtils.getDailyCountGoal(3)),
        )
        testUtils.addRecord(goalCountFinished)
        testUtils.addRecord(goalCountFinished)
        addRecords(testUtils, goalCountFinished)

        testUtils.addActivity(
            otherGoals,
            goals = listOf(
                GoalsTestUtils.getWeeklyDurationGoal(durationInSeconds),
                GoalsTestUtils.getMonthlyCountGoal(1),
            ),
        )
        addRecords(testUtils, otherGoals)

        NavUtils.openStatisticsScreen()

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        GoalsTestUtils.checkGoal(goalTimeNotFinished, "10$minuteString", "$goal - 40$minuteString")
        GoalsTestUtils.checkGoalPercent(goalTimeNotFinished, "25%")
        GoalsTestUtils.checkGoalMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        GoalsTestUtils.checkGoal(goalTimeFinished, "10$minuteString", "$goal - 10$minuteString")
        GoalsTestUtils.checkGoalMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        GoalsTestUtils.checkGoal(goalCountNotFinished, "1 Record", "$goal - 4 Records")
        GoalsTestUtils.checkGoalPercent(goalCountNotFinished, "25%")
        GoalsTestUtils.checkGoalMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        GoalsTestUtils.checkGoal(goalCountFinished, "3 Records", "$goal - 3 Records")
        GoalsTestUtils.checkGoalMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        GoalsTestUtils.checkNoGoal(otherGoals)
    }

    @Test
    fun weeklyGoal() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val goalTimeNotFinished = "goalTimeNotFinished"
        val goalTimeFinished = "goalTimeFinished"
        val goalCountNotFinished = "goalCountNotFinished"
        val goalCountFinished = "goalCountFinished"
        val otherGoals = "otherWeeklyGoals"

        // Add data
        testUtils.addActivity(
            goalTimeNotFinished,
            goals = listOf(GoalsTestUtils.getWeeklyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalTimeNotFinished)

        testUtils.addActivity(
            goalTimeFinished,
            goals = listOf(GoalsTestUtils.getWeeklyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalTimeFinished)

        testUtils.addActivity(
            goalCountNotFinished,
            goals = listOf(GoalsTestUtils.getWeeklyCountGoal(4)),
        )
        addRecords(testUtils, goalCountNotFinished)

        testUtils.addActivity(
            goalCountFinished,
            goals = listOf(GoalsTestUtils.getWeeklyCountGoal(3)),
        )
        testUtils.addRecord(goalCountFinished)
        testUtils.addRecord(goalCountFinished)
        addRecords(testUtils, goalCountFinished)

        testUtils.addActivity(
            otherGoals,
            goals = listOf(
                GoalsTestUtils.getDailyDurationGoal(durationInSeconds),
                GoalsTestUtils.getMonthlyCountGoal(1),
            ),
        )
        addRecords(testUtils, otherGoals)

        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        GoalsTestUtils.checkGoal(goalTimeNotFinished, "20$minuteString", "$goal - 40$minuteString")
        GoalsTestUtils.checkGoalPercent(goalTimeNotFinished, "50%")
        GoalsTestUtils.checkGoalMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        GoalsTestUtils.checkGoal(goalTimeFinished, "20$minuteString", "$goal - 10$minuteString")
        GoalsTestUtils.checkGoalMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        GoalsTestUtils.checkGoal(goalCountNotFinished, "2 Records", "$goal - 4 Records")
        GoalsTestUtils.checkGoalPercent(goalCountNotFinished, "50%")
        GoalsTestUtils.checkGoalMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        GoalsTestUtils.checkGoal(goalCountFinished, "4 Records", "$goal - 3 Records")
        GoalsTestUtils.checkGoalMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        GoalsTestUtils.checkNoGoal(otherGoals)
    }

    @Test
    fun monthlyGoal() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val goalTimeNotFinished = "goalTimeNotFinished"
        val goalTimeFinished = "goalTimeFinished"
        val goalCountNotFinished = "goalCountNotFinished"
        val goalCountFinished = "goalCountFinished"
        val otherGoals = "otherMonthlyGoals"

        // Add data
        testUtils.addActivity(
            goalTimeNotFinished,
            goals = listOf(GoalsTestUtils.getMonthlyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalTimeNotFinished)

        testUtils.addActivity(
            goalTimeFinished,
            goals = listOf(GoalsTestUtils.getMonthlyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalTimeFinished)

        testUtils.addActivity(
            goalCountNotFinished,
            goals = listOf(GoalsTestUtils.getMonthlyCountGoal(4)),
        )
        addRecords(testUtils, goalCountNotFinished)

        testUtils.addActivity(
            goalCountFinished,
            goals = listOf(GoalsTestUtils.getMonthlyCountGoal(3)),
        )
        testUtils.addRecord(goalCountFinished)
        testUtils.addRecord(goalCountFinished)
        addRecords(testUtils, goalCountFinished)

        testUtils.addActivity(
            otherGoals,
            goals = listOf(
                GoalsTestUtils.getDailyDurationGoal(durationInSeconds),
                GoalsTestUtils.getWeeklyCountGoal(1),
            ),
        )
        addRecords(testUtils, otherGoals)

        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_month)

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        GoalsTestUtils.checkGoal(goalTimeNotFinished, "30$minuteString", "$goal - 40$minuteString")
        GoalsTestUtils.checkGoalPercent(goalTimeNotFinished, "75%")
        GoalsTestUtils.checkGoalMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        GoalsTestUtils.checkGoal(goalTimeFinished, "30$minuteString", "$goal - 10$minuteString")
        GoalsTestUtils.checkGoalMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        GoalsTestUtils.checkGoal(goalCountNotFinished, "3 Records", "$goal - 4 Records")
        GoalsTestUtils.checkGoalPercent(goalCountNotFinished, "75%")
        GoalsTestUtils.checkGoalMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        GoalsTestUtils.checkGoal(goalCountFinished, "5 Records", "$goal - 3 Records")
        GoalsTestUtils.checkGoalMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        GoalsTestUtils.checkNoGoal(otherGoals)
    }

    @Test
    fun dailyCategoryGoal() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val goalTimeNotFinished = "goalTimeNotFinished"
        val goalTimeFinished = "goalTimeFinished"
        val goalCountNotFinished = "goalCountNotFinished"
        val goalCountFinished = "goalCountFinished"
        val otherGoals = "otherDailyGoals"

        // Add data
        testUtils.addCategory(
            goalTimeNotFinished,
            goals = listOf(GoalsTestUtils.getDailyDurationGoalCategory(8 * durationInSeconds)),
        )
        testUtils.addActivity(
            goalTimeNotFinished.first(),
            categories = listOf(goalTimeNotFinished),
        )
        testUtils.addActivity(
            goalTimeNotFinished.second(),
            categories = listOf(goalTimeNotFinished),
        )
        addRecords(testUtils, goalTimeNotFinished.first())
        addRecords(testUtils, goalTimeNotFinished.second())

        testUtils.addCategory(
            goalTimeFinished,
            goals = listOf(GoalsTestUtils.getDailyDurationGoalCategory(2 * durationInSeconds)),
        )
        testUtils.addActivity(
            goalTimeFinished.first(),
            categories = listOf(goalTimeFinished),
        )
        testUtils.addActivity(
            goalTimeFinished.second(),
            categories = listOf(goalTimeFinished),
        )
        addRecords(testUtils, goalTimeFinished.first())
        addRecords(testUtils, goalTimeFinished.second())

        testUtils.addCategory(
            goalCountNotFinished,
            goals = listOf(GoalsTestUtils.getDailyCountGoalCategory(8)),
        )
        testUtils.addActivity(
            goalCountNotFinished.first(),
            categories = listOf(goalCountNotFinished),
        )
        testUtils.addActivity(
            goalCountNotFinished.second(),
            categories = listOf(goalCountNotFinished),
        )
        addRecords(testUtils, goalCountNotFinished.first())
        addRecords(testUtils, goalCountNotFinished.second())

        testUtils.addCategory(
            goalCountFinished,
            goals = listOf(GoalsTestUtils.getDailyCountGoalCategory(3)),
        )
        testUtils.addActivity(
            goalCountFinished.first(),
            categories = listOf(goalCountFinished),
        )
        testUtils.addActivity(
            goalCountFinished.second(),
            categories = listOf(goalCountFinished),
        )
        testUtils.addRecord(goalCountFinished.first())
        testUtils.addRecord(goalCountFinished.second())
        addRecords(testUtils, goalCountFinished.first())

        testUtils.addCategory(
            otherGoals,
            goals = listOf(
                GoalsTestUtils.getWeeklyDurationGoalCategory(durationInSeconds),
                GoalsTestUtils.getMonthlyCountGoalCategory(1),
            ),
        )
        testUtils.addActivity(
            otherGoals.first(),
            categories = listOf(otherGoals),
        )
        testUtils.addActivity(
            otherGoals.second(),
            categories = listOf(otherGoals),
        )
        addRecords(testUtils, otherGoals.first())
        addRecords(testUtils, otherGoals.second())

        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        GoalsTestUtils.checkGoal(goalTimeNotFinished, "20$minuteString", "$goal - 1$hourString 20$minuteString")
        GoalsTestUtils.checkGoalPercent(goalTimeNotFinished, "25%")
        GoalsTestUtils.checkGoalMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        GoalsTestUtils.checkGoal(goalTimeFinished, "20$minuteString", "$goal - 20$minuteString")
        GoalsTestUtils.checkGoalMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        GoalsTestUtils.checkGoal(goalCountNotFinished, "2 Records", "$goal - 8 Records")
        GoalsTestUtils.checkGoalPercent(goalCountNotFinished, "25%")
        GoalsTestUtils.checkGoalMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        GoalsTestUtils.checkGoal(goalCountFinished, "3 Records", "$goal - 3 Records")
        GoalsTestUtils.checkGoalMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        GoalsTestUtils.checkNoGoal(otherGoals)
    }

    @Test
    fun weeklyCategoryGoal() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val goalTimeNotFinished = "goalTimeNotFinished"
        val goalTimeFinished = "goalTimeFinished"
        val goalCountNotFinished = "goalCountNotFinished"
        val goalCountFinished = "goalCountFinished"
        val otherGoals = "otherWeeklyGoals"

        // Add data
        testUtils.addCategory(
            goalTimeNotFinished,
            goals = listOf(GoalsTestUtils.getWeeklyDurationGoalCategory(8 * durationInSeconds)),
        )
        testUtils.addActivity(
            goalTimeNotFinished.first(),
            categories = listOf(goalTimeNotFinished),
        )
        testUtils.addActivity(
            goalTimeNotFinished.second(),
            categories = listOf(goalTimeNotFinished),
        )
        addRecords(testUtils, goalTimeNotFinished.first())
        addRecords(testUtils, goalTimeNotFinished.second())

        testUtils.addCategory(
            goalTimeFinished,
            goals = listOf(GoalsTestUtils.getWeeklyDurationGoalCategory(2 * durationInSeconds)),
        )
        testUtils.addActivity(
            goalTimeFinished.first(),
            categories = listOf(goalTimeFinished),
        )
        testUtils.addActivity(
            goalTimeFinished.second(),
            categories = listOf(goalTimeFinished),
        )
        addRecords(testUtils, goalTimeFinished.first())
        addRecords(testUtils, goalTimeFinished.second())

        testUtils.addCategory(
            goalCountNotFinished,
            goals = listOf(GoalsTestUtils.getWeeklyCountGoalCategory(8)),
        )
        testUtils.addActivity(
            goalCountNotFinished.first(),
            categories = listOf(goalCountNotFinished),
        )
        testUtils.addActivity(
            goalCountNotFinished.second(),
            categories = listOf(goalCountNotFinished),
        )
        addRecords(testUtils, goalCountNotFinished.first())
        addRecords(testUtils, goalCountNotFinished.second())

        testUtils.addCategory(
            goalCountFinished,
            goals = listOf(GoalsTestUtils.getWeeklyCountGoalCategory(3)),
        )
        testUtils.addActivity(
            goalCountFinished.first(),
            categories = listOf(goalCountFinished),
        )
        testUtils.addActivity(
            goalCountFinished.second(),
            categories = listOf(goalCountFinished),
        )
        testUtils.addRecord(goalCountFinished.first())
        testUtils.addRecord(goalCountFinished.second())
        addRecords(testUtils, goalCountFinished.first())

        testUtils.addCategory(
            otherGoals,
            goals = listOf(
                GoalsTestUtils.getDailyDurationGoalCategory(durationInSeconds),
                GoalsTestUtils.getMonthlyCountGoalCategory(1),
            ),
        )
        testUtils.addActivity(
            otherGoals.first(),
            categories = listOf(otherGoals),
        )
        testUtils.addActivity(
            otherGoals.second(),
            categories = listOf(otherGoals),
        )
        addRecords(testUtils, otherGoals.first())
        addRecords(testUtils, otherGoals.second())

        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        GoalsTestUtils.checkGoal(goalTimeNotFinished, "40$minuteString", "$goal - 1$hourString 20$minuteString")
        GoalsTestUtils.checkGoalPercent(goalTimeNotFinished, "50%")
        GoalsTestUtils.checkGoalMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        GoalsTestUtils.checkGoal(goalTimeFinished, "40$minuteString", "$goal - 20$minuteString")
        GoalsTestUtils.checkGoalMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        GoalsTestUtils.checkGoal(goalCountNotFinished, "4 Records", "$goal - 8 Records")
        GoalsTestUtils.checkGoalPercent(goalCountNotFinished, "50%")
        GoalsTestUtils.checkGoalMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        GoalsTestUtils.checkGoal(goalCountFinished, "4 Records", "$goal - 3 Records")
        GoalsTestUtils.checkGoalMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        GoalsTestUtils.checkNoGoal(otherGoals)
    }

    @Test
    fun monthlyCategoryGoal() {
        val goal = getString(coreR.string.change_record_type_goal_time_hint).lowercase()

        val goalTimeNotFinished = "goalTimeNotFinished"
        val goalTimeFinished = "goalTimeFinished"
        val goalCountNotFinished = "goalCountNotFinished"
        val goalCountFinished = "goalCountFinished"
        val otherGoals = "otherMonthlyGoals"

        // Add data
        testUtils.addCategory(
            goalTimeNotFinished,
            goals = listOf(GoalsTestUtils.getMonthlyDurationGoalCategory(8 * durationInSeconds)),
        )
        testUtils.addActivity(
            goalTimeNotFinished.first(),
            categories = listOf(goalTimeNotFinished),
        )
        testUtils.addActivity(
            goalTimeNotFinished.second(),
            categories = listOf(goalTimeNotFinished),
        )
        addRecords(testUtils, goalTimeNotFinished.first())
        addRecords(testUtils, goalTimeNotFinished.second())

        testUtils.addCategory(
            goalTimeFinished,
            goals = listOf(GoalsTestUtils.getMonthlyDurationGoalCategory(2 * durationInSeconds)),
        )
        testUtils.addActivity(
            goalTimeFinished.first(),
            categories = listOf(goalTimeFinished),
        )
        testUtils.addActivity(
            goalTimeFinished.second(),
            categories = listOf(goalTimeFinished),
        )
        addRecords(testUtils, goalTimeFinished.first())
        addRecords(testUtils, goalTimeFinished.second())

        testUtils.addCategory(
            goalCountNotFinished,
            goals = listOf(GoalsTestUtils.getMonthlyCountGoalCategory(8)),
        )
        testUtils.addActivity(
            goalCountNotFinished.first(),
            categories = listOf(goalCountNotFinished),
        )
        testUtils.addActivity(
            goalCountNotFinished.second(),
            categories = listOf(goalCountNotFinished),
        )
        addRecords(testUtils, goalCountNotFinished.first())
        addRecords(testUtils, goalCountNotFinished.second())

        testUtils.addCategory(
            goalCountFinished,
            goals = listOf(GoalsTestUtils.getMonthlyCountGoalCategory(3)),
        )
        testUtils.addActivity(
            goalCountFinished.first(),
            categories = listOf(goalCountFinished),
        )
        testUtils.addActivity(
            goalCountFinished.second(),
            categories = listOf(goalCountFinished),
        )
        testUtils.addRecord(goalCountFinished.first())
        testUtils.addRecord(goalCountFinished.second())
        addRecords(testUtils, goalCountFinished.first())

        testUtils.addCategory(
            otherGoals,
            goals = listOf(
                GoalsTestUtils.getDailyDurationGoalCategory(durationInSeconds),
                GoalsTestUtils.getWeeklyCountGoalCategory(1),
            ),
        )
        testUtils.addActivity(
            otherGoals.first(),
            categories = listOf(otherGoals),
        )
        testUtils.addActivity(
            otherGoals.second(),
            categories = listOf(otherGoals),
        )
        addRecords(testUtils, otherGoals.first())
        addRecords(testUtils, otherGoals.second())

        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_month)
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        GoalsTestUtils.checkGoal(goalTimeNotFinished, "1$hourString 0$minuteString", "$goal - 1$hourString 20$minuteString")
        GoalsTestUtils.checkGoalPercent(goalTimeNotFinished, "75%")
        GoalsTestUtils.checkGoalMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        GoalsTestUtils.checkGoal(goalTimeFinished, "1$hourString 0$minuteString", "$goal - 20$minuteString")
        GoalsTestUtils.checkGoalMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        GoalsTestUtils.checkGoal(goalCountNotFinished, "6 Records", "$goal - 8 Records")
        GoalsTestUtils.checkGoalPercent(goalCountNotFinished, "75%")
        GoalsTestUtils.checkGoalMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        GoalsTestUtils.checkGoal(goalCountFinished, "5 Records", "$goal - 3 Records")
        GoalsTestUtils.checkGoalMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        GoalsTestUtils.checkNoGoal(otherGoals)
    }

    private fun scrollTo(typeName: String) {
        tryAction {
            scrollRecyclerInPagerToView(
                statisticsR.id.rvStatisticsList,
                allOf(withId(baseR.id.viewStatisticsGoalItem), hasDescendant(withText(typeName))),
            )
        }
    }

    private fun scrollBottom() {
        onView(allOf(withId(statisticsR.id.rvStatisticsList), isCompletelyDisplayed())).perform(scrollToBottom())
    }

    private fun String.first(): String {
        return this + "1"
    }

    private fun String.second(): String {
        return this + "2"
    }
}
