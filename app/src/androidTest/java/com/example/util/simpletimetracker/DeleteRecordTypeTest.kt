package com.example.util.simpletimetracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.ui.MainActivity
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class DeleteRecordTypeTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Inject
    lateinit var iconMapper: IconMapper

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
        val name = "Test"
        val color = ColorMapper.availableColors.first()
        val icon = iconMapper.availableIconsNames.values.first()

        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(R.string.running_records_add_type)
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordTypeDelete))
        pressBack()
        pressBack()

        // Add item
        NavUtils.addActivity(name, color, icon)

        // Delete item
        longClickOnView(withText(name))
        checkViewIsDisplayed(withId(R.id.btnChangeRecordTypeDelete))
        clickOnViewWithId(R.id.btnChangeRecordTypeDelete)

        // TODO check message

        // Record type is deleted
        checkViewDoesNotExist(withText(name))
        checkViewDoesNotExist(withCardColor(color))
        checkViewDoesNotExist(withTagValue(equalTo(icon)))
    }
}
