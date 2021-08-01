package com.example.util.simpletimetracker

import android.view.View
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
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
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
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconImageMapper.availableIconsNames.values.first()
        val comment = "Comment"
        val tag = "Tag"

        // Add activity
        testUtils.addActivity(name, color, icon)
        testUtils.addRecordTag(name, tag)

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
            .perform(PickerActions.setTime(hourStarted, minutesStarted))
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
            .perform(PickerActions.setTime(hourEnded, minutesEnded))
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

        // Selecting tag
        clickOnRecyclerItem(R.id.rvChangeRecordCategories, withText(tag))
        checkPreviewUpdated(hasDescendant(withText("$name - $tag")))
        clickOnViewWithId(R.id.fieldChangeRecordCategory)

        clickOnViewWithText(R.string.change_record_save)

        // Record added
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                withCardColor(color),
                hasDescendant(withText("$name - $tag")),
                hasDescendant(withTag(icon)),
                hasDescendant(withText(timeStartedPreview)),
                hasDescendant(withText(timeEndedPreview)),
                hasDescendant(withText("2$hourString 3$minuteString")),
                hasDescendant(withText(comment)),
                isCompletelyDisplayed()
            )
        )
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

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecord), matcher))
}
