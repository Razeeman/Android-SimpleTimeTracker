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
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_categories.R as categoriesR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_views.R as viewsR

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
        checkViewIsDisplayed(withText(coreR.string.categories_record_type_hint))
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTagR.id.buttonsChangeRecordTagType)),
                withText(coreR.string.change_record_tag_type_typed)
            )
        )

        // View is set up
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.fieldChangeRecordTagColor))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagType), withCardColor(viewsR.color.colorBackground))
        )

        // Name is not selected
        clickOnViewWithText(coreR.string.change_category_save)

        // Typing name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)
        tryAction { checkPreviewUpdated(hasDescendant(withText(name))) }

        // Open activity chooser
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagType), withCardColor(viewsR.color.inputFieldBorder))
        )

        // Selecting activity
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagType), withCardColor(viewsR.color.colorBackground))
        )

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Tag added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(firstColor))

        // Check tag saved
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.etChangeRecordTagName), withText(name)))
    }

    @Test
    fun addRecordGeneralTag() {
        val name = "Test"
        val typeName = "Type"

        // Add activities
        testUtils.addActivity(name = typeName, color = firstColor, icon = firstIcon)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(coreR.string.categories_record_type_hint))
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.fieldChangeRecordTagType))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagColor), withCardColor(viewsR.color.colorBackground))
        )

        // Name is not selected
        clickOnViewWithText(coreR.string.change_category_save)

        // Typing name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)
        tryAction { checkPreviewUpdated(hasDescendant(withText(name))) }

        // Open color chooser
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagColor)
        checkViewIsDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagColor), withCardColor(viewsR.color.inputFieldBorder))
        )

        // Selecting color
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.viewColorItemSelected), withParent(withCardColor(lastColor)))
        )
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagColor)
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagColor), withCardColor(viewsR.color.colorBackground))
        )

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Tag added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))

        // Check tag saved
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.etChangeRecordTagName), withText(name)))
    }

    @Test
    fun addRecordTagTypesEmpty() {
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_record_tag)

        // Open activity chooser
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTagR.id.buttonsChangeRecordTagType)),
                withText(coreR.string.change_record_tag_type_typed)
            )
        )
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(withText(coreR.string.record_types_empty))
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
        onView(withId(categoriesR.id.rvCategoriesList)).check(recyclerItemCount(7))

        // Add another tag
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagNameActivity)
        clickOnView(
            allOf(
                isDescendantOfA(withId(changeRecordTagR.id.buttonsChangeRecordTagType)),
                withText(coreR.string.change_record_tag_type_typed)
            )
        )
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName))
        clickOnViewWithText(coreR.string.change_record_type_save)

        onView(withId(categoriesR.id.rvCategoriesList)).check(recyclerItemCount(7))

        // Add another general tag
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagNameGeneral)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_type_save)

        onView(withId(categoriesR.id.rvCategoriesList)).check(recyclerItemCount(7))
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
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordCategory)
        clickOnViewWithText(coreR.string.categories_add_record_tag)

        // Activity already selected
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagName1)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_save)

        // Tag added
        checkViewIsDisplayed(withText(tagName1))

        // Change tag
        longClickOnView(withText(tagName1))
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagName2)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_save)

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
        longClickOnView(allOf(isDescendantOfA(withId(changeRecordTagR.id.viewRunningRecordItem)), withText(typeName)))

        // Add category
        clickOnViewWithId(changeRecordR.id.fieldChangeRecordCategory)
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagName1)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_save)

        // Category added
        checkViewIsDisplayed(withText(tagName1))

        // Change category
        longClickOnView(withText(tagName1))
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagName2)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_category_save)

        // Category changed
        checkViewDoesNotExist(withText(tagName1))
        checkViewIsDisplayed(withText(tagName2))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.previewChangeRecordTag), matcher))
}
