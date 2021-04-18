package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteRecordTest : BaseUiTest() {

    @Test
    fun deleteRecord() {
        val name = "Name"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconImageMapper.availableIconsNames.values.first()

        // Add activity
        testUtils.addActivity(name, color, icon)

        // Add record
        NavUtils.openRecordsScreen()
        NavUtils.addRecord(name)

        // Delete item
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(withId(R.id.btnChangeRecordDelete))
        clickOnViewWithId(R.id.btnChangeRecordDelete)

        // Check message
        checkViewIsDisplayed(
            allOf(
                withText("Record Name removed"),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )

        // Record is deleted
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withCardColor(color), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withTag(icon), isCompletelyDisplayed()))

        // Check undo
        clickOnViewWithText(R.string.record_removed_undo)

        // Record is back
        checkViewIsDisplayed(
            CoreMatchers.allOf(
                withId(R.id.viewRecordItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                isCompletelyDisplayed()
            )
        )
    }
}
