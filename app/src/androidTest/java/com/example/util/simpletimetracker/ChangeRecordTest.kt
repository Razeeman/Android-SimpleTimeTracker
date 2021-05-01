package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class ChangeRecordTest : BaseUiTest() {

    @Test
    fun changeRecord() {
        val name = "Test1"
        val newName = "Test2"
        val comment = "comment"
        val newComment = "new comment"

        // Add activities
        testUtils.addActivity(name, firstColor, firstIcon)
        testUtils.addActivity(newName, lastColor, emoji = lastEmoji)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime - 60 * 60 * 1000
        var timeEndedTimestamp = currentTime
        var timeStarted = timeMapper.formatDateTime(timeStartedTimestamp, true)
        var timeEnded = timeMapper.formatDateTime(timeEndedTimestamp, true)
        var timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(it, true) }
        var timeEndedPreview = timeEndedTimestamp
            .let { timeMapper.formatTime(it, true) }
        var timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp)
            .let(timeMapper::formatInterval)

        typeTextIntoView(R.id.etChangeRecordComment, comment)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(R.string.change_record_save)

        // Open edit view
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordType))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRecordComment), withText(comment)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeEndedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeRangePreview)))
        checkPreviewUpdated(hasDescendant(withText(comment)))

        // Change item
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(newName))
        clickOnViewWithText(R.string.change_record_type_field)

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val hourStarted = 15
        val minutesStarted = 16
        val hourEnded = 17
        val minutesEnded = 19
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        clickOnViewWithId(R.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(hourStarted, minutesStarted))
        clickOnViewWithText(R.string.date_time_dialog_date)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(year, month + 1, day))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        clickOnViewWithId(R.id.tvChangeRecordTimeEnded)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(PickerActions.setTime(hourEnded, minutesEnded))
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
        timeEndedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourEnded)
            set(Calendar.MINUTE, minutesEnded)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp
            .let { timeMapper.formatDateTime(it, true) }
        timeEnded = timeEndedTimestamp
            .let { timeMapper.formatDateTime(it, true) }
        timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(it, true) }
        timeEndedPreview = timeEndedTimestamp
            .let { timeMapper.formatTime(it, true) }
        timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp)
            .let(timeMapper::formatInterval)

        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        typeTextIntoView(R.id.etChangeRecordComment, newComment)
        closeSoftKeyboard()

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(newName)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withText(lastEmoji)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeEndedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeRangePreview)))
        checkPreviewUpdated(hasDescendant(withText(newComment)))

        clickOnViewWithText(R.string.change_record_type_save)

        // Record updated
        checkViewDoesNotExist(allOf(withText(newName), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                withCardColor(lastColor),
                hasDescendant(withText(newName)),
                hasDescendant(withText(lastEmoji)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                hasDescendant(withText(timeRangePreview)),
                hasDescendant(withText(newComment)),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), matcher))
}
