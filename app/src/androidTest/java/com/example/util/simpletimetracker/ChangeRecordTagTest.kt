package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangeRecordTagTest : BaseUiTest() {

    @Test
    fun changeRecordTag() {
        val name = "Test"
        val newName = "Updated"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(typeName, firstColor, firstIcon)

        // Add category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addRecordTag(name, typeName)

        clickOnViewWithText(name)

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withText(R.string.change_record_type_field))
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

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordTag), matcher))
}
