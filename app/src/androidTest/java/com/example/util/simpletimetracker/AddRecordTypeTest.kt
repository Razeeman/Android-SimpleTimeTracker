package com.example.util.simpletimetracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.ui.MainActivity
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AddRecordTypeTest {

    @Inject
    lateinit var testUtils: TestUtils

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
        val name = "Test"
        val firstColor = ColorMapper.availableColors[0]
        val lastColor = ColorMapper.availableColors.let { it[it.size - 1] }

        NavUtils.openRunningRecordsScreen()
        onView(withText(R.string.running_records_add_type)).perform(click())

        // Choosers are empty
        onView(withId(R.id.rvChangeRecordTypeColor))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.rvChangeRecordTypeIcon))
            .check(matches(not(isDisplayed())))

        // Typing name
        onView(withId(R.id.etChangeRecordTypeName))
            .perform(typeText(name))
        onView(allOf(isDescendantOfA(withId(R.id.previewChangeRecordType)), withText(name)))
            .check(matches(isDisplayed()))

        // Hide keyboard
        pressBack()

        // Open color chooser
        onView(withText(R.string.change_record_type_color_hint))
            .perform(click())
        onView(withId(R.id.rvChangeRecordTypeColor))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rvChangeRecordTypeIcon))
            .check(matches(not(isDisplayed())))

        // Selecting color
        onView(withCardColor(firstColor)).perform(click())
        onView(
            allOf(isDescendantOfA(withId(R.id.previewChangeRecordType)), withCardColor(firstColor))
        ).check(matches(isDisplayed()))
        onView(withCardColor(lastColor)).perform(click())
        onView(
            allOf(isDescendantOfA(withId(R.id.previewChangeRecordType)), withCardColor(lastColor))
        ).check(matches(isDisplayed()))

        // Open icon chooser
        onView(withText(R.string.change_record_type_icon_hint))
            .perform(click())
        onView(withId(R.id.rvChangeRecordTypeColor))
            .check(matches(not(isDisplayed())))
        onView(withId(R.id.rvChangeRecordTypeIcon))
            .check(matches(isDisplayed()))

        // TODO select icon

        onView(withText(R.string.change_record_type_save))
            .perform(click())
    }
}
