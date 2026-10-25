package com.example.util.simpletimetracker

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.GoalsTestUtils.checkTypeMark
import com.example.util.simpletimetracker.GoalsTestUtils.checkNoTypeMark
import com.example.util.simpletimetracker.GoalsTestUtils.checkTypeLimitMark
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.tryAction
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
                GoalsTestUtils.getYearlyDurationGoal(1),
                GoalsTestUtils.getWeeklyCountGoal(1),
                GoalsTestUtils.getMonthlyCountGoal(1),
                GoalsTestUtils.getYearlyCountGoal(1),
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
            goals = listOf(GoalsTestUtils.getDailyDurationGoal(120)),
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
            timeStarted = current - TimeUnit.MINUTES.toMillis(1),
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
            timeStarted = current - TimeUnit.MINUTES.toMillis(1),
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

    @Test
    fun severalDailyGoals() {
        val typeName = "multipleDailyGoals"
        val currentTime = System.currentTimeMillis()
        testUtils.addActivity(
            name = typeName,
            goals = listOf(
                GoalsTestUtils.getDailyDurationGoal(TimeUnit.MINUTES.toSeconds(20)),
                GoalsTestUtils.getDailyDurationGoal(TimeUnit.MINUTES.toSeconds(10)),
                GoalsTestUtils.getDailyCountGoal(3),
                // Goals outside today's applicable set must not prevent completion.
                GoalsTestUtils.getWeeklyDurationGoal(TimeUnit.HOURS.toSeconds(10)),
                GoalsTestUtils.getDailyDurationGoal(TimeUnit.HOURS.toSeconds(10))
                    .copy(daysOfWeek = emptySet()),
            ),
        )
        tryAction { checkTypeMark(typeName, isVisible = false) }

        fun addRecord(duration: Long) {
            NavUtils.openRecordsScreen()
            testUtils.addRecord(
                typeName = typeName,
                timeStarted = currentTime - duration,
                timeEnded = currentTime,
            )
            NavUtils.openRunningRecordsScreen()
        }

        // Only the first duration target has been reached.
        addRecord(TimeUnit.MINUTES.toMillis(10))
        checkTypeMark(typeName, isVisible = false)

        // Both duration targets are complete, but the count target is still pending.
        addRecord(TimeUnit.MINUTES.toMillis(10))
        checkTypeMark(typeName, isVisible = false)

        // All applicable goals are now reached.
        addRecord(TimeUnit.SECONDS.toMillis(1))
        checkTypeMark(typeName, isVisible = true)
    }

    @Test
    fun dailyGoalAndLimit() {
        val typeName = "dailyGoalAndLimit"
        val currentTime = System.currentTimeMillis()
        testUtils.addActivity(
            name = typeName,
            goals = listOf(
                GoalsTestUtils.getDailyDurationGoal(TimeUnit.MINUTES.toSeconds(10)),
                GoalsTestUtils.getDailyDurationGoal(TimeUnit.MINUTES.toSeconds(20))
                    .copy(subtype = RecordTypeGoal.Subtype.Limit),
            ),
        )
        tryAction { checkTypeMark(typeName, isVisible = false) }
        checkTypeLimitMark(typeName, isReached = false)

        fun addRecord(duration: Long) {
            NavUtils.openRecordsScreen()
            testUtils.addRecord(
                typeName = typeName,
                timeStarted = currentTime - duration,
                timeEnded = currentTime,
            )
            NavUtils.openRunningRecordsScreen()
        }

        // Reaching the goal leaves the limit's successful indicator intact.
        addRecord(TimeUnit.MINUTES.toMillis(10))
        checkTypeMark(typeName, isVisible = true)
        checkTypeLimitMark(typeName, isReached = false)

        // Exactly twenty minutes is still allowed.
        addRecord(TimeUnit.MINUTES.toMillis(10))
        checkTypeMark(typeName, isVisible = true)
        checkTypeLimitMark(typeName, isReached = false)

        // One additional second breaches the limit without changing goal completion.
        addRecord(TimeUnit.SECONDS.toMillis(1))
        checkTypeMark(typeName, isVisible = true)
        checkTypeLimitMark(typeName, isReached = true)
    }
}
