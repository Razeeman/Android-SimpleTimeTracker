package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.typeTextIntoView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest : BaseUiTest() {

    @Test
    fun test() {
        val name = "Test"

        // Add activity
        clickOnView(withText(R.string.running_records_add_type))
        closeSoftKeyboard()
        pressBack()

        clickOnView(withText(R.string.running_records_add_type))
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        clickOnView(withText(R.string.change_record_type_save))

        // Start timer
        clickOnViewWithText(name)

        // Stop timer
        clickOnView(allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name)))

        // Records
        NavUtils.openRecordsScreen()

        longClickOnView(allOf(withText(name), isCompletelyDisplayed()))
        pressBack()

        clickOnViewWithId(R.id.btnRecordAdd)
        pressBack()

        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))
        clickOnViewWithText(R.string.change_record_save)

        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        clickOnViewWithId(R.id.btnRecordsContainerNext)

        // Statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(R.id.btnStatisticsChartFilter)
        pressBack()
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)

        // Settings
        NavUtils.openSettingsScreen()
    }
}
