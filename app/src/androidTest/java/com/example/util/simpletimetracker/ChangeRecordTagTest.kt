package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeRecordTagTest : BaseUiTest() {

    @Test
    fun changeRecordTag() {
        val name = "Test"
        val newName = "Updated"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(name = typeName, color = firstColor, icon = firstIcon)

        // Add category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addRecordTag(name, activity = typeName)

        clickOnViewWithText(name)

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(R.id.buttonsChangeRecordTagType))
        checkViewIsNotDisplayed(withId(R.id.fieldChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(R.id.fieldChangeRecordTagType))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRecordTagName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))

        // Change item
        typeTextIntoView(R.id.etChangeRecordTagName, newName)
        tryAction { checkPreviewUpdated(hasDescendant(withText(newName))) }

        clickOnViewWithText(R.string.change_record_type_save)

        // Record tag updated
        checkViewIsDisplayed(withText(newName))
    }

    @Test
    fun changeRecordGeneralTag() {
        val name = "Test"
        val newName = "Updated"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(name = typeName, color = firstColor, icon = firstIcon)

        // Add category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addRecordTag(name, color = firstColor)

        clickOnViewWithText(name)

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(R.id.buttonsChangeRecordTagType))
        checkViewIsDisplayed(withId(R.id.fieldChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(R.id.fieldChangeRecordTagType))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRecordTagName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))

        // Change item name
        typeTextIntoView(R.id.etChangeRecordTagName, newName)
        tryAction { checkPreviewUpdated(hasDescendant(withText(newName))) }

        // Change item color
        clickOnViewWithId(R.id.fieldChangeRecordTagColor)
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(firstColor))))
        scrollRecyclerToView(R.id.rvChangeRecordTagColor, withCardColor(lastColor))
        clickOnRecyclerItem(R.id.rvChangeRecordTagColor, withCardColor(lastColor))
        tryAction { checkPreviewUpdated(withCardColor(lastColor)) }
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(lastColor))))

        clickOnViewWithText(R.string.change_record_type_save)

        // Record tag updated
        checkViewIsDisplayed(withText(newName))
        checkViewIsDisplayed(withCardColor(lastColor))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordTag), matcher))
}
