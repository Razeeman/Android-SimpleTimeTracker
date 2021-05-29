package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddRecordTagTest : BaseUiTest() {

    @Test
    fun addRecordTag() {
        val name = "Test"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(typeName, firstColor, firstIcon)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(R.string.categories_record_type_hint))
        clickOnViewWithText(R.string.categories_add_record_tag)
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsDisplayed(
            allOf(withId(R.id.fieldChangeRecordTagType), withCardColor(R.color.colorBackground))
        )

        // Name is not selected
        clickOnViewWithText(R.string.change_category_save)

        // Typing name
        typeTextIntoView(
            R.id.etChangeRecordTagName, name
        )
        tryAction { checkPreviewUpdated(hasDescendant(withText(name))) }

        // Activity is not selected
        clickOnViewWithText(R.string.change_category_save)

        // Open activity chooser
        clickOnViewWithText(R.string.change_record_type_field)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsDisplayed(
            allOf(withId(R.id.fieldChangeRecordTagType), withCardColor(R.color.inputFieldBorder))
        )

        // Selecting activity
        clickOnRecyclerItem(R.id.rvChangeRecordTagType, withText(typeName))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        clickOnViewWithText(R.string.change_record_type_field)
        checkViewIsDisplayed(
            allOf(withId(R.id.fieldChangeRecordTagType), withCardColor(R.color.colorBackground))
        )

        clickOnViewWithText(R.string.change_record_type_save)

        // Tag added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(firstColor))

        // Check tag saved
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRecordTagName), withText(name)))
        checkViewIsNotDisplayed(withText(R.string.change_record_type_field))
    }

    @Test
    fun addRecordTagTypesEmpty() {
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(R.string.categories_add_record_tag)

        // Open activity chooser
        clickOnViewWithText(R.string.change_record_type_field)
        checkViewIsDisplayed(withText(R.string.record_types_empty))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordTag), matcher))
}
