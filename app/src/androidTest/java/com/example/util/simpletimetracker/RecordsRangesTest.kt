package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.typeTextIntoView
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordsRangesTest : BaseUiTest() {

    @Test
    fun test() {
        val name = "Test"

        // Add activity
        NavUtils.openRunningRecordsScreen()
        clickOnView(withText(R.string.running_records_add_type))
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        pressBack()
        clickOnView(withText(R.string.change_record_type_save))

        // Start timer
        clickOnViewWithText(name)
        clickOnView(allOf(isDescendantOfA(withId(R.id.layoutRunningRecordItem)), withText(name)))

        // Records
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(R.string.records_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(R.string.records_empty), isCompletelyDisplayed()))

        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.records_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.records_empty), isCompletelyDisplayed()))
    }
}
