package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.domain.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.getMillis
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import com.google.android.material.R
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_running_record.R as changeRunningRecordR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_statistics_detail.R as statisticsDetailR

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
        testUtils.addActivity(
            name = name1,
            color = firstColor,
            icon = firstIcon,
            goals = listOf(
                RecordTypeGoal(
                    idData = RecordTypeGoal.IdData.Type(0),
                    range = RecordTypeGoal.Range.Session,
                    type = RecordTypeGoal.Type.Duration(firstGoalTime),
                    daysOfWeek = emptyList(),
                ),
            ),
        )
        testUtils.addActivity(name = name2, color = lastColor, text = lastEmoji)
        testUtils.addRecordTag(tag2, name2)

        // Start timer
        tryAction { clickOnViewWithText(name1) }
        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime
        var timeStarted = timeMapper.getFormattedDateTime(
            time = timeStartedTimestamp, useMilitaryTime = true, showSeconds = false,
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
            comment = "",
        )

        // Open edit view
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(name1)))

        // View is set up
        checkViewIsDisplayed(withId(changeRunningRecordR.id.btnChangeRecordDelete))
        checkViewIsDisplayed(withId(changeRunningRecordR.id.btnChangeRecordStatistics))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordType))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordCategories))
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeStartedAdjust))
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordTimeStartedPrev))
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordTimeStartedNext))
        checkViewIsNotDisplayed(withId(changeRecordR.id.containerChangeRecordTimeEndedAdjust))
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordTimeEndedPrev))
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordTimeEndedNext))
        checkViewIsNotDisplayed(allOf(withId(changeRecordR.id.etChangeRecordComment), withText("")))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedDate), withText(timeStarted.date)))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedTime), withText(timeStarted.time)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name1)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withSubstring(goalString)))

        // Check statistics navigation
        clickOnViewWithId(changeRunningRecordR.id.btnChangeRecordStatistics)
        checkViewIsDisplayed(
            allOf(
                withId(statisticsDetailR.id.viewStatisticsDetailItem),
                hasDescendant(withText(name1)),
            ),
        )
        pressBack()

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

        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeStarted)
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
            .let { timeMapper.getFormattedDateTime(time = it, useMilitaryTime = true, showSeconds = false) }
        timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false) }

        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedDate), withText(timeStarted.date)))
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedTime), withText(timeStarted.time)))
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
                withId(changeRecordR.id.tvRunningRecordItemGoalTime),
            ),
        )

        // Save
        clickOnViewWithText(coreR.string.change_record_save)

        // Record updated
        tryAction {
            checkViewDoesNotExist(
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(name1)),
            )
        }
        checkRunningRecordDisplayed(
            name = fullName2,
            color = lastColor,
            text = lastEmoji,
            timeStarted = timeStartedPreview,
            comment = comment,
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
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordTimeStarted)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name))).perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkAfterTimeAdjustment(timeStarted = "00:00")

        // Check visibility
        checkViewIsDisplayed(withId(changeRecordR.id.containerChangeRecordTimeStartedAdjust))
        checkViewIsNotDisplayed(withId(changeRecordR.id.containerChangeRecordTimeEndedAdjust))

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
            hasDescendant(allOf(withId(changeRecordR.id.tvRunningRecordItemTimer), withText("0$secondString"))),
        )
        clickOnViewWithText("+30")
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRunningRecordItemTimer), withText("0$secondString"))),
        )
    }

    @Test
    fun changeRunningRecordPrevNext() {
        // Add data
        val type1 = "type1"
        val type2 = "type2"
        val calendar = Calendar.getInstance()
            .apply { add(Calendar.DATE, -1) }

        testUtils.addActivity(type1)
        testUtils.addActivity(type2)

        testUtils.addRecord(
            typeName = type1,
            timeStarted = calendar.getMillis(hour = 10),
            timeEnded = calendar.getMillis(hour = 11),
        )
        testUtils.addRecord(
            typeName = type1,
            timeStarted = calendar.getMillis(hour = 12),
            timeEnded = calendar.getMillis(hour = 13),
        )
        testUtils.addRecord(
            typeName = type1,
            timeStarted = calendar.getMillis(hour = 14),
            timeEnded = calendar.getMillis(hour = 15),
        )

        Thread.sleep(1000)
        tryAction { clickOnViewWithText(type2) }
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(type2)))

        // Check visibility
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordTimeStartedPrev))
        checkViewIsDisplayed(withId(changeRecordR.id.btnChangeRecordTimeStartedNext))
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordTimeEndedPrev))
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordTimeEndedNext))

        fun checkTimes(started: Int) {
            checkAfterTimeAdjustment(calendar.getMillis(started).formatTime())
        }

        // Check times
        clickOnViewWithId(changeRecordR.id.btnChangeRecordTimeStartedPrev)
        checkTimes(15)
        clickOnViewWithId(changeRecordR.id.btnChangeRecordTimeStartedPrev)
        checkTimes(13)
        clickOnViewWithId(changeRecordR.id.btnChangeRecordTimeStartedPrev)
        checkTimes(11)
        clickOnViewWithId(changeRecordR.id.btnChangeRecordTimeStartedPrev)
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.change_record_previous_not_found),
                withId(R.id.snackbar_text),
            ),
        )

        clickOnViewWithId(changeRecordR.id.btnChangeRecordTimeStartedNext)
        checkTimes(13)
        clickOnViewWithId(changeRecordR.id.btnChangeRecordTimeStartedNext)
        checkTimes(15)
        clickOnViewWithId(changeRecordR.id.btnChangeRecordTimeStartedNext)
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.change_record_next_not_found),
                withId(R.id.snackbar_text),
            ),
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
            allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(nameNoComments)),
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
    fun dayTotal() {
        val name1 = "name1"
        val name2 = "name2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        val time = calendar.apply {
            timeInMillis = System.currentTimeMillis()
            setToStartOfDay()
            set(Calendar.HOUR_OF_DAY, 12)
        }.timeInMillis
        testUtils.addRecord(
            typeName = name1,
            timeStarted = time,
            timeEnded = time + TimeUnit.HOURS.toMillis(1),
        )

        // Check
        val day = getString(coreR.string.title_today).lowercase()
        val dayTotal = "$day 1$hourString 0$minuteString"

        tryAction { clickOnViewWithText(name1) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(changeRecordR.id.viewRunningRecordItem),
                    hasDescendant(withText(name1)),
                    hasDescendant(withText(dayTotal)),
                ),
            )
        }

        tryAction { clickOnViewWithText(name2) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(changeRecordR.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                ),
            )
        }
        tryAction {
            checkViewDoesNotExist(
                allOf(
                    withId(changeRecordR.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                    hasDescendant(withSubstring(day)),
                ),
            )
        }
    }

    private fun checkAfterTimeAdjustment(timeStarted: String) {
        checkPreviewUpdated(
            hasDescendant(allOf(withId(changeRecordR.id.tvRunningRecordItemTimeStarted), withText(timeStarted))),
        )
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.tvChangeRecordTimeStartedTime), withSubstring(timeStarted)))
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
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withTag(icon)),
            )
        }
        if (text != null) {
            checkViewIsDisplayed(
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(text)),
            )
        }
        if (timeStarted != null) {
            checkViewIsDisplayed(
                allOf(isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)), withText(timeStarted)),
            )
        }
        if (!goalTime.isNullOrEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                    withSubstring(goalTime),
                ),
            )
        } else {
            checkViewIsNotDisplayed(
                allOf(
                    isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                    withId(changeRecordR.id.tvRunningRecordItemGoalTime),
                ),
            )
        }
        if (!comment.isNullOrEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(changeRecordR.id.viewRunningRecordItem)),
                    withText(comment),
                ),
            )
        }
    }
}
