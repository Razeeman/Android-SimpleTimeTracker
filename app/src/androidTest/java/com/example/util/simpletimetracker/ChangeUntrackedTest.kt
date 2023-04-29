package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_views.R as viewsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeUntrackedTest : BaseUiTest() {

    @Test
    fun changeUntracked() {
        val name = "Test"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Open edit view
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))

        // View is set up
        checkViewIsNotDisplayed(withId(changeRecordR.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(changeRecordR.id.rvChangeRecordType))
        checkPreviewUpdated(withCardColor(viewsR.color.colorUntracked))

        // Change item
        clickOnViewWithText(coreR.string.change_record_type_field)
        clickOnRecyclerItem(changeRecordR.id.rvChangeRecordType, withText(name))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(color))
        checkPreviewUpdated(hasDescendant(withTag(icon)))

        // Save
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record updated
        tryAction { checkViewDoesNotExist(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordItem),
                withCardColor(color),
                hasDescendant(withText(name)),
                hasDescendant(withTag(icon)),
                isCompletelyDisplayed()
            )
        )

        // Delete record
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(changeRecordR.id.btnChangeRecordDelete)

        // Untracked is back
        tryAction { checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed())) }
        checkViewIsDisplayed(allOf(withText(coreR.string.untracked_time_name), isCompletelyDisplayed()))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordR.id.previewChangeRecord), matcher))
}
