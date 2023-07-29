package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import org.hamcrest.CoreMatchers.allOf
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR

object GoalsTestUtils {

    val durationInSeconds = TimeUnit.MINUTES.toSeconds(10)
    private val durationInMillis = TimeUnit.MINUTES.toMillis(10)

    fun getSessionDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Session, duration)

    fun getDailyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Daily, duration)

    fun getWeeklyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Weekly, duration)

    fun getMonthlyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Monthly, duration)

    fun getDailyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Daily, count)

    fun getWeeklyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Weekly, count)

    fun getMonthlyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Monthly, count)

    fun addRecords(testUtils: TestUtils, typeName: String) {
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR, 15)
        }.timeInMillis
        val thisWeek = Calendar.getInstance().apply {
            set(Calendar.HOUR, 15)
            val dateShift = if (get(Calendar.DAY_OF_WEEK) == firstDayOfWeek) +1 else -1
            add(Calendar.DATE, dateShift)
        }.timeInMillis
        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.HOUR, 15)
            val dateShift = if (get(Calendar.DAY_OF_MONTH) < 15) +7 else -7
            add(Calendar.DATE, dateShift)
        }.timeInMillis

        testUtils.addRecord(
            typeName = typeName,
            timeStarted = currentTime - durationInMillis,
            timeEnded = currentTime,
        )
        testUtils.addRecord(
            typeName = typeName,
            timeStarted = thisWeek - durationInMillis,
            timeEnded = thisWeek,
        )
        testUtils.addRecord(
            typeName = typeName,
            timeStarted = thisMonth - durationInMillis,
            timeEnded = thisMonth,
        )
    }

    fun checkNoGoal(typeName: String) {
        allOf(
            isDescendantOfA(withId(R.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            isCompletelyDisplayed(),
        ).let(::checkViewDoesNotExist)
    }

    fun checkGoal(
        typeName: String,
        current: String,
        goal: String,
    ) {
        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvStatisticsGoalItemCurrent),
            withText(current),
        ).let(::checkViewIsDisplayed)

        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvStatisticsGoalItemGoal),
            withText(goal),
        ).let(::checkViewIsDisplayed)
    }

    fun checkGoalPercent(
        typeName: String,
        percent: String,
    ) {
        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.tvStatisticsGoalItemPercent),
            withText(percent),
        ).let(::checkViewIsDisplayed)
    }

    fun checkGoalMark(typeName: String, isVisible: Boolean) {
        allOf(
            isDescendantOfA(withId(baseR.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            withId(R.id.ivStatisticsGoalItemCheck),
        ).let {
            if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it)
        }
    }

    private fun getDurationGoal(
        range: RecordTypeGoal.Range,
        duration: Long,
    ): RecordTypeGoal {
        return RecordTypeGoal(
            typeId = 0,
            range = range,
            type = RecordTypeGoal.Type.Duration(duration),
        )
    }

    private fun getCountGoal(
        range: RecordTypeGoal.Range,
        count: Long,
    ): RecordTypeGoal {
        return RecordTypeGoal(
            typeId = 0,
            range = range,
            type = RecordTypeGoal.Type.Count(count),
        )
    }
}
