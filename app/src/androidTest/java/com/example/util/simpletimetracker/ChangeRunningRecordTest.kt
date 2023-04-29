package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.Calendar
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_running_record.R as changeRunningRecordR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_running_records.R as runningRecordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeRunningRecordTest : BaseUiTest() {

    @Test
    fun changeRunningRecord() {
        val name1 = "Test1"
        val name2 = "Test2"
        val firstGoalTime = TimeUnit.MINUTES.toSeconds(10)
        val comment = "comment"
        val tag2 = "Tag2"
        val fullName2 = "$name2 - $tag2"

        // Add activities
        testUtils.addActivity(name = name1, color = firstColor, icon = firstIcon, goalTime = firstGoalTime)
        testUtils.addActivity(name = name2, color = lastColor, text = lastEmoji)
        testUtils.addRecordTag(tag2, name2)

        // Start timer
        tryAction { clickOnViewWithText(name1) }
        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime
        var timeStarted = timeMapper.formatDateTime(
            time = timeStartedTimestamp, useMilitaryTime = true, showSeconds = false
        )
        var timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false) }
        val goalString = getString(coreR.string.change_record_type_session_goal_time).lowercase() + " 9$minuteString"

        checkRunningRecordDisplayed(
            name = name1,
            color = firstColor,
            icon = firstIcon,
            timeStarted = timeStartedPreview,
            goalTime = goalString,
            comment = ""
        )

        // Open edit view
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(name1)))

        // View is set up
        checkViewIsDisplayed(withId(changeRunningRecordR.id.btnChangeRunningRecordDelete))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordType))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordCategories))
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeAdjust))
        checkViewIsNotDisplayed(allOf(withId(changeRecordR.id.etChangeRecordComment), withText("")))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStarted), withText(timeStarted)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name1)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withSubstring(goalString)))

        // Change item
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name2))
        tryAction { clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag2)) }
        clickOnViewWithText(coreR.string.change_record_tag_field)

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val hourStarted = 0
        val minutesStarted = 0
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        clickOnViewWithId(changeRecordR.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithText(coreR.string.date_time_dialog_date)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(year, month + 1, day))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        timeStartedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourStarted)
            set(Calendar.MINUTE, minutesStarted)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp
            .let { timeMapper.formatDateTime(time = it, useMilitaryTime = true, showSeconds = false) }
        timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false) }

        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        clickOnViewWithText(coreR.string.change_record_comment_field)
        typeTextIntoView(changeRecordR.id.etChangeRecordComment, comment)
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(fullName2)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withText(lastEmoji)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(comment)))
        checkViewIsNotDisplayed(
            allOf(
                isDescendantOfA(withId(changeRunningRecordR.id.previewChangeRunningRecord)),
                withId(changeRecordR.id.tvRunningRecordItemGoalTime)
            )
        )

        // Save
        clickOnViewWithText(coreR.string.change_record_save)

        // Record updated
        tryAction {
            checkViewDoesNotExist(
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(name1))
            )
        }
        checkRunningRecordDisplayed(
            name = fullName2,
            color = lastColor,
            text = lastEmoji,
            timeStarted = timeStartedPreview,
            comment = comment
        )
    }

    @Test
    fun changeRecordUntagged() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"
        val fullName1 = "$name1 - $tag1"
        val fullName2 = "$name2 - $tag2, $tag3"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name2)
        testUtils.addRecordTag(tag3)
        Thread.sleep(1000)

        // Add running record
        tryAction { clickOnViewWithText(name1) }

        // Record is added
        checkRunningRecordDisplayed(name = name1)

        // Change tag
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(name1)))
        checkPreviewUpdated(hasDescendant(withText(name1)))
        clickOnViewWithText(coreR.string.change_record_tag_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag1))
        checkPreviewUpdated(hasDescendant(withText(fullName1)))
        clickOnViewWithText(coreR.string.change_record_tag_field)
        clickOnViewWithText(coreR.string.change_record_save)

        // Record updated
        tryAction { checkRunningRecordDisplayed(name = fullName1) }

        // Change activity and tag
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(fullName1)))
        checkPreviewUpdated(hasDescendant(withText(fullName1)))
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name2))
        checkPreviewUpdated(hasDescendant(withText(name2)))
        tryAction { clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag2)) }
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(tag3))
        checkPreviewUpdated(hasDescendant(withText(fullName2)))
        clickOnViewWithText(coreR.string.change_record_tag_field)
        clickOnViewWithText(coreR.string.change_record_save)

        // Record updated
        tryAction { checkRunningRecordDisplayed(name = fullName2) }

        // Remove tag
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(fullName2)))
        checkPreviewUpdated(hasDescendant(withText(fullName2)))
        clickOnViewWithText(coreR.string.change_record_tag_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordCategories, withText(coreR.string.change_record_untagged))
        checkPreviewUpdated(hasDescendant(withText(name2)))
        clickOnViewWithText(coreR.string.change_record_tag_field)
        clickOnViewWithText(coreR.string.change_record_save)

        // Record updated
        tryAction { checkRunningRecordDisplayed(name = name2) }
    }

    @Test
    fun changeRunningRecordAdjustTime() {
        // Add activity
        val name = "Test"
        testUtils.addActivity(name)

        // Setup
        val hourStarted = 0
        val minutesStarted = 0

        tryAction { clickOnViewWithText(name) }
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(name)))
        clickOnViewWithId(changeRecordR.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkAfterTimeAdjustment(timeStarted = "00:00")

        // Check visibility
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeAdjust))
        unconstrainedClickOnView(withId(changeRecordR.id.btnChangeRecordTimeStartedAdjust))
        checkViewIsNotDisplayed(withId(changeRecordR.id.containerChangeRecordTimeAdjust))
        unconstrainedClickOnView(withId(changeRecordR.id.btnChangeRecordTimeStartedAdjust))
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeAdjust))

        // Check time adjustments
        clickOnViewWithText("+30")
        checkAfterTimeAdjustment(timeStarted = "00:30")
        clickOnViewWithText("+5")
        checkAfterTimeAdjustment(timeStarted = "00:35")
        clickOnViewWithText("+1")
        checkAfterTimeAdjustment(timeStarted = "00:36")
        clickOnViewWithText("-1")
        checkAfterTimeAdjustment(timeStarted = "00:35")
        clickOnViewWithText("-5")
        checkAfterTimeAdjustment(timeStarted = "00:30")
        clickOnViewWithText("-30")
        checkAfterTimeAdjustment(timeStarted = "00:00")
        clickOnViewWithText("-30")
        checkAfterTimeAdjustment(timeStarted = "23:30")
        clickOnViewWithText(coreR.string.time_now)
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRunningRecordItemTimer), withText("0$secondString")))
        )
        clickOnViewWithText("+30")
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRunningRecordItemTimer), withText("0$secondString")))
        )
    }

    @Test
    fun lastComments() {
        val nameNoComments = "Name1"
        val nameComment = "Name2"
        val nameComments = "Name3"
        val comment1 = "Comment1"
        val comment2 = "Comment2"
        val comment3 = "Comment3"

        // Add data
        testUtils.addActivity(nameNoComments)
        testUtils.addActivity(nameComment)
        testUtils.addActivity(nameComments)
        testUtils.addRecord(nameNoComments)
        testUtils.addRecord(nameComment, comment = comment1)
        testUtils.addRecord(nameComments, comment = comment2)
        testUtils.addRecord(nameComments, comment = comment3)
        Thread.sleep(1000)

        // No last comments
        tryAction { clickOnViewWithText(nameNoComments) }
        longClickOnView(
            allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(nameNoComments))
        )

        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Select activity with one previous comment
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(nameComment))

        // One last comment
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewIsDisplayed(withText(coreR.string.change_record_last_comments_hint))
        checkViewIsDisplayed(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))

        // Select last comment
        clickOnViewWithText(comment1)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment1))) }
        typeTextIntoView(changeRecordR.id.etChangeRecordComment, "")
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Select activity with many previous comments
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(nameComments))

        // Two last comments
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewIsDisplayed(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(comment1))
        checkViewIsDisplayed(withText(comment2))
        checkViewIsDisplayed(withText(comment3))

        // Select last comment
        clickOnViewWithText(comment2)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment2))) }
        clickOnViewWithText(comment3)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment3))) }
        typeTextIntoView(changeRecordR.id.etChangeRecordComment, "")
        clickOnViewWithText(coreR.string.change_record_comment_field)

        // Select activity with no previous comments
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(nameNoComments))

        // No last comments
        clickOnViewWithText(coreR.string.change_record_comment_field)
        closeSoftKeyboard()
        checkViewDoesNotExist(withText(coreR.string.change_record_last_comments_hint))
        checkViewDoesNotExist(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))
        clickOnViewWithText(coreR.string.change_record_comment_field)
    }

    @Test
    fun goalTimes() {
        fun checkGoal(typeName: String, @IdRes goalTextId: Int, goal: String) {
            allOf(
                isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                hasSibling(withText(typeName)),
                withId(goalTextId),
                withSubstring(goal),
            ).let(::checkViewIsDisplayed)
        }

        fun checkNoGoal(typeName: String, @IdRes goalTextId: Int) {
            allOf(
                isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                hasSibling(withText(typeName)),
                withId(goalTextId),
            ).let(::checkViewIsNotDisplayed)
        }

        fun checkGoalMark(typeName: String, @IdRes checkId: Int, isVisible: Boolean) {
            allOf(
                isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                hasSibling(withText(typeName)),
                withId(checkId),
            ).let {
                if (isVisible) checkViewIsDisplayed(it) else checkViewIsNotDisplayed(it)
            }
        }

        fun scrollTo(typeName: String) {
            tryAction {
                scrollRecyclerToView(
                    runningRecordsR.id.rvRunningRecordsList,
                    allOf(withId(changeRecordR.id.viewRunningRecordItem), hasDescendant(withText(typeName)))
                )
            }
        }

        val sessionGoal = getString(coreR.string.change_record_type_session_goal_time).lowercase()
        val dailyGoal = getString(coreR.string.change_record_type_daily_goal_time).lowercase()
        val weeklyGoal = getString(coreR.string.change_record_type_weekly_goal_time).lowercase()
        val currentTime = Calendar.getInstance().timeInMillis

        val noGoals = "noGoals"
        val sessionGoalNotFinished = "sessionGoalNotFinished"
        val sessionGoalFinished = "sessionGoalFinished"
        val dailyGoalNotFinished = "dailyGoalNotFinished"
        val dailyGoalFinished = "dailyGoalFinished"
        val weeklyGoalNotFinished = "weeklyGoalNotFinished"
        val weeklyGoalFinished = "weeklyGoalFinished"
        val allGoalsNotFinished = "allGoalsNotFinished"
        val allGoalsFinished = "allGoalsFinished"
        val allGoalsSessionFinished = "allGoalsSessionFinished"
        val allGoalsDailyFinished = "allGoalsDailyFinished"
        val allGoalsWeeklyFinished = "allGoalsWeeklyFinished"

        // Add data
        testUtils.addActivity(noGoals)
        testUtils.addRunningRecord(noGoals)

        testUtils.addActivity(sessionGoalNotFinished, goalTime = TimeUnit.MINUTES.toSeconds(10))
        testUtils.addRecord(sessionGoalNotFinished)
        testUtils.addRunningRecord(sessionGoalNotFinished)

        testUtils.addActivity(sessionGoalFinished, goalTime = TimeUnit.MINUTES.toSeconds(10))
        testUtils.addRunningRecord(
            typeName = sessionGoalFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
        )

        testUtils.addActivity(dailyGoalNotFinished, dailyGoalTime = TimeUnit.MINUTES.toSeconds(10))
        testUtils.addRecord(
            typeName = dailyGoalNotFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(5),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(dailyGoalNotFinished)

        testUtils.addActivity(dailyGoalFinished, dailyGoalTime = TimeUnit.MINUTES.toSeconds(10))
        testUtils.addRecord(
            typeName = dailyGoalFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(dailyGoalFinished)

        testUtils.addActivity(weeklyGoalNotFinished, weeklyGoalTime = TimeUnit.MINUTES.toSeconds(10))
        testUtils.addRecord(
            typeName = weeklyGoalNotFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(5),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(weeklyGoalNotFinished)

        testUtils.addActivity(weeklyGoalFinished, weeklyGoalTime = TimeUnit.MINUTES.toSeconds(10))
        testUtils.addRecord(
            typeName = weeklyGoalFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(weeklyGoalFinished)

        testUtils.addActivity(
            name = allGoalsNotFinished,
            goalTime = TimeUnit.MINUTES.toSeconds(10),
            dailyGoalTime = TimeUnit.MINUTES.toSeconds(20),
            weeklyGoalTime = TimeUnit.MINUTES.toSeconds(30),
        )
        testUtils.addRecord(
            typeName = allGoalsNotFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(5),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(allGoalsNotFinished)

        testUtils.addActivity(
            name = allGoalsFinished,
            goalTime = TimeUnit.MINUTES.toSeconds(10),
            dailyGoalTime = TimeUnit.MINUTES.toSeconds(10),
            weeklyGoalTime = TimeUnit.MINUTES.toSeconds(10),
        )
        testUtils.addRecord(
            typeName = allGoalsFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
            timeEnded = currentTime,
        )
        testUtils.addRunningRecord(
            typeName = allGoalsFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
        )

        testUtils.addActivity(
            name = allGoalsSessionFinished,
            goalTime = TimeUnit.MINUTES.toSeconds(10),
            dailyGoalTime = TimeUnit.MINUTES.toSeconds(20),
            weeklyGoalTime = TimeUnit.MINUTES.toSeconds(30),
        )
        testUtils.addRunningRecord(
            typeName = allGoalsSessionFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
        )

        testUtils.addActivity(
            name = allGoalsDailyFinished,
            goalTime = TimeUnit.MINUTES.toSeconds(20),
            dailyGoalTime = TimeUnit.MINUTES.toSeconds(10),
            weeklyGoalTime = TimeUnit.MINUTES.toSeconds(30),
        )
        testUtils.addRunningRecord(
            typeName = allGoalsDailyFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
        )

        testUtils.addActivity(
            name = allGoalsWeeklyFinished,
            goalTime = TimeUnit.MINUTES.toSeconds(20),
            dailyGoalTime = TimeUnit.MINUTES.toSeconds(30),
            weeklyGoalTime = TimeUnit.MINUTES.toSeconds(10),
        )
        testUtils.addRunningRecord(
            typeName = allGoalsWeeklyFinished,
            timeStarted = currentTime - TimeUnit.MINUTES.toMillis(10),
        )

        // No goals
        Thread.sleep(1000)
        scrollTo(noGoals)
        checkNoGoal(noGoals, changeRecordR.id.tvRunningRecordItemGoalTime)
        checkGoalMark(noGoals, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkNoGoal(noGoals, changeRecordR.id.tvRunningRecordItemGoalTime2)
        checkGoalMark(noGoals, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkNoGoal(noGoals, changeRecordR.id.tvRunningRecordItemGoalTime3)
        checkGoalMark(noGoals, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // Session goal not finished
        scrollTo(sessionGoalNotFinished)
        checkGoal(
            sessionGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime, "$sessionGoal 9$minuteString"
        )
        checkGoalMark(sessionGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkNoGoal(sessionGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime2)
        checkGoalMark(sessionGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkNoGoal(sessionGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime3)
        checkGoalMark(sessionGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // Session goal finished
        scrollTo(sessionGoalFinished)
        checkGoal(sessionGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime, sessionGoal)
        checkGoalMark(sessionGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = true)
        checkNoGoal(sessionGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime2)
        checkGoalMark(sessionGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkNoGoal(sessionGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime3)
        checkGoalMark(sessionGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // Daily goal not finished
        scrollTo(dailyGoalNotFinished)
        checkNoGoal(dailyGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime)
        checkGoalMark(dailyGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkGoal(
            dailyGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime2, "$dailyGoal 4$minuteString"
        )
        checkGoalMark(dailyGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkNoGoal(dailyGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime3)
        checkGoalMark(dailyGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // Daily goal finished
        scrollTo(dailyGoalFinished)
        checkNoGoal(dailyGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime)
        checkGoalMark(dailyGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkGoal(dailyGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime2, dailyGoal)
        checkGoalMark(dailyGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = true)
        checkNoGoal(dailyGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime3)
        checkGoalMark(dailyGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // Weekly goal not finished
        scrollTo(weeklyGoalNotFinished)
        checkNoGoal(weeklyGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime)
        checkGoalMark(weeklyGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkNoGoal(weeklyGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime2)
        checkGoalMark(weeklyGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkGoal(
            weeklyGoalNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime3, "$weeklyGoal 4$minuteString"
        )
        checkGoalMark(weeklyGoalNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // Weekly goal finished
        scrollTo(weeklyGoalFinished)
        checkNoGoal(weeklyGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime)
        checkGoalMark(weeklyGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkNoGoal(weeklyGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime2)
        checkGoalMark(weeklyGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkGoal(weeklyGoalFinished, changeRecordR.id.tvRunningRecordItemGoalTime3, weeklyGoal)
        checkGoalMark(weeklyGoalFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = true)

        // All goals, all not finished
        scrollTo(allGoalsNotFinished)
        checkGoal(
            allGoalsNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime, "$sessionGoal 9$minuteString"
        )
        checkGoalMark(allGoalsNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkGoal(
            allGoalsNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime2, "$dailyGoal 14$minuteString"
        )
        checkGoalMark(allGoalsNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkGoal(
            allGoalsNotFinished, changeRecordR.id.tvRunningRecordItemGoalTime3, "$weeklyGoal 24$minuteString"
        )
        checkGoalMark(allGoalsNotFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // All goals, all finished
        scrollTo(allGoalsFinished)
        checkGoal(allGoalsFinished, changeRecordR.id.tvRunningRecordItemGoalTime, sessionGoal)
        checkGoalMark(allGoalsFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = true)
        checkGoal(allGoalsFinished, changeRecordR.id.tvRunningRecordItemGoalTime2, dailyGoal)
        checkGoalMark(allGoalsFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = true)
        checkGoal(allGoalsFinished, changeRecordR.id.tvRunningRecordItemGoalTime3, weeklyGoal)
        checkGoalMark(allGoalsFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = true)

        // All goals, session finished
        scrollTo(allGoalsSessionFinished)
        checkGoal(allGoalsSessionFinished, changeRecordR.id.tvRunningRecordItemGoalTime, sessionGoal)
        checkGoalMark(allGoalsSessionFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = true)
        checkGoal(
            allGoalsSessionFinished, changeRecordR.id.tvRunningRecordItemGoalTime2, "$dailyGoal 9$minuteString"
        )
        checkGoalMark(
            allGoalsSessionFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false
        )
        checkGoal(
            allGoalsSessionFinished, changeRecordR.id.tvRunningRecordItemGoalTime3, "$weeklyGoal 19$minuteString"
        )
        checkGoalMark(
            allGoalsSessionFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false
        )

        // All goals, daily finished
        scrollTo(allGoalsDailyFinished)
        checkGoal(
            allGoalsDailyFinished, changeRecordR.id.tvRunningRecordItemGoalTime, "$sessionGoal 9$minuteString"
        )
        checkGoalMark(allGoalsDailyFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkGoal(
            allGoalsDailyFinished, changeRecordR.id.tvRunningRecordItemGoalTime2, dailyGoal
        )
        checkGoalMark(allGoalsDailyFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = true)
        checkGoal(
            allGoalsDailyFinished, changeRecordR.id.tvRunningRecordItemGoalTime3, "$weeklyGoal 19$minuteString"
        )
        checkGoalMark(allGoalsDailyFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = false)

        // All goals, weekly finished
        scrollTo(allGoalsWeeklyFinished)
        checkGoal(
            allGoalsWeeklyFinished, changeRecordR.id.tvRunningRecordItemGoalTime, "$sessionGoal 9$minuteString"
        )
        checkGoalMark(allGoalsWeeklyFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck, isVisible = false)
        checkGoal(
            allGoalsWeeklyFinished, changeRecordR.id.tvRunningRecordItemGoalTime2, "$dailyGoal 19$minuteString"
        )
        checkGoalMark(allGoalsWeeklyFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck2, isVisible = false)
        checkGoal(
            allGoalsWeeklyFinished, changeRecordR.id.tvRunningRecordItemGoalTime3, weeklyGoal
        )
        checkGoalMark(allGoalsWeeklyFinished, changeRecordR.id.ivRunningRecordItemGoalTimeCheck3, isVisible = true)
    }

    private fun checkAfterTimeAdjustment(timeStarted: String) {
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRunningRecordItemTimeStarted), withText(timeStarted)))
        )
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStarted), withSubstring(timeStarted)))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRunningRecordR.id.previewChangeRunningRecord), matcher))

    private fun checkRunningRecordDisplayed(
        name: String,
        color: Int? = null,
        icon: Int? = null,
        text: String? = null,
        timeStarted: String? = null,
        goalTime: String? = null,
        comment: String? = null,
    ) {
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(name)))

        if (color != null) {
            checkViewIsDisplayed(allOf(withId(changeRecordR.id.viewRunningRecordItem), withCardColor(color)))
        }
        if (icon != null) {
            checkViewIsDisplayed(
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withTag(icon))
            )
        }
        if (text != null) {
            checkViewIsDisplayed(
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(text))
            )
        }
        if (timeStarted != null) {
            checkViewIsDisplayed(
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(timeStarted))
            )
        }
        if (!goalTime.isNullOrEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                    withSubstring(goalTime),
                )
            )
        } else {
            checkViewIsNotDisplayed(
                allOf(
                    isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                    withId(changeRecordR.id.tvRunningRecordItemGoalTime)
                )
            )
        }
        if (!comment.isNullOrEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                    withText(comment)
                )
            )
        }
    }
}
