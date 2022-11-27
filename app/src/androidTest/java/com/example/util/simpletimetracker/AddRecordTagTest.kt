package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
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
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddRecordTagTest : BaseUiTest() {

    @Test
    fun addRecordTag() {
        val name = "Test"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(name = typeName, color = firstColor, icon = firstIcon)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(R.string.categories_record_type_hint))
        clickOnViewWithText(R.string.categories_add_record_tag)
        closeSoftKeyboard()
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.buttonsChangeRecordTagType)),
                withText(R.string.change_record_tag_type_typed)
            )
        )

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(R.id.fieldChangeRecordTagColor))
        checkViewIsDisplayed(allOf(withId(R.id.fieldChangeRecordTagType), withCardColor(R.color.colorBackground)))

        // Name is not selected
        clickOnViewWithText(R.string.change_category_save)

        // Typing name
        typeTextIntoView(R.id.etChangeRecordTagName, name)
        tryAction { checkPreviewUpdated(hasDescendant(withText(name))) }

        // Open activity chooser
        clickOnViewWithId(R.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsDisplayed(allOf(withId(R.id.fieldChangeRecordTagType), withCardColor(R.color.inputFieldBorder)))

        // Selecting activity
        clickOnRecyclerItem(R.id.rvChangeRecordTagType, withText(typeName))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        clickOnViewWithId(R.id.fieldChangeRecordTagType)
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
    }

    @Test
    fun addRecordGeneralTag() {
        val name = "Test"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(name = typeName, color = firstColor, icon = firstIcon)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(R.string.categories_record_type_hint))
        clickOnViewWithText(R.string.categories_add_record_tag)
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(R.id.fieldChangeRecordTagType))
        checkViewIsDisplayed(allOf(withId(R.id.fieldChangeRecordTagColor), withCardColor(R.color.colorBackground)))

        // Name is not selected
        clickOnViewWithText(R.string.change_category_save)

        // Typing name
        typeTextIntoView(R.id.etChangeRecordTagName, name)
        tryAction { checkPreviewUpdated(hasDescendant(withText(name))) }

        // Open color chooser
        clickOnViewWithId(R.id.fieldChangeRecordTagColor)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTagColor))
        checkViewIsDisplayed(allOf(withId(R.id.fieldChangeRecordTagColor), withCardColor(R.color.inputFieldBorder)))

        // Selecting color
        clickOnRecyclerItem(R.id.rvChangeRecordTagColor, withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColor(lastColor))))
        clickOnViewWithId(R.id.fieldChangeRecordTagColor)
        checkViewIsDisplayed(
            allOf(withId(R.id.fieldChangeRecordTagColor), withCardColor(R.color.colorBackground))
        )

        clickOnViewWithText(R.string.change_record_type_save)

        // Tag added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))

        // Check tag saved
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRecordTagName), withText(name)))
    }

    @Test
    fun addRecordTagTypesEmpty() {
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(R.string.categories_add_record_tag)

        // Open activity chooser
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.buttonsChangeRecordTagType)),
                withText(R.string.change_record_tag_type_typed)
            )
        )
        clickOnViewWithId(R.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(withText(R.string.record_types_empty))
    }

    @Test
    fun addRecordTagSameName() {
        val typeName = "typeName"
        val tagNameActivity = "tagNameActivity"
        val tagNameGeneral = "tagNameGeneral"

        // Add activities
        testUtils.addActivity(name = typeName, color = firstColor, icon = firstIcon)
        testUtils.addRecordTag(tagNameActivity, typeName)
        testUtils.addRecordTag(tagNameGeneral)

        // Check items
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        onView(withId(R.id.rvCategoriesList)).check(recyclerItemCount(7))

        // Add another tag
        clickOnViewWithText(R.string.categories_add_record_tag)
        typeTextIntoView(R.id.etChangeRecordTagName, tagNameActivity)
        clickOnView(
            allOf(
                isDescendantOfA(withId(R.id.buttonsChangeRecordTagType)),
                withText(R.string.change_record_tag_type_typed)
            )
        )
        clickOnViewWithId(R.id.fieldChangeRecordTagType)
        clickOnRecyclerItem(R.id.rvChangeRecordTagType, withText(typeName))
        clickOnViewWithText(R.string.change_record_type_save)

        onView(withId(R.id.rvCategoriesList)).check(recyclerItemCount(7))

        // Add another general tag
        clickOnViewWithText(R.string.categories_add_record_tag)
        typeTextIntoView(R.id.etChangeRecordTagName, tagNameGeneral)
        clickOnViewWithText(R.string.change_record_type_save)

        onView(withId(R.id.rvCategoriesList)).check(recyclerItemCount(7))
    }

    @Test
    fun addRecordTagFromChangeRecord() {
        val tagName1 = "Tag1"
        val tagName2 = "Tag2"
        val typeName = "Type"

        // Add data
        testUtils.addActivity(name = typeName, color = lastColor, icon = lastIcon)
        testUtils.addRecord(typeName)

        // Add tag
        NavUtils.openRecordsScreen()
        clickOnView(allOf(withText(typeName), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.fieldChangeRecordCategory)
        clickOnViewWithText(R.string.categories_add_record_tag)

        // Activity already selected
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        typeTextIntoView(R.id.etChangeRecordTagName, tagName1)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_category_save)

        // Tag added
        checkViewIsDisplayed(withText(tagName1))

        // Change tag
        longClickOnView(withText(tagName1))
        typeTextIntoView(R.id.etChangeRecordTagName, tagName2)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_category_save)

        // Tag changed
        checkViewDoesNotExist(withText(tagName1))
        checkViewIsDisplayed(withText(tagName2))
    }

    @Test
    fun addRecordTagFromChangeRunningRecord() {
        val tagName1 = "Tag1"
        val tagName2 = "Tag2"
        val typeName = "Type"

        // Add data
        testUtils.addActivity(typeName)
        tryAction { clickOnViewWithText(typeName) }
        longClickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(typeName)))

        // Add category
        clickOnViewWithId(R.id.fieldChangeRecordCategory)
        clickOnViewWithText(R.string.categories_add_record_tag)
        typeTextIntoView(R.id.etChangeRecordTagName, tagName1)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_category_save)

        // Category added
        checkViewIsDisplayed(withText(tagName1))

        // Change category
        longClickOnView(withText(tagName1))
        typeTextIntoView(R.id.etChangeRecordTagName, tagName2)
        closeSoftKeyboard()
        clickOnViewWithText(R.string.change_category_save)

        // Category changed
        checkViewDoesNotExist(withText(tagName1))
        checkViewIsDisplayed(withText(tagName2))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordTag), matcher))
}
