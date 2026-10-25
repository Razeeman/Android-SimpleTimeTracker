package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_change_record.R
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.hamcrest.Description
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_goals.R as changeGoalsR
import com.example.util.simpletimetracker.feature_change_goals.api.R as changeGoalsApiR

object GoalsTestUtils {

    val durationInSeconds = TimeUnit.MINUTES.toSeconds(10)
    private val durationInMillis = TimeUnit.MINUTES.toMillis(10)

    fun getSessionDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Session, duration)

    fun getSessionDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Session, duration)

    fun getDailyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Daily, duration)

    fun getDailyDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Daily, duration)

    fun getDailyDurationGoalTag(duration: Long): RecordTypeGoal =
        getDurationGoalTag(RecordTypeGoal.Range.Daily, duration)

    fun getWeeklyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Weekly, duration)

    fun getWeeklyDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Weekly, duration)

    fun getWeeklyDurationGoalTag(duration: Long): RecordTypeGoal =
        getDurationGoalTag(RecordTypeGoal.Range.Weekly, duration)

    fun getMonthlyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Monthly, duration)

    fun getMonthlyDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Monthly, duration)

    fun getMonthlyDurationGoalTag(duration: Long): RecordTypeGoal =
        getDurationGoalTag(RecordTypeGoal.Range.Monthly, duration)

    fun getYearlyDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Yearly, duration)

    fun getYearlyDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Yearly, duration)

    fun getYearlyDurationGoalTag(duration: Long): RecordTypeGoal =
        getDurationGoalTag(RecordTypeGoal.Range.Yearly, duration)

    fun getOverallDurationGoal(duration: Long): RecordTypeGoal =
        getDurationGoal(RecordTypeGoal.Range.Overall, duration)

    fun getOverallDurationGoalCategory(duration: Long): RecordTypeGoal =
        getDurationGoalCategory(RecordTypeGoal.Range.Overall, duration)

    fun getOverallDurationGoalTag(duration: Long): RecordTypeGoal =
        getDurationGoalTag(RecordTypeGoal.Range.Overall, duration)

    fun getDailyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Daily, count)

    fun getDailyCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Daily, count)

    fun getDailyCountGoalTag(count: Long): RecordTypeGoal =
        getCountGoalTag(RecordTypeGoal.Range.Daily, count)

    fun getWeeklyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Weekly, count)

    fun getWeeklyCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Weekly, count)

    fun getWeeklyCountGoalTag(count: Long): RecordTypeGoal =
        getCountGoalTag(RecordTypeGoal.Range.Weekly, count)

    fun getMonthlyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Monthly, count)

    fun getMonthlyCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Monthly, count)

    fun getMonthlyCountGoalTag(count: Long): RecordTypeGoal =
        getCountGoalTag(RecordTypeGoal.Range.Monthly, count)

    fun getYearlyCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Yearly, count)

    fun getYearlyCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Yearly, count)

    fun getYearlyCountGoalTag(count: Long): RecordTypeGoal =
        getCountGoalTag(RecordTypeGoal.Range.Yearly, count)

    fun getOverallCountGoal(count: Long): RecordTypeGoal =
        getCountGoal(RecordTypeGoal.Range.Overall, count)

    fun getOverallCountGoalCategory(count: Long): RecordTypeGoal =
        getCountGoalCategory(RecordTypeGoal.Range.Overall, count)

    fun getOverallCountGoalTag(count: Long): RecordTypeGoal =
        getCountGoalTag(RecordTypeGoal.Range.Overall, count)

    fun addGoal(range: RecordTypeGoal.Range = RecordTypeGoal.Range.Daily) {
        val addButton = withText(R.string.running_records_add_type)
        scrollRecyclerToView(changeGoalsApiR.id.rvChangeRecordTypeGoals, addButton)
        clickOnRecyclerItem(
            changeGoalsApiR.id.rvChangeRecordTypeGoals,
            withText(R.string.running_records_add_type),
        )

        if (range !is RecordTypeGoal.Range.Daily) {
            clickOnView(
                allOf(
                    withId(changeGoalsR.id.fieldRecordTypeGoalRange),
                    isDisplayed(),
                ),
            )
            clickOnView(
                allOf(
                    withText(getRangeStringResId(range)),
                    isDisplayed(),
                ),
            )
        }
    }

    fun expandGoal(
        range: RecordTypeGoal.Range,
        type: RecordTypeGoal.Type,
        value: String? = null,
    ) {
        val card = goalCard(range, type, value)
        scrollRecyclerToView(changeGoalsApiR.id.rvChangeRecordTypeGoals, card)
        clickOnView(
            allOf(
                withId(changeGoalsR.id.containerChangeRecordTypeGoalSummary),
                isDescendantOfA(card),
            ),
        )
    }

    fun goalCard(
        range: RecordTypeGoal.Range,
        type: RecordTypeGoal.Type,
        value: String? = null,
    ): Matcher<View> {
        val typeStringResId = when (type) {
            is RecordTypeGoal.Type.Duration -> R.string.change_record_type_goal_duration
            is RecordTypeGoal.Type.Count -> R.string.change_record_type_goal_count
        }
        val card = allOf(
            withId(changeGoalsR.id.containerChangeRecordTypeGoalCard),
            hasDescendant(
                allOf(
                    withId(changeGoalsR.id.tvChangeRecordTypeGoalRange),
                    withText(getRangeStringResId(range)),
                ),
            ),
            hasDescendant(
                allOf(
                    withId(changeGoalsR.id.tvChangeRecordTypeGoalType),
                    withText(typeStringResId),
                ),
            ),
        )
        return if (value == null) {
            card
        } else {
            allOf(
                card,
                hasDescendant(
                    allOf(
                        withId(changeGoalsR.id.tvChangeRecordTypeGoalSummary),
                        withSubstring(value),
                    ),
                ),
            )
        }
    }

    fun goalField(
        range: RecordTypeGoal.Range,
        type: RecordTypeGoal.Type,
        fieldId: Int,
    ): Matcher<View> {
        return allOf(
            withId(fieldId),
            isDescendantOfA(goalCard(range, type)),
        )
    }

    fun visibleGoalField(fieldId: Int): Matcher<View> {
        return allOf(withId(fieldId), isDisplayed())
    }

    fun addRecords(testUtils: TestUtils, typeName: String, tagNames: List<String> = emptyList()) {
        val currentTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
        }.timeInMillis
        val thisWeek = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            val dateShift = when {
                get(Calendar.DAY_OF_WEEK) == firstDayOfWeek -> +1
                get(Calendar.DAY_OF_MONTH) == 1 -> +1
                else -> -1
            }
            add(Calendar.DATE, dateShift)
        }.timeInMillis
        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            val dateShift = if (get(Calendar.DAY_OF_MONTH) < 15) +7 else -7
            add(Calendar.DATE, dateShift)
        }.timeInMillis

        testUtils.addRecord(
            typeName = typeName,
            timeStarted = currentTime - durationInMillis,
            timeEnded = currentTime,
            tagNames = tagNames,
        )
        testUtils.addRecord(
            typeName = typeName,
            timeStarted = thisWeek - durationInMillis,
            timeEnded = thisWeek,
            tagNames = tagNames,
        )
        testUtils.addRecord(
            typeName = typeName,
            timeStarted = thisMonth - durationInMillis,
            timeEnded = thisMonth,
            tagNames = tagNames,
        )
    }

    fun addYearlyRecords(testUtils: TestUtils, typeName: String, tagNames: List<String> = emptyList()) {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val times = listOf(
            now.timeInMillis,
            (now.clone() as Calendar).apply { set(Calendar.DAY_OF_YEAR, 1) }.timeInMillis,
            (now.clone() as Calendar).apply {
                set(Calendar.DAY_OF_YEAR, maxOf(1, now.get(Calendar.DAY_OF_YEAR) - 1))
            }.timeInMillis,
        )
        times.forEach { time ->
            testUtils.addRecord(
                typeName = typeName,
                timeStarted = time - durationInMillis,
                timeEnded = time,
                tagNames = tagNames,
            )
        }
    }

    fun addOverallRecords(
        testUtils: TestUtils,
        typeName: String,
        tagNames: List<String> = emptyList(),
    ) {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // One ten-minute record in each of three years distinguishes overall from yearly progress.
        (0..2).forEach { yearsAgo ->
            val time = (now.clone() as Calendar).apply {
                add(Calendar.YEAR, -yearsAgo)
            }.timeInMillis
            testUtils.addRecord(
                typeName = typeName,
                timeStarted = time - durationInMillis,
                timeEnded = time,
                tagNames = tagNames,
            )
        }
    }

    fun checkNoStatisticsGoal(typeName: String) {
        allOf(
            isDescendantOfA(withId(R.id.viewStatisticsGoalItem)),
            hasSibling(withText(typeName)),
            isCompletelyDisplayed(),
        ).let(::checkViewDoesNotExist)
    }

    fun checkStatisticsGoal(
        typeName: String,
        current: String,
        goal: String,
    ) {
        allOf(
            withId(baseR.id.viewStatisticsGoalItem),
            hasDescendant(withText(typeName)),
            hasDescendant(
                allOf(
                    withId(R.id.tvStatisticsGoalItemCurrent), withSubstring(current),
                ),
            ),
            hasDescendant(
                allOf(
                    withId(R.id.tvStatisticsGoalItemGoal), withText(goal),
                ),
            ),
            isCompletelyDisplayed(),
        ).let(::checkViewIsDisplayed)
    }

    fun checkStatisticsPercent(
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

    fun checkStatisticsMark(typeName: String, isVisible: Boolean) {
        allOf(withId(R.id.viewStatisticsGoalItem), hasDescendant(withText(typeName)), isCompletelyDisplayed())
            .let(::checkViewIsDisplayed)
        allOf(getStatisticsMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheckOutline))
            .let(::checkViewIsDisplayed)
        allOf(getStatisticsMatcher(typeName), withId(R.id.ivGoalCheckmarkItemCheck))
            .let { if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it) }
    }

    fun checkTypeMark(typeName: String, isVisible: Boolean) {
        allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText(typeName)), isCompletelyDisplayed())
            .let(::checkViewIsDisplayed)
        getTypeMarkMatcher(typeName, R.id.ivGoalCheckmarkItemCheckOutline)
            .let(::checkViewIsDisplayed)
        getTypeMarkMatcher(typeName, R.id.ivGoalCheckmarkItemCheck)
            .let { if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it) }
    }

    fun checkNoTypeMark(typeName: String) {
        allOf(withId(R.id.viewRecordTypeItem), hasDescendant(withText(typeName)), isCompletelyDisplayed())
            .let(::checkViewIsDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.viewRecordTypeItemGoalCheckmark))
            .let(::checkViewIsNotDisplayed)
        allOf(getTypeMatcher(typeName), withId(R.id.viewRecordTypeItemLimitCheckmark))
            .let(::checkViewIsNotDisplayed)
    }

    fun checkTypeLimitMark(typeName: String, isReached: Boolean) {
        val state = if (isReached) {
            GoalCheckmarkView.CheckState.LIMIT_REACHED
        } else {
            GoalCheckmarkView.CheckState.LIMIT_NOT_REACHED
        }
        allOf(
            getTypeMatcher(typeName),
            withId(R.id.viewRecordTypeItemLimitCheckmark),
            withGoalCheckState(state),
        ).let(::checkViewIsDisplayed)
    }

    fun checkRunningGoal(typeName: String, goal: String, isSecondRow: Boolean = false) {
        allOf(
            getRunningMatcher(typeName),
            withId(if (isSecondRow) R.id.tvRunningRecordItemGoalTimeSecond else R.id.tvRunningRecordItemGoalTime),
            withSubstring(goal),
        ).let(::checkViewIsDisplayed)
    }

    fun checkNoRunningGoal(typeName: String) {
        allOf(
            getRunningMatcher(typeName),
            withId(R.id.tvRunningRecordItemGoalTime),
        ).let(::checkViewIsNotDisplayed)
    }

    fun checkRunningMark(typeName: String, isVisible: Boolean, isSecondRow: Boolean = false) {
        allOf(
            getRunningMatcher(typeName),
            withId(if (isSecondRow) R.id.ivRunningRecordItemGoalTimeCheckSecond else R.id.ivRunningRecordItemGoalTimeCheck),
        ).let {
            if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it)
        }
    }

    fun checkRunningLimitMark(typeName: String, isSecondRow: Boolean = true) {
        allOf(
            getRunningMatcher(typeName),
            withId(if (isSecondRow) R.id.ivRunningRecordItemGoalTimeCheckSecond else R.id.ivRunningRecordItemGoalTimeCheck),
            withGoalCheckState(GoalCheckmarkView.CheckState.LIMIT_REACHED),
        ).let(::checkViewIsDisplayed)
    }

    private fun getRunningMatcher(typeName: String): Matcher<View> {
        // Other tabs can keep a copy of the card attached; the mark itself may be hidden.
        return isDescendantOfA(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(typeName)),
                isCompletelyDisplayed(),
            ),
        )
    }

    private fun withGoalCheckState(state: GoalCheckmarkView.CheckState): Matcher<View> =
        object : BoundedMatcher<View, GoalCheckmarkView>(GoalCheckmarkView::class.java) {
            override fun matchesSafely(view: GoalCheckmarkView): Boolean {
                return view.itemCheckState == state
            }

            override fun describeTo(description: Description) {
                description.appendText("goal check state: $state")
            }
        }

    private fun getTypeMatcher(typeName: String): Matcher<View> {
        return isDescendantOfA(
            allOf(
                withId(R.id.viewRecordTypeItem),
                hasDescendant(withText(typeName)),
            ),
        )
    }

    private fun getTypeMarkMatcher(typeName: String, viewId: Int): Matcher<View> {
        return allOf(
            withId(viewId),
            getTypeMatcher(typeName),
            isDescendantOfA(withId(R.id.viewRecordTypeItemGoalCheckmark)),
        )
    }

    private fun getStatisticsMatcher(typeName: String): Matcher<View> {
        return isDescendantOfA(
            allOf(
                withId(R.id.viewStatisticsGoalItem),
                hasDescendant(withText(typeName)),
            ),
        )
    }

    private fun getDurationGoal(
        range: RecordTypeGoal.Range,
        duration: Long,
    ): RecordTypeGoal {
        return RecordTypeGoal(
            idData = RecordTypeGoal.IdData.Type(0),
            range = range,
            type = RecordTypeGoal.Type.Duration(duration),
            subtype = RecordTypeGoal.Subtype.Goal,
            daysOfWeek = DayOfWeek.entries.toSet(),
        )
    }

    private fun getDurationGoalCategory(
        range: RecordTypeGoal.Range,
        duration: Long,
    ): RecordTypeGoal {
        return getDurationGoal(range = range, duration = duration)
            .copy(idData = RecordTypeGoal.IdData.Category(0))
    }

    private fun getDurationGoalTag(
        range: RecordTypeGoal.Range,
        duration: Long,
    ): RecordTypeGoal {
        return getDurationGoal(range = range, duration = duration)
            .copy(idData = RecordTypeGoal.IdData.Tag(0))
    }

    private fun getCountGoal(
        range: RecordTypeGoal.Range,
        count: Long,
    ): RecordTypeGoal {
        return RecordTypeGoal(
            idData = RecordTypeGoal.IdData.Type(0),
            range = range,
            type = RecordTypeGoal.Type.Count(count),
            subtype = RecordTypeGoal.Subtype.Goal,
            daysOfWeek = DayOfWeek.entries.toSet(),
        )
    }

    private fun getCountGoalCategory(
        range: RecordTypeGoal.Range,
        count: Long,
    ): RecordTypeGoal {
        return getCountGoal(range = range, count = count)
            .copy(idData = RecordTypeGoal.IdData.Category(0))
    }

    private fun getCountGoalTag(
        range: RecordTypeGoal.Range,
        count: Long,
    ): RecordTypeGoal {
        return getCountGoal(range = range, count = count)
            .copy(idData = RecordTypeGoal.IdData.Tag(0))
    }

    private fun getRangeStringResId(range: RecordTypeGoal.Range): Int {
        return when (range) {
            is RecordTypeGoal.Range.Session -> R.string.change_record_type_session_goal_time
            is RecordTypeGoal.Range.Daily -> R.string.change_record_type_daily_goal_time
            is RecordTypeGoal.Range.Weekly -> R.string.change_record_type_weekly_goal_time
            is RecordTypeGoal.Range.Monthly -> R.string.change_record_type_monthly_goal_time
            is RecordTypeGoal.Range.Yearly -> R.string.change_record_type_yealy_goal_time
            is RecordTypeGoal.Range.Overall -> R.string.range_overall
        }
    }
}
