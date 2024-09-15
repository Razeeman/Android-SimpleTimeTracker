package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DeleteRecordTagTest : BaseUiTest() {

    @Test
    fun quickDelete() {
        val type = "Type"
        val tag = "Tag"

        // Add data
        testUtils.addActivity(type)
        testUtils.addRecordTag(tag)
        testUtils.addRecord(type, tagNames = listOf(tag))

        // Check
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(withText("$type - $tag"))

        // Delete
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        longClickOnView(withText(tag))
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagDelete)
        clickOnViewWithText(coreR.string.ok)
        checkViewDoesNotExist(withText(tag))
        pressBack()

        // Check
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(type), isCompletelyDisplayed()))
        checkViewDoesNotExist(withText("$type - $tag"))
    }
}
