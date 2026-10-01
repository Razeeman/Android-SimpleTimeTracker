package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.GoalsTestUtils.checkNoRunningGoal
import com.example.util.simpletimetracker.GoalsTestUtils.checkNoStatisticsGoal
import com.example.util.simpletimetracker.GoalsTestUtils.checkNoTypeMark
import com.example.util.simpletimetracker.GoalsTestUtils.checkRunningGoal
import com.example.util.simpletimetracker.GoalsTestUtils.checkStatisticsGoal
import com.example.util.simpletimetracker.GoalsTestUtils.checkTypeMark
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.extension.removeIf
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnPrevDate
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerInPagerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.feature_change_goals.R as changeGoalsR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GoalsDaysOfWeek : BaseUiTest() {

    @Test
    fun daysVisibility() {
        fun checkString(
            range: RecordTypeGoal.Range,
            textMatcher: Matcher<View>,
            visibilityMatcher: (Matcher<View>) -> ViewInteraction,
        ) {
            visibilityMatcher(
                allOf(
                    textMatcher,
                    isDescendantOfA(
                        GoalsTestUtils.goalCard(range, RecordTypeGoal.Type.Duration(1)),
                    ),
                ),
            )
        }

        val name = "Test"

        // Add data
        testUtils.addActivity(
            name = name,
            goals = listOf(
                GoalsTestUtils.getSessionDurationGoal(1),
                GoalsTestUtils.getDailyDurationGoal(1),
                GoalsTestUtils.getWeeklyDurationGoal(1),
                GoalsTestUtils.getMonthlyDurationGoal(1),
            ),
        )
        Thread.sleep(1000)

        // Check
        tryAction { longClickOnView(withText(name)) }
        onView(withText(R.string.change_record_type_goal_time_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_goal_time_hint)

        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Session, RecordTypeGoal.Type.Duration(1))
        checkString(
            RecordTypeGoal.Range.Session,
            withText("1$secondString"),
            ::checkViewIsDisplayed,
        )
        checkString(
            RecordTypeGoal.Range.Session,
            withText(R.string.day_of_week_sunday),
            ::checkViewDoesNotExist,
        )

        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Daily, RecordTypeGoal.Type.Duration(1))
        checkString(
            RecordTypeGoal.Range.Daily,
            withText("1$secondString"),
            ::checkViewIsDisplayed,
        )
        checkString(
            RecordTypeGoal.Range.Daily,
            withText(R.string.day_of_week_sunday),
            ::checkViewIsDisplayed,
        )

        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Weekly, RecordTypeGoal.Type.Duration(1))
        checkString(
            RecordTypeGoal.Range.Weekly,
            withText("1$secondString"),
            ::checkViewIsDisplayed,
        )
        checkString(
            RecordTypeGoal.Range.Weekly,
            withText(R.string.day_of_week_sunday),
            ::checkViewDoesNotExist,
        )

        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Monthly, RecordTypeGoal.Type.Duration(1))
        checkString(
            RecordTypeGoal.Range.Monthly,
            withText("1$secondString"),
            ::checkViewIsDisplayed,
        )
        checkString(
            RecordTypeGoal.Range.Monthly,
            withText(R.string.day_of_week_sunday),
            ::checkViewDoesNotExist,
        )
    }

    @Test
    fun changeDays() {
        val name = "Test"

        // Add data
        testUtils.addActivity(name)
        Thread.sleep(1000)
        tryAction { longClickOnView(withText(name)) }

        // No days by default
        onView(withText(R.string.change_record_type_goal_time_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_goal_time_hint)
        checkViewDoesNotExist(withText(R.string.day_of_week_sunday))
        GoalsTestUtils.addGoal()

        // Set goal
        clickOnView(GoalsTestUtils.visibleGoalField(changeGoalsR.id.fieldChangeRecordTypeGoalDuration))
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)

        checkViewIsDisplayed(withText("1$secondString"))
        daysResIdList.forEach { checkTypeDay(stringResId = it, colorResId = R.color.colorActive) }

        // Disable goal
        clickOnView(GoalsTestUtils.visibleGoalField(changeGoalsR.id.fieldChangeRecordTypeGoalDuration))
        clickOnViewWithText(R.string.duration_dialog_disable)
        checkViewIsNotDisplayed(withId(changeGoalsR.id.rvChangeRecordTypeGoalDays))

        // Change days
        clickOnView(GoalsTestUtils.visibleGoalField(changeGoalsR.id.fieldChangeRecordTypeGoalDuration))
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)

        daysResIdList.forEach {
            clickOnView(withText(it))
            checkTypeDay(stringResId = it, colorResId = R.color.colorInactive)
            clickOnView(withText(it))
            checkTypeDay(stringResId = it, colorResId = R.color.colorActive)
        }

        clickOnView(withText(R.string.day_of_week_monday))
        clickOnView(withText(R.string.day_of_week_wednesday))
        clickOnView(withText(R.string.day_of_week_friday))
        checkTypeDay(stringResId = R.string.day_of_week_sunday, colorResId = R.color.colorActive)
        checkTypeDay(stringResId = R.string.day_of_week_monday, colorResId = R.color.colorInactive)
        checkTypeDay(stringResId = R.string.day_of_week_tuesday, colorResId = R.color.colorActive)
        checkTypeDay(stringResId = R.string.day_of_week_wednesday, colorResId = R.color.colorInactive)
        checkTypeDay(stringResId = R.string.day_of_week_thursday, colorResId = R.color.colorActive)
        checkTypeDay(stringResId = R.string.day_of_week_friday, colorResId = R.color.colorInactive)
        checkTypeDay(stringResId = R.string.day_of_week_saturday, colorResId = R.color.colorActive)
    }

    @Test
    fun daysAreSaved() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add data
        testUtils.addActivity(
            name = name1,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(1)),
        )
        testUtils.addActivity(
            name = name2,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(1)),
        )
        Thread.sleep(1000)

        // Change days
        tryAction { longClickOnView(withText(name1)) }
        onView(withText(R.string.change_record_type_goal_time_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_goal_time_hint)
        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Daily, RecordTypeGoal.Type.Duration(1))
        clickOnView(withText(R.string.day_of_week_monday))
        clickOnView(withText(R.string.day_of_week_wednesday))
        clickOnView(withText(R.string.day_of_week_friday))
        clickOnViewWithText(R.string.change_record_type_save)

        // Days are saved
        tryAction { longClickOnView(withText(name1)) }
        onView(withText(R.string.change_record_type_goal_time_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_goal_time_hint)
        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Daily, RecordTypeGoal.Type.Duration(1))
        checkTypeDay(stringResId = R.string.day_of_week_sunday, colorResId = R.color.colorActive)
        checkTypeDay(stringResId = R.string.day_of_week_monday, colorResId = R.color.colorInactive)
        checkTypeDay(stringResId = R.string.day_of_week_tuesday, colorResId = R.color.colorActive)
        checkTypeDay(stringResId = R.string.day_of_week_wednesday, colorResId = R.color.colorInactive)
        checkTypeDay(stringResId = R.string.day_of_week_thursday, colorResId = R.color.colorActive)
        checkTypeDay(stringResId = R.string.day_of_week_friday, colorResId = R.color.colorInactive)
        checkTypeDay(stringResId = R.string.day_of_week_saturday, colorResId = R.color.colorActive)
        pressBack()
        pressBack()

        tryAction { longClickOnView(withText(name2)) }
        onView(withText(R.string.change_record_type_goal_time_hint)).perform(nestedScrollTo())
        clickOnViewWithText(R.string.change_record_type_goal_time_hint)
        GoalsTestUtils.expandGoal(RecordTypeGoal.Range.Daily, RecordTypeGoal.Type.Duration(1))
        daysResIdList.forEach { checkTypeDay(stringResId = it, colorResId = R.color.colorActive) }
    }

    @Test
    fun daysAreWorking() {
        val dailyGoalHint = getString(R.string.change_record_type_daily_goal_time).lowercase()
        val goalHint = getString(R.string.change_record_type_goal_time_hint).lowercase()
        val currentDay = Calendar.getInstance()
            .get(Calendar.DAY_OF_WEEK)
            .let(timeMapper::toDayOfWeek)

        val name1 = "name1"
        val name2 = "name2"

        // Add data
        val goal = GoalsTestUtils.getDailyDurationGoal(TimeUnit.HOURS.toSeconds(1))
        testUtils.addActivity(
            name = name1,
            goals = listOf(
                goal.copy(daysOfWeek = goal.daysOfWeek.removeIf { it == currentDay }),
            ),
        )
        testUtils.addActivity(
            name = name2,
            goals = listOf(goal),
        )
        testUtils.addRunningRecord(name1)
        testUtils.addRunningRecord(name2)
        Thread.sleep(1000)

        // Checkmark on type
        tryAction { checkNoTypeMark(name1) }
        checkTypeMark(name2, isVisible = false)

        // Running record
        checkNoRunningGoal(name1)
        checkRunningGoal(name2, "$dailyGoalHint 59$minuteString")

        // Statistics
        NavUtils.openStatisticsScreen()
        checkNoStatisticsGoal(name1)
        scrollTo(name2)
        checkStatisticsGoal(name2, minuteString, "$goalHint - 1$hourString 0$minuteString")

        clickOnPrevDate()
        scrollTo(name1)
        checkStatisticsGoal(name1, "0$minuteString", "$goalHint - 1$hourString 0$minuteString")
        scrollTo(name2)
        checkStatisticsGoal(name2, "0$minuteString", "$goalHint - 1$hourString 0$minuteString")
    }

    private fun checkTypeDay(
        stringResId: Int,
        colorResId: Int,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.containerDayOfWeekItem),
                hasDescendant(withCardColor(colorResId)),
                hasDescendant(withText(stringResId)),
            ),
        )
    }

    private fun scrollTo(typeName: String) {
        tryAction {
            scrollRecyclerInPagerToView(
                statisticsR.id.rvStatisticsList,
                allOf(withId(R.id.viewStatisticsGoalItem), hasDescendant(withText(typeName))),
            )
        }
    }

    companion object {
        private val daysResIdList = listOf(
            R.string.day_of_week_sunday,
            R.string.day_of_week_monday,
            R.string.day_of_week_tuesday,
            R.string.day_of_week_wednesday,
            R.string.day_of_week_thursday,
            R.string.day_of_week_friday,
            R.string.day_of_week_saturday,
        )
    }
}
