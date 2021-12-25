package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
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
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ChangeCategoryTest : BaseUiTest() {

    @Test
    fun changeCategory() {
        val name = "Test"
        val newName = "Updated"
        val typeName1 = "Type1"
        val typeName2 = "Type2"

        // Add activities
        testUtils.addActivity(typeName1)
        testUtils.addActivity(typeName2)

        // Add category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        NavUtils.addCategory(name, firstColor, listOf(typeName1))

        longClickOnView(withText(name))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeCategoryDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeCategoryType))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeCategoryName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))

        // Change item
        typeTextIntoView(R.id.etChangeCategoryName, newName)
        checkPreviewUpdated(hasDescendant(withText(newName)))

        clickOnViewWithText(R.string.change_record_type_color_hint)
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(firstColor))))
        scrollRecyclerToView(R.id.rvChangeCategoryColor, withCardColor(lastColor))
        clickOnRecyclerItem(R.id.rvChangeCategoryColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(lastColor))))

        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName1))
        clickOnRecyclerItem(R.id.rvChangeCategoryType, withText(typeName2))
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))

        clickOnViewWithText(R.string.change_record_type_save)

        // Record type updated
        checkViewIsDisplayed(withText(newName))
        checkViewIsDisplayed(withCardColor(lastColor))
        longClickOnView(withText(newName))
        clickOnViewWithText(R.string.change_category_types_hint)
        onView(withText(typeName1)).check(isCompletelyBelow(withId(R.id.viewDividerItem)))
        onView(withText(typeName2)).check(isCompletelyAbove(withId(R.id.viewDividerItem)))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeCategory), matcher))
}
