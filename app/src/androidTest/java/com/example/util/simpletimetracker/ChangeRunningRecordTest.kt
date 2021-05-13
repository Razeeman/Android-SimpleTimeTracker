package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
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
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ChangeRunningRecordTest : BaseUiTest() {

    @Test
    fun changeRunningRecord() {
        val name = "Test1"
        val newName = "Test2"
        val firstGoalTime = TimeUnit.MINUTES.toSeconds(10)
        val comment = "comment"

        // Add activities
        testUtils.addActivity(name, firstColor, firstIcon, goalTime = firstGoalTime)
        testUtils.addActivity(newName, lastColor, emoji = lastEmoji)

        // Start timer
        tryAction { clickOnViewWithText(name) }
        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime
        var timeStarted = timeMapper.formatDateTime(timeStartedTimestamp, true)
        var timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(it, true) }

        checkRunningRecordDisplayed(
            name = name,
            color = firstColor,
            icon = firstIcon,
            timeStarted = timeStartedPreview,
            goalTime = "10m",
            comment = ""
        )

        // Open edit view
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRunningRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRunningRecordType))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRunningRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRunningRecordComment), withText("")))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText("goal 10m")))

        // Change item
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRunningRecordType, withText(newName))
        clickOnViewWithText(R.string.change_record_type_field)

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
            .perform(PickerActions.setTime(hourStarted, minutesStarted))
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
        checkPreviewUpdated(hasDescendant(withText(newName)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withText(lastEmoji)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(comment)))
        checkViewDoesNotExist(
            allOf(withId(R.id.previewChangeRunningRecord), hasDescendant(withSubstring("goal")))
        )

        // Save
        clickOnViewWithText(R.string.change_record_save)

        // Record updated
        checkViewDoesNotExist(
            allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))
        )
        checkRunningRecordDisplayed(
            name = newName,
            color = lastColor,
            emoji = lastEmoji,
            timeStarted = timeStartedPreview,
            goalTime = "",
            comment = comment
        )
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRunningRecord), matcher))

    private fun checkRunningRecordDisplayed(
        name: String,
        color: Int,
        icon: Int? = null,
        emoji: String? = null,
        timeStarted: String,
        goalTime: String,
        comment: String
    ) {
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))
        checkViewIsDisplayed(allOf(withId(R.id.viewRunningRecordItem), withCardColor(color)))
        if (icon != null) {
            checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withTag(icon)))
        }
        if (emoji != null) {
            checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(emoji)))
        }
        checkViewIsDisplayed(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(timeStarted)))
        if (goalTime.isNotEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(R.id.viewRunningRecordItem)),
                    withText("goal $goalTime")
                )
            )
        } else {
            checkViewDoesNotExist(
                allOf(
                    isDescendantOfA(withId(R.id.viewRunningRecordItem)),
                    withSubstring("goal")
                )
            )
        }
        if (comment.isNotEmpty()) {
            checkViewIsDisplayed(
                allOf(
                    isDescendantOfA(withId(R.id.viewRunningRecordItem)),
                    withText(comment)
                )
            )
        }
    }
}
