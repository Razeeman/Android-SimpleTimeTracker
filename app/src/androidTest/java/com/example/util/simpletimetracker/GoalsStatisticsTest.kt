package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
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
import com.example.util.simpletimetracker.GoalsTestUtils.getDailyCountGoalCategory
import com.example.util.simpletimetracker.GoalsTestUtils.getDailyDurationGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getDailyDurationGoalCategory
import com.example.util.simpletimetracker.GoalsTestUtils.getMonthlyCountGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getMonthlyCountGoalCategory
import com.example.util.simpletimetracker.GoalsTestUtils.getMonthlyDurationGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getMonthlyDurationGoalCategory
import com.example.util.simpletimetracker.GoalsTestUtils.getWeeklyCountGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getWeeklyCountGoalCategory
import com.example.util.simpletimetracker.GoalsTestUtils.getWeeklyDurationGoal
import com.example.util.simpletimetracker.GoalsTestUtils.getWeeklyDurationGoalCategory
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
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
import com.example.util.simpletimetracker.feature_statistics_detail.R as featureStatisticsDetailR

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
            goals = listOf(getDailyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalTimeNotFinished)

        testUtils.addActivity(
            goalTimeFinished,
            goals = listOf(getDailyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalTimeFinished)

        testUtils.addActivity(
            goalCountNotFinished,
            goals = listOf(getDailyCountGoal(4)),
        )
        addRecords(testUtils, goalCountNotFinished)

        testUtils.addActivity(
            goalCountFinished,
            goals = listOf(getDailyCountGoal(3)),
        )
        testUtils.addRecord(goalCountFinished)
        testUtils.addRecord(goalCountFinished)
        addRecords(testUtils, goalCountFinished)

        testUtils.addActivity(
            otherGoals,
            goals = listOf(
                getWeeklyDurationGoal(durationInSeconds),
                getMonthlyCountGoal(1),
            ),
        )
        addRecords(testUtils, otherGoals)

        NavUtils.openStatisticsScreen()

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        checkStatisticsGoal(goalTimeNotFinished, "10$minuteString", "$goal - 40$minuteString")
        checkStatisticsPercent(goalTimeNotFinished, "25%")
        checkStatisticsMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkStatisticsGoal(goalTimeFinished, "10$minuteString", "$goal - 10$minuteString")
        checkStatisticsMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkStatisticsGoal(goalCountNotFinished, "1 Record", "$goal - 4 Records")
        checkStatisticsPercent(goalCountNotFinished, "25%")
        checkStatisticsMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkStatisticsGoal(goalCountFinished, "3 Records", "$goal - 3 Records")
        checkStatisticsMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        checkNoStatisticsGoal(otherGoals)
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
            goals = listOf(getWeeklyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalTimeNotFinished)

        testUtils.addActivity(
            goalTimeFinished,
            goals = listOf(getWeeklyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalTimeFinished)

        testUtils.addActivity(
            goalCountNotFinished,
            goals = listOf(getWeeklyCountGoal(4)),
        )
        addRecords(testUtils, goalCountNotFinished)

        testUtils.addActivity(
            goalCountFinished,
            goals = listOf(getWeeklyCountGoal(3)),
        )
        testUtils.addRecord(goalCountFinished)
        testUtils.addRecord(goalCountFinished)
        addRecords(testUtils, goalCountFinished)

        testUtils.addActivity(
            otherGoals,
            goals = listOf(
                getDailyDurationGoal(durationInSeconds),
                getMonthlyCountGoal(1),
            ),
        )
        addRecords(testUtils, otherGoals)

        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        checkStatisticsGoal(goalTimeNotFinished, "20$minuteString", "$goal - 40$minuteString")
        checkStatisticsPercent(goalTimeNotFinished, "50%")
        checkStatisticsMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkStatisticsGoal(goalTimeFinished, "20$minuteString", "$goal - 10$minuteString")
        checkStatisticsMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkStatisticsGoal(goalCountNotFinished, "2 Records", "$goal - 4 Records")
        checkStatisticsPercent(goalCountNotFinished, "50%")
        checkStatisticsMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkStatisticsGoal(goalCountFinished, "4 Records", "$goal - 3 Records")
        checkStatisticsMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        checkNoStatisticsGoal(otherGoals)
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
            goals = listOf(getMonthlyDurationGoal(4 * durationInSeconds)),
        )
        addRecords(testUtils, goalTimeNotFinished)

        testUtils.addActivity(
            goalTimeFinished,
            goals = listOf(getMonthlyDurationGoal(durationInSeconds)),
        )
        addRecords(testUtils, goalTimeFinished)

        testUtils.addActivity(
            goalCountNotFinished,
            goals = listOf(getMonthlyCountGoal(4)),
        )
        addRecords(testUtils, goalCountNotFinished)

        testUtils.addActivity(
            goalCountFinished,
            goals = listOf(getMonthlyCountGoal(3)),
        )
        testUtils.addRecord(goalCountFinished)
        testUtils.addRecord(goalCountFinished)
        addRecords(testUtils, goalCountFinished)

        testUtils.addActivity(
            otherGoals,
            goals = listOf(
                getDailyDurationGoal(durationInSeconds),
                getWeeklyCountGoal(1),
            ),
        )
        addRecords(testUtils, otherGoals)

        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_month)

        // Goal time not finished
        scrollTo(goalTimeNotFinished)
        checkStatisticsGoal(goalTimeNotFinished, "30$minuteString", "$goal - 40$minuteString")
        checkStatisticsPercent(goalTimeNotFinished, "75%")
        checkStatisticsMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkStatisticsGoal(goalTimeFinished, "30$minuteString", "$goal - 10$minuteString")
        checkStatisticsMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkStatisticsGoal(goalCountNotFinished, "3 Records", "$goal - 4 Records")
        checkStatisticsPercent(goalCountNotFinished, "75%")
        checkStatisticsMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkStatisticsGoal(goalCountFinished, "5 Records", "$goal - 3 Records")
        checkStatisticsMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        checkNoStatisticsGoal(otherGoals)
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
            goals = listOf(getDailyDurationGoalCategory(8 * durationInSeconds)),
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
            goals = listOf(getDailyDurationGoalCategory(2 * durationInSeconds)),
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
            goals = listOf(getDailyCountGoalCategory(8)),
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
            goals = listOf(getDailyCountGoalCategory(3)),
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
                getWeeklyDurationGoalCategory(durationInSeconds),
                getMonthlyCountGoalCategory(1),
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
        checkStatisticsGoal(goalTimeNotFinished, "20$minuteString", "$goal - 1$hourString 20$minuteString")
        checkStatisticsPercent(goalTimeNotFinished, "25%")
        checkStatisticsMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkStatisticsGoal(goalTimeFinished, "20$minuteString", "$goal - 20$minuteString")
        checkStatisticsMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkStatisticsGoal(goalCountNotFinished, "2 Records", "$goal - 8 Records")
        checkStatisticsPercent(goalCountNotFinished, "25%")
        checkStatisticsMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkStatisticsGoal(goalCountFinished, "3 Records", "$goal - 3 Records")
        checkStatisticsMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        checkNoStatisticsGoal(otherGoals)
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
            goals = listOf(getWeeklyDurationGoalCategory(8 * durationInSeconds)),
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
            goals = listOf(getWeeklyDurationGoalCategory(2 * durationInSeconds)),
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
            goals = listOf(getWeeklyCountGoalCategory(8)),
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
            goals = listOf(getWeeklyCountGoalCategory(3)),
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
                getDailyDurationGoalCategory(durationInSeconds),
                getMonthlyCountGoalCategory(1),
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
        checkStatisticsGoal(goalTimeNotFinished, "40$minuteString", "$goal - 1$hourString 20$minuteString")
        checkStatisticsPercent(goalTimeNotFinished, "50%")
        checkStatisticsMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkStatisticsGoal(goalTimeFinished, "40$minuteString", "$goal - 20$minuteString")
        checkStatisticsMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkStatisticsGoal(goalCountNotFinished, "4 Records", "$goal - 8 Records")
        checkStatisticsPercent(goalCountNotFinished, "50%")
        checkStatisticsMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkStatisticsGoal(goalCountFinished, "4 Records", "$goal - 3 Records")
        checkStatisticsMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        checkNoStatisticsGoal(otherGoals)
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
            goals = listOf(getMonthlyDurationGoalCategory(8 * durationInSeconds)),
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
            goals = listOf(getMonthlyDurationGoalCategory(2 * durationInSeconds)),
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
            goals = listOf(getMonthlyCountGoalCategory(8)),
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
            goals = listOf(getMonthlyCountGoalCategory(3)),
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
                getDailyDurationGoalCategory(durationInSeconds),
                getWeeklyCountGoalCategory(1),
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
        checkStatisticsGoal(goalTimeNotFinished, "1$hourString 0$minuteString", "$goal - 1$hourString 20$minuteString")
        checkStatisticsPercent(goalTimeNotFinished, "75%")
        checkStatisticsMark(goalTimeNotFinished, isVisible = false)

        // Goal time finished
        scrollTo(goalTimeFinished)
        checkStatisticsGoal(goalTimeFinished, "1$hourString 0$minuteString", "$goal - 20$minuteString")
        checkStatisticsMark(goalTimeFinished, isVisible = true)

        // Goal count not finished
        scrollTo(goalCountNotFinished)
        checkStatisticsGoal(goalCountNotFinished, "6 Records", "$goal - 8 Records")
        checkStatisticsPercent(goalCountNotFinished, "75%")
        checkStatisticsMark(goalCountNotFinished, isVisible = false)

        // Goal count finished
        scrollTo(goalCountFinished)
        checkStatisticsGoal(goalCountFinished, "5 Records", "$goal - 3 Records")
        checkStatisticsMark(goalCountFinished, isVisible = true)

        // Other goals
        scrollBottom()
        checkNoStatisticsGoal(otherGoals)
    }

    @Test
    fun goalNavigation() {
        val typeName = "typeName"

        // Add data
        testUtils.addActivity(
            name = typeName,
            goals = listOf(getDailyDurationGoal(durationInSeconds)),
        )
        testUtils.addRecord(typeName)

        // Check
        NavUtils.openStatisticsScreen()
        scrollTo(typeName)
        clickOnView(
            allOf(
                withId(baseR.id.viewStatisticsGoalItem),
                hasDescendant(withText(typeName)),
                isCompletelyDisplayed(),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(featureStatisticsDetailR.id.viewStatisticsDetailItem),
                hasDescendant(withText(typeName)),
            ),
        )
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
