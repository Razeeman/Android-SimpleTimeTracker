package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
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
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangeUntrackedTest : BaseUiTest() {

    @Test
    fun test() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        // Add activity
        NavUtils.addActivity(name, color, icon)

        // Open edit view
        NavUtils.openRecordsScreen()
        longClickOnView(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordType))
        checkPreviewUpdated(withCardColor(R.color.colorUntracked))

        // Change item
        clickOnViewWithText(R.string.change_record_type_field)
        clickOnRecyclerItem(R.id.rvChangeRecordType, withText(name))

        // Preview is updated
        checkPreviewUpdated(withText(name))
        checkPreviewUpdated(withCardColor(color))
        checkPreviewUpdated(withTag(icon))

        // Save
        clickOnViewWithText(R.string.change_record_type_save)

        // Record updated
        checkViewDoesNotExist(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                ViewMatchers.hasDescendant(withText(name)),
                ViewMatchers.hasDescendant(withCardColor(color)),
                ViewMatchers.hasDescendant(withTag(icon)),
                isCompletelyDisplayed()
            )
        )

        // Delete record
        longClickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnChangeRecordDelete)

        // Untracked is back
        checkViewDoesNotExist(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.previewChangeRecord)), matcher)
        )
}
