package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GoalsOnCardsTest : BaseUiTest() {

    @Test
    fun noGoals() {
        val noGoals = "noGoals"
        val otherGoals = "otherGoals"

        // Add data
        testUtils.addActivity(noGoals)
        testUtils.addActivity(
            name = otherGoals,
            goals = listOf(
                GoalsTestUtils.getSessionDurationGoal(1),
                GoalsTestUtils.getWeeklyDurationGoal(1),
                GoalsTestUtils.getMonthlyDurationGoal(1),
                GoalsTestUtils.getWeeklyCountGoal(1),
                GoalsTestUtils.getMonthlyCountGoal(1),
            ),
        )
        Thread.sleep(1000)

        // Check
        checkNoGoal(noGoals)
        checkNoGoal(otherGoals)
    }

    @Test
    fun checkmarks() {
        val durationGoal = "durationGoal"
        val countGoal = "countGoal"

        // Add data
        testUtils.addActivity(
            name = durationGoal,
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(2)),
        )
        testUtils.addActivity(
            name = countGoal,
            goals = listOf(GoalsTestUtils.getDailyCountGoal(2)),
        )
        Thread.sleep(1000)

        // Not reached
        checkCheckmark(durationGoal, isVisible = false)
        checkCheckmark(countGoal, isVisible = false)

        // Add records
        NavUtils.openRecordsScreen()
        val current = System.currentTimeMillis()
        testUtils.addRecord(
            typeName = durationGoal,
            timeStarted = current - TimeUnit.SECONDS.toMillis(1),
            timeEnded = current,
        )
        testUtils.addRecord(
            typeName = countGoal,
        )

        // Not reached
        NavUtils.openRunningRecordsScreen()
        checkCheckmark(durationGoal, isVisible = false)
        checkCheckmark(countGoal, isVisible = false)

        // Add more records
        NavUtils.openRecordsScreen()
        testUtils.addRecord(
            typeName = durationGoal,
            timeStarted = current - TimeUnit.SECONDS.toMillis(1),
            timeEnded = current,
        )
        testUtils.addRecord(
            typeName = countGoal,
        )

        // Reached
        NavUtils.openRunningRecordsScreen()
        checkCheckmark(durationGoal, isVisible = true)
        checkCheckmark(countGoal, isVisible = true)
    }

    private fun checkNoGoal(typeName: String) {
        allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText(typeName)), isCompletelyDisplayed())
            .let(::checkViewIsDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheckOutline))
            .let(::checkViewIsNotDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheck))
            .let(::checkViewIsNotDisplayed)
    }

    private fun checkCheckmark(typeName: String, isVisible: Boolean) {
        allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText(typeName)), isCompletelyDisplayed())
            .let(::checkViewIsDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheckOutline))
            .let(::checkViewIsDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheck))
            .let { if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it) }
    }

    private fun getTypeMatcher(typeName: String): Matcher<View> {
        return isDescendantOfA(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(typeName)),
            ),
        )
    }
}
