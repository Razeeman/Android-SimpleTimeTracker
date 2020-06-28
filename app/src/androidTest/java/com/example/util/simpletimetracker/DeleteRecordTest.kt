package com.example.util.simpletimetracker

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
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
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class DeleteRecordTest {

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
    fun deleteTest() {
        val name = "Name"
        val color = ColorMapper.availableColors.first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(R.string.running_records_add_type)
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        Espresso.pressBack()
        clickOnViewWithText(R.string.change_record_type_color_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(color))
        clickOnViewWithText(R.string.change_record_type_icon_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTagValue(equalTo(icon)))
        clickOnViewWithText(R.string.change_record_type_save)

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(R.string.change_record_save)

        // Delete item
        longClickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(withId(R.id.btnChangeRecordDelete))
        clickOnViewWithId(R.id.btnChangeRecordDelete)

        // TODO check message

        // Record is deleted
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withCardColor(color), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withTagValue(equalTo(icon)), isCompletelyDisplayed()))
    }
}
