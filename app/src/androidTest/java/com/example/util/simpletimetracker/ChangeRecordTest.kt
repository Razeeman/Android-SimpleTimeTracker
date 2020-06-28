package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.ui.MainActivity
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ChangeRecordTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Inject
    lateinit var iconMapper: IconMapper

    @Inject
    lateinit var timeMapper: TimeMapper

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext() as TimeTrackerApp
        DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .build()
            .inject(this)

        testUtils.clearDatabase()
    }

    @Test
    fun test() {
        val name = "Test1"
        val newName = "Test2"
        val firstColor = ColorMapper.availableColors.first()
        val lastColor = ColorMapper.availableColors.last()
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()

        // Add activities
        NavUtils.addActivity(name, firstColor, firstIcon)
        NavUtils.addActivity(newName, lastColor, lastIcon)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        val currentTime = System.currentTimeMillis()
        var timeStartedTimestamp = currentTime - 60 * 60 * 1000
        var timeEndedTimestamp = currentTime
        var timeStarted = timeMapper.formatDateTime(timeStartedTimestamp)
        var timeEnded = timeMapper.formatDateTime(timeEndedTimestamp)
        var timeStartedPreview = timeStartedTimestamp
            .let(timeMapper::formatTime)
        var timeEndedPreview = timeStartedTimestamp
            .let(timeMapper::formatTime)
        var timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp)
            .let(timeMapper::formatInterval)

        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(R.string.change_record_save)

        // Open edit view
        longClickOnView(allOf(withText(name), isCompletelyDisplayed()))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordType))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        // Preview is updated
        checkPreviewUpdated(withText(name))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(withTag(firstIcon))
        checkPreviewUpdated(withText(timeStartedPreview))
        checkPreviewUpdated(withText(timeEndedPreview))
        checkPreviewUpdated(withText(timeRangePreview))

        // Change item
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(newName))

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
        timeStarted = timeStartedTimestamp.let(timeMapper::formatDateTime)
        timeEnded = timeEndedTimestamp.let(timeMapper::formatDateTime)
        timeStartedPreview = timeStartedTimestamp
            .let(timeMapper::formatTime)
        timeEndedPreview = timeStartedTimestamp
            .let(timeMapper::formatTime)
        timeRangePreview = (timeEndedTimestamp - timeStartedTimestamp)
            .let(timeMapper::formatInterval)

        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        // Preview is updated
        checkPreviewUpdated(withText(newName))
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(withTag(lastIcon))
        checkPreviewUpdated(withText(timeStartedPreview))
        checkPreviewUpdated(withText(timeEndedPreview))
        checkPreviewUpdated(withText(timeRangePreview))

        clickOnViewWithText(R.string.change_record_type_save)

        // Record updated
        checkViewDoesNotExist(allOf(withText(newName), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(newName), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withCardColor(lastColor), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withTag(lastIcon), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(timeStartedPreview), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(timeEndedPreview), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(timeRangePreview), isCompletelyDisplayed()))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.previewChangeRecord)), matcher)
        )
}
