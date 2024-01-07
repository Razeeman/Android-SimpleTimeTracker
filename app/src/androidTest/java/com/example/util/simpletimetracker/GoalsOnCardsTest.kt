package com.example.util.simpletimetracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.GoalsTestUtils.checkTypeMark
import com.example.util.simpletimetracker.GoalsTestUtils.checkNoTypeMark
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import dagger.hilt.android.testing.HiltAndroidTest
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
        checkNoTypeMark(noGoals)
        checkNoTypeMark(otherGoals)
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
        checkTypeMark(durationGoal, isVisible = false)
        checkTypeMark(countGoal, isVisible = false)

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
        checkTypeMark(durationGoal, isVisible = false)
        checkTypeMark(countGoal, isVisible = false)

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
        checkTypeMark(durationGoal, isVisible = true)
        checkTypeMark(countGoal, isVisible = true)
    }
}
