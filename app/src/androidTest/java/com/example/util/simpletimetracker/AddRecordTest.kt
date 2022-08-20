package com.example.util.simpletimetracker

import android.view.View
import android.widget.TimePicker
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
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
class AddRecordTest : BaseUiTest() {

    @Test
    fun addRecord() {
        val name = "Name"
        val color = firstColor
        val icon = firstIcon
        val comment = "Comment"
        val tag1 = "Tag1"
        val tag2 = "Tag2"

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)
        testUtils.addRecordTag(tag1, name)
        testUtils.addRecordTag(tag2)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordType))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordCategories))
        val currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatDateTime(currentTime - 60 * 60 * 1000, true)
        var timeEnded = timeMapper.formatDateTime(currentTime, true)
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        // Set time started
        val hourStarted = 15
        val minutesStarted = 16
        clickOnViewWithId(R.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        val timeStartedTimestamp = Calendar.getInstance().run {
            set(Calendar.HOUR_OF_DAY, hourStarted)
            set(Calendar.MINUTE, minutesStarted)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp.let { timeMapper.formatDateTime(it, true) }
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))

        // Set time ended
        val hourEnded = 17
        val minutesEnded = 19
        clickOnViewWithId(R.id.tvChangeRecordTimeEnded)
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        val timeEndedTimestamp = Calendar.getInstance().run {
            set(Calendar.HOUR_OF_DAY, hourEnded)
            set(Calendar.MINUTE, minutesEnded)
            timeInMillis
        }
        timeEnded = timeEndedTimestamp.let { timeMapper.formatDateTime(it, true) }
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        // Preview is updated
        val timeStartedPreview = timeStartedTimestamp.let { timeMapper.formatTime(it, true) }
        val timeEndedPreview = timeEndedTimestamp.let { timeMapper.formatTime(it, true) }
        checkPreviewUpdated(hasDescendant(withText(timeStartedPreview)))
        checkPreviewUpdated(hasDescendant(withText(timeEndedPreview)))
        checkPreviewUpdated(hasDescendant(withText("2$hourString 3$minuteString")))

        // Activity not selected
        clickOnViewWithText(R.string.change_record_save)

        // Open activity chooser
        clickOnViewWithText(R.string.change_record_type_field)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordType))

        // Selecting activity
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(color))
        checkPreviewUpdated(hasDescendant(withTag(icon)))
        clickOnViewWithText(R.string.change_record_type_field)

        // Set comment
        typeTextIntoView(R.id.etChangeRecordComment, comment)
        closeSoftKeyboard()
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment))) }

        // Open tag chooser
        clickOnViewWithId(R.id.fieldChangeRecordCategory)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordCategories))

        // Selecting tags
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag1))
        checkPreviewUpdated(hasDescendant(withText("$name - $tag1")))

        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag2))
        checkPreviewUpdated(hasDescendant(withText("$name - $tag1, $tag2")))

        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag1))
        checkPreviewUpdated(hasDescendant(withText("$name - $tag2")))

        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag2))
        checkPreviewUpdated(hasDescendant(withText(name)))

        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag1))
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag2))
        clickOnViewWithId(R.id.fieldChangeRecordCategory)

        clickOnViewWithText(R.string.change_record_save)

        // Record added
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRecordItem),
                    withCardColor(color),
                    hasDescendant(withText("$name - $tag1, $tag2")),
                    hasDescendant(withTag(icon)),
                    hasDescendant(withText(timeStartedPreview)),
                    hasDescendant(withText(timeEndedPreview)),
                    hasDescendant(withText("2$hourString 3$minuteString")),
                    hasDescendant(withText(comment)),
                    isCompletelyDisplayed()
                )
            )
        }
    }

    @Test
    fun addRecordTypesEmpty() {
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        // Open activity chooser
        clickOnViewWithText(R.string.change_record_type_field)
        checkViewIsDisplayed(withText(R.string.record_types_empty))
    }

    @Test
    fun addRecordTagsEmpty() {
        val name = "name"
        testUtils.addActivity(name)

        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        // Select activity
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))

        // Open tag chooser
        clickOnViewWithId(R.id.fieldChangeRecordCategory)
        checkViewIsDisplayed(withText(R.string.change_record_categories_empty))
    }

    @Test
    fun addRecordComment() {
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

        // Check comments
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        // No last comments
        checkViewIsNotDisplayed(withText(R.string.change_record_last_comments_hint))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordLastComments))
        checkViewDoesNotExist(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))

        // Select activity with no previous comments
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(nameNoComments))
        clickOnViewWithText(R.string.change_record_type_field)

        // Still no last comments
        checkViewIsNotDisplayed(withText(R.string.change_record_last_comments_hint))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordLastComments))
        checkViewDoesNotExist(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))

        // Select activity with one previous comment
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(nameComment))
        clickOnViewWithText(R.string.change_record_type_field)

        // One last comment
        checkViewIsDisplayed(withText(R.string.change_record_last_comments_hint))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordLastComments))
        checkViewDoesNotExist(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))

        clickOnViewWithText(R.string.change_record_last_comments_hint)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordLastComments))
        checkViewIsDisplayed(withText(comment1))
        checkViewDoesNotExist(withText(comment2))
        checkViewDoesNotExist(withText(comment3))

        // Select last comment
        clickOnViewWithText(comment1)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment1))) }
        typeTextIntoView(R.id.etChangeRecordComment, "")

        // Select activity with many previous comments
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(nameComments))
        clickOnViewWithText(R.string.change_record_type_field)

        // Two last comments
        checkViewIsDisplayed(withId(R.id.fieldChangeRecordLastComments))
        checkViewIsDisplayed(withId(R.id.rvChangeRecordLastComments))
        checkViewDoesNotExist(withText(comment1))
        checkViewIsDisplayed(withText(comment2))
        checkViewIsDisplayed(withText(comment3))

        clickOnViewWithId(R.id.fieldChangeRecordLastComments)
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordLastComments))
        checkViewDoesNotExist(withText(comment1))
        checkViewIsNotDisplayed(withText(comment2))
        checkViewIsNotDisplayed(withText(comment3))

        clickOnViewWithId(R.id.fieldChangeRecordLastComments)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordLastComments))
        checkViewDoesNotExist(withText(comment1))
        checkViewIsDisplayed(withText(comment2))
        checkViewIsDisplayed(withText(comment3))

        // Select last comment
        clickOnViewWithText(comment2)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment2))) }
        clickOnViewWithText(comment3)
        tryAction { checkPreviewUpdated(hasDescendant(withText(comment3))) }
    }

    @Test
    fun addRecordAdjustTime() {
        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        // Setup
        val hourStarted = 15
        val minutesStarted = 0
        clickOnViewWithId(R.id.tvChangeRecordTimeStarted)
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        val hourEnded = 16
        val minutesEnded = 0
        clickOnViewWithId(R.id.tvChangeRecordTimeEnded)
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkAfterTimeAdjustment(
            timeStarted = "15:00", timeEnded = "16:00", duration = "1$hourString 0$minuteString"
        )

        // Check visibility
        checkViewIsNotDisplayed(withId(R.id.containerChangeRecordTimeAdjust))
        clickOnViewWithId(R.id.btnChangeRecordTimeStartedAdjust)
        checkViewIsDisplayed(withId(R.id.containerChangeRecordTimeAdjust))
        clickOnViewWithId(R.id.btnChangeRecordTimeStartedAdjust)
        checkViewIsNotDisplayed(withId(R.id.containerChangeRecordTimeAdjust))

        // Check time start adjustments
        clickOnViewWithId(R.id.btnChangeRecordTimeStartedAdjust)

        clickOnViewWithText("-30")
        checkAfterTimeAdjustment(
            timeStarted = "14:30", timeEnded = "16:00", duration = "1$hourString 30$minuteString"
        )
        clickOnViewWithText("-5")
        checkAfterTimeAdjustment(
            timeStarted = "14:25", timeEnded = "16:00", duration = "1$hourString 35$minuteString"
        )
        clickOnViewWithText("-1")
        checkAfterTimeAdjustment(
            timeStarted = "14:24", timeEnded = "16:00", duration = "1$hourString 36$minuteString"
        )
        clickOnViewWithText("+1")
        checkAfterTimeAdjustment(
            timeStarted = "14:25", timeEnded = "16:00", duration = "1$hourString 35$minuteString"
        )
        clickOnViewWithText("+5")
        checkAfterTimeAdjustment(
            timeStarted = "14:30", timeEnded = "16:00", duration = "1$hourString 30$minuteString"
        )
        clickOnViewWithText("+30")
        checkAfterTimeAdjustment(
            timeStarted = "15:00", timeEnded = "16:00", duration = "1$hourString 0$minuteString"
        )
        clickOnViewWithText("+30")
        clickOnViewWithText("+30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "16:00", duration = "0$secondString"
        )

        // Check time end adjustments
        clickOnViewWithId(R.id.btnChangeRecordTimeEndedAdjust)

        tryAction { clickOnViewWithText("+30") }
        clickOnViewWithText("+30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:00", duration = "1$hourString 0$minuteString"
        )
        clickOnViewWithText("+5")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:05", duration = "1$hourString 5$minuteString"
        )
        clickOnViewWithText("+1")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:06", duration = "1$hourString 6$minuteString"
        )
        clickOnViewWithText("-1")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:05", duration = "1$hourString 5$minuteString"
        )
        clickOnViewWithText("-5")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "17:00", duration = "1$hourString 0$minuteString"
        )
        clickOnViewWithText("-30")
        checkAfterTimeAdjustment(
            timeStarted = "16:00", timeEnded = "16:30", duration = "30$minuteString"
        )
        clickOnViewWithText("-30")
        clickOnViewWithText("-30")
        checkAfterTimeAdjustment(
            timeStarted = "15:30", timeEnded = "15:30", duration = "0$secondString"
        )
    }

    private fun checkAfterTimeAdjustment(
        timeStarted: String,
        timeEnded: String,
        duration: String,
    ) {
        checkPreviewUpdated(hasDescendant(allOf(withId(R.id.tvRecordItemTimeStarted), withText(timeStarted))))
        checkPreviewUpdated(hasDescendant(allOf(withId(R.id.tvRecordItemTimeFinished), withText(timeEnded))))
        checkPreviewUpdated(hasDescendant(allOf(withId(R.id.tvRecordItemDuration), withText(duration))))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withSubstring(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withSubstring(timeEnded)))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), matcher))
}
