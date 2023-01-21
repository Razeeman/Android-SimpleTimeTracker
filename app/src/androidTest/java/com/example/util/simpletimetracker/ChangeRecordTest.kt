package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_dialogs.dateTime.CustomTimePicker
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeRecordTest : BaseUiTest() {

    @Test
    fun changeRecord() {
        val name = "Test1"
        val newName = "Test2"
        val comment = "comment"
        val newComment = "new comment"
        val tag1 = "tag1"
        val tag2 = "tag2"
        val fullName1 = "$name - $tag1"
        val fullName2 = "$newName - $tag2"

        // Add activities
        testUtils.addActivity(name = name, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name = newName, color = lastColor, text = lastEmoji)
        testUtils.addRecordTag(tag1, name)
        testUtils.addRecordTag(tag2, newName)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime - 60 * 60 * 1000
        var timeEndedTimestamp = currentTime
        var timeStarted = timeMapper.formatDateTime(
            time = timeStartedTimestamp, useMilitaryTime = true, showSeconds = false
        )
        var timeEnded = timeMapper.formatDateTime(
            time = timeEndedTimestamp, useMilitaryTime = true, showSeconds = false
        )
        var timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false) }
        var timeEndedPreview = timeEndedTimestamp
            .let { timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false) }
        var timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp)
            .let { timeMapper.formatInterval(interval = it, forceSeconds = false, useProportionalMinutes = false) }

        clickOnViewWithText(R.string.change_record_comment_field)
        typeTextIntoView(R.id.etChangeRecordComment, comment)
        clickOnViewWithText(R.string.change_record_comment_field)
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag1))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnViewWithText(R.string.change_record_save)
        checkViewIsDisplayed(allOf(withText(fullName1), isCompletelyDisplayed()))

        // Open edit view
        clickOnView(withText(fullName1))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordType))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordCategories))
        checkViewIsNotDisplayed(allOf(withId(R.id.etChangeRecordComment), withText(comment)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(fullName1)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeEndedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeRangePreview)))
        checkPreviewUpdated(hasDescendant(withText(comment)))

        // Change item
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(newName))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag2))
        clickOnViewWithText(R.string.change_record_tag_field)

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
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
            .perform(PickerActions.setTime(hourStarted, minutesStarted))
        clickOnViewWithText(R.string.date_time_dialog_date)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(PickerActions.setDate(year, month + 1, day))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        clickOnViewWithId(R.id.tvChangeRecordTimeEnded)
        onView(withClassName(equalTo(CustomTimePicker::class.java.name)))
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
            .let { timeMapper.formatDateTime(time = it, useMilitaryTime = true, showSeconds = false) }
        timeEnded = timeEndedTimestamp
            .let { timeMapper.formatDateTime(time = it, useMilitaryTime = true, showSeconds = false) }
        timeStartedPreview = timeStartedTimestamp
            .let { timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false) }
        timeEndedPreview = timeEndedTimestamp
            .let { timeMapper.formatTime(time = it, useMilitaryTime = true, showSeconds = false) }
        timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp)
            .let { timeMapper.formatInterval(interval = it, forceSeconds = false, useProportionalMinutes = false) }

        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        clickOnViewWithText(R.string.change_record_comment_field)
        typeTextIntoView(R.id.etChangeRecordComment, newComment)
        clickOnViewWithText(R.string.change_record_comment_field)

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(fullName2)))
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
                hasDescendant(withText(fullName2)),
                hasDescendant(withText(lastEmoji)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                hasDescendant(withText(timeRangePreview)),
                hasDescendant(withText(newComment)),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun changeRecordUntagged() {
        val name = "TypeName"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val fullName = "$name - $tag1, $tag2"

        // Add activities
        testUtils.addActivity(name = name, color = firstColor, icon = firstIcon)
        testUtils.addRecordTag(tag1, name)
        testUtils.addRecordTag(tag2)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(R.string.change_record_save)

        // Record is added
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.viewRecordItem), hasDescendant(withText(name)))) }

        // Change tag
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkPreviewUpdated(hasDescendant(withText(name)))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag1))
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag2))
        clickOnViewWithText(R.string.change_record_tag_field)
        checkPreviewUpdated(hasDescendant(withText(fullName)))
        clickOnViewWithText(R.string.change_record_type_save)

        // Record updated
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.viewRecordItem), hasDescendant(withText(fullName)))) }

        // Remove tag
        clickOnView(allOf(withText(fullName)))
        checkPreviewUpdated(hasDescendant(withText(fullName)))
        clickOnViewWithText(R.string.change_record_tag_field)
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(R.string.change_record_untagged))
        clickOnViewWithText(R.string.change_record_tag_field)
        checkPreviewUpdated(hasDescendant(withText(name)))
        clickOnViewWithText(R.string.change_record_type_save)

        // Record updated
        tryAction { checkViewIsDisplayed(allOf(withId(R.id.viewRecordItem), hasDescendant(withText(name)))) }
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), matcher))
}
