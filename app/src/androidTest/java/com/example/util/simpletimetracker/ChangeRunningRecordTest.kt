package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

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
        testUtils.addActivity(name = name2, color = lastColor, emoji = lastEmoji)
        testUtils.addRecordTag(tag2, name2)

        // Start timer
        tryAction { clickOnViewWithText(name1) }
        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime
        var timeStarted = timeMapper.formatDateTime(timeStartedTimestamp, true)
        var timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(it, true) }

        checkRunningRecordDisplayed(
            name = name1,
            color = firstColor,
            icon = firstIcon,
            timeStarted = timeStartedPreview,
            goalTime = "10$minuteString",
            comment = ""
        )

        // Open edit view
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name1)))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRunningRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRunningRecordType))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRunningRecordCategories))
        checkViewIsNotDisplayed(withId(R.id.containerChangeRunningRecordTimeAdjust))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRunningRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRunningRecordComment), withText("")))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name1)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(
            hasDescendant(withText(getString(R.string.running_record_goal_time, "10$minuteString")))
        )

        // Change item
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordType, withText(name2))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordCategories, withText(tag2))
        clickOnViewWithText(R.string.change_record_tag_field)

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val hourStarted = 0
        val minutesStarted = 0
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        clickOnViewWithId(R.id.tvChangeRunningRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithText(R.string.date_time_dialog_date)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(year, month + 1, day))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        timeStartedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourStarted)
            set(Calendar.MINUTE, minutesStarted)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp
            .let { timeMapper.formatDateTime(it, true) }
        timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(it, true) }

        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRunningRecordTimeStarted), withText(timeStarted)))
        typeTextIntoView(R.id.etChangeRunningRecordComment, comment)

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(fullName2)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withText(lastEmoji)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(comment)))
        checkViewIsNotDisplayed(
            allOf(isDescendantOfA(withId(R.id.previewChangeRunningRecord)), withId(R.id.tvRunningRecordItemGoalTime))
        )

        // Save
        clickOnViewWithText(R.string.change_record_save)

        // Record updated
        tryAction {
            checkViewDoesNotExist(
                allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name1))
            )
        }
        checkRunningRecordDisplayed(
            name = fullName2,
            color = lastColor,
            emoji = lastEmoji,
            timeStarted = timeStartedPreview,
            goalTime = "",
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

        // Add running record
        tryAction { clickOnViewWithText(name1) }

        // Record is added
        checkRunningRecordDisplayed(name = name1)

        // Change tag
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name1)))
        checkPreviewUpdated(hasDescendant(withText(name1)))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordCategories, withText(tag1))
        checkPreviewUpdated(hasDescendant(withText(fullName1)))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnViewWithText(R.string.change_record_save)

        // Record updated
        tryAction { checkRunningRecordDisplayed(name = fullName1) }

        // Change activity and tag
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(fullName1)))
        checkPreviewUpdated(hasDescendant(withText(fullName1)))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordType, withText(name2))
        checkPreviewUpdated(hasDescendant(withText(name2)))
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordCategories, withText(tag2))
        clickOnRecyclerItem(R.id.rvChangeRunningRecordCategories, withText(tag3))
        checkPreviewUpdated(hasDescendant(withText(fullName2)))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnViewWithText(R.string.change_record_save)

        // Record updated
        tryAction { checkRunningRecordDisplayed(name = fullName2) }

        // Remove tag
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(fullName2)))
        checkPreviewUpdated(hasDescendant(withText(fullName2)))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordCategories, withText(R.string.change_record_untagged))
        checkPreviewUpdated(hasDescendant(withText(name2)))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnViewWithText(R.string.change_record_save)

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
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))
        clickOnViewWithId(R.id.tvChangeRunningRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkAfterTimeAdjustment(timeStarted = "00:00")

        // Check visibility
        checkViewIsNotDisplayed(withId(R.id.containerChangeRunningRecordTimeAdjust))
        clickOnViewWithId(R.id.btnChangeRunningRecordTimeStartedAdjust)
        checkViewIsDisplayed(withId(R.id.containerChangeRunningRecordTimeAdjust))
        clickOnViewWithId(R.id.btnChangeRunningRecordTimeStartedAdjust)
        checkViewIsNotDisplayed(withId(R.id.containerChangeRunningRecordTimeAdjust))

        // Check time adjustments
        clickOnViewWithId(R.id.btnChangeRunningRecordTimeStartedAdjust)

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
        clickOnViewWithText(R.string.time_now)
        checkPreviewUpdated(hasDescendant(allOf(withId(R.id.tvRunningRecordItemTimer), withText("0$secondString"))))
        clickOnViewWithText("+30")
        checkPreviewUpdated(hasDescendant(allOf(withId(R.id.tvRunningRecordItemTimer), withText("0$secondString"))))
    }

    private fun checkAfterTimeAdjustment(timeStarted: String) {
        checkPreviewUpdated(hasDescendant(allOf(withId(R.id.tvRunningRecordItemTimeStarted), withText(timeStarted))))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRunningRecordTimeStarted), withSubstring(timeStarted)))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRunningRecord), matcher))

    private fun checkRunningRecordDisplayed(
        name: String,
        color: Int? = null,
        icon: Int? = null,
        emoji: String? = null,
        timeStarted: String? = null,
        goalTime: String? = null,
        comment: String? = null,
    ) {
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))

        if (color != null) {
            checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), withCardColor(color)))
        }
        if (icon != null) {
            checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withTag(icon)))
        }
        if (emoji != null) {
            checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(emoji)))
        }
        if (timeStarted != null) {
            checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(timeStarted)))
        }
        if (!goalTime.isNullOrEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(R.id.viewRunningRecordItem)),
                    withText(getString(R.string.running_record_goal_time, goalTime))
                )
            )
        } else {
            checkViewIsNotDisplayed(
                allOf(
                    isDescendantOfA(withId(R.id.viewRunningRecordItem)),
                    withId(R.id.tvRunningRecordItemGoalTime)
                )
            )
        }
        if (!comment.isNullOrEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(R.id.viewRunningRecordItem)),
                    withText(comment)
                )
            )
        }
    }
}
