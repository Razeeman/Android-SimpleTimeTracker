package com.example.util.simpletimetracker

import android.view.View
import android.widget.TimePicker
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
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
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
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
class AddRecordTest {

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
    fun addTest() {
        val name = "Name"
        val color = ColorMapper.availableColors.first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(R.string.running_records_add_type)
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        pressBack()
        clickOnViewWithText(R.string.change_record_type_color_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(color))
        clickOnViewWithText(R.string.change_record_type_icon_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTagValue(equalTo(icon)))
        clickOnViewWithText(R.string.change_record_type_save)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordType))
        val currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatDateTime(currentTime - 60 * 60 * 1000)
        var timeEnded = timeMapper.formatDateTime(currentTime)
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
        timeStarted = timeStartedTimestamp.let(timeMapper::formatDateTime)
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
        timeEnded = timeEndedTimestamp.let(timeMapper::formatDateTime)
        checkViewIsDisplayed(allOf(withId(R.id.tvChangeRecordTimeEnded), withText(timeEnded)))

        // Preview is updated
        val timeStartedPreview = timeStartedTimestamp.let(timeMapper::formatTime)
        val timeEndedPreview = timeStartedTimestamp.let(timeMapper::formatTime)
        checkPreviewUpdated(withText(timeStartedPreview))
        checkPreviewUpdated(withText(timeEndedPreview))
        checkPreviewUpdated(withText("2h 3m"))

        // Open activity chooser
        clickOnViewWithText(R.string.change_record_type_field)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordType))

        // Selecting activity
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        checkPreviewUpdated(withText(name))
        checkPreviewUpdated(withCardColor(color))
        checkPreviewUpdated(withTagValue(equalTo(icon)))

        clickOnViewWithText(R.string.change_record_save)

        // Record added
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withCardColor(color), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withTagValue(equalTo(icon)), isCompletelyDisplayed()))

        checkViewIsDisplayed(allOf(withText(timeStartedPreview), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(timeEndedPreview), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText("2h 3m"), isCompletelyDisplayed()))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.previewChangeRecord)), matcher)
        )
}
