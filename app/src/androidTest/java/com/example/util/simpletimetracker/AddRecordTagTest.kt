package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.collapseToolbar
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.recyclerItemCount
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
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
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_categories.R as categoriesR
import com.example.util.simpletimetracker.feature_change_record.R as changeRecordR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_views.R as viewsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddRecordTagTest : BaseUiTest() {

    @Test
    fun addRecordTag() {
        val name = "Test"
        val typeName1 = "Type1"
        val typeName2 = "Type2"
        val lastColorPosition = ColorMapper.getAvailableColors().size - 1

        // Add activities
        testUtils.addActivity(name = typeName1, color = firstColor, icon = firstIcon)
        testUtils.addActivity(name = typeName2, color = lastColor, icon = lastIcon)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(withText(coreR.string.categories_record_type_hint))
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()

        // View is set up
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.btnChangeRecordTagDelete))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.btnChangeRecordTagStatistics))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvIconSelection))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagDefaultType))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagColor), withCardColor(viewsR.color.colorBackground)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagIcon), withCardColor(viewsR.color.colorBackground)),
        )
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagType), withCardColor(viewsR.color.colorBackground)),
        )
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTagR.id.fieldChangeRecordTagDefaultType),
                withCardColor(viewsR.color.colorBackground),
            ),
        )

        // Name is not selected
        clickOnViewWithText(coreR.string.change_category_save)

        // Typing name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)
        tryAction { checkPreviewUpdated(hasDescendant(withText(name))) }

        // Open color chooser
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagColor), withCardColor(viewsR.color.inputFieldBorder)),
        )
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvIconSelection))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagDefaultType))

        // Selecting color
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.viewColorItemSelected), withParent(withCardColor(firstColor))),
        )

        // Selecting color
        scrollRecyclerToPosition(changeRecordTagR.id.rvChangeRecordTagColor, lastColorPosition)
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.viewColorItemSelected), withParent(withCardColor(lastColor))),
        )

        // Close color selection
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagColor), withCardColor(viewsR.color.colorBackground)),
        )

        // Open icon chooser
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        checkViewIsDisplayed(withId(changeRecordTagR.id.rvIconSelection))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagIcon), withCardColor(viewsR.color.inputFieldBorder)),
        )
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagDefaultType))

        // Selecting icon
        clickOnRecyclerItem(changeRecordTagR.id.rvIconSelection, withTag(firstIcon))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Selecting icon
        onView(withId(changeRecordTagR.id.rvIconSelection)).perform(collapseToolbar())
        scrollRecyclerToView(changeRecordTagR.id.rvIconSelection, hasDescendant(withTag(lastIcon)))
        clickOnRecyclerItem(changeRecordTagR.id.rvIconSelection, withTag(lastIcon))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        // Close icon selection
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagIcon)
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagIcon), withCardColor(viewsR.color.colorBackground)),
        )

        // Open activity chooser
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagType), withCardColor(viewsR.color.inputFieldBorder)),
        )
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvIconSelection))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagDefaultType))

        // Tag type hint
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.tvChangeRecordTagTypesPreview))
        checkViewIsDisplayed(withText(R.string.change_record_tag_type_general_hint))

        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName1))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.tvChangeRecordTagTypesPreview), withText("1")))
        checkViewIsDisplayed(withText(R.string.change_record_tag_type_typed_hint))

        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName2))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.tvChangeRecordTagTypesPreview), withText("2")))
        checkViewIsDisplayed(withText(R.string.change_record_tag_type_typed_hint))

        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName1))
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName2))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.tvChangeRecordTagTypesPreview))
        checkViewIsDisplayed(withText(R.string.change_record_tag_type_general_hint))

        // Selecting activity
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName1))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(lastColor))
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(
            allOf(withId(changeRecordTagR.id.fieldChangeRecordTagType), withCardColor(viewsR.color.colorBackground)),
        )

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Tag added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))

        // Check tag saved
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(lastColor))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))
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
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()

        // Typing name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)
        tryAction { checkPreviewUpdated(hasDescendant(withText(name))) }

        clickOnViewWithText(coreR.string.change_record_type_save)

        // Tag added
        checkViewIsDisplayed(withText(name))

        // Check tag saved
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.etChangeRecordTagName), withText(name)))
    }

    @Test
    fun addRecordTagDefaultTypes() {
        val name = "Test"
        val typeName1 = "Type1"
        val typeName2 = "Type2"

        // Add activities
        testUtils.addActivity(name = typeName1)
        testUtils.addActivity(name = typeName2)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()

        // Typing name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)

        // Open activity chooser
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagDefaultType)
        checkViewIsDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagDefaultType))
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTagR.id.fieldChangeRecordTagDefaultType),
                withCardColor(viewsR.color.inputFieldBorder),
            ),
        )
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvIconSelection))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagColor))
        checkViewIsNotDisplayed(withId(changeRecordTagR.id.rvChangeRecordTagType))

        checkViewIsNotDisplayed(withId(changeRecordTagR.id.tvChangeRecordTagDefaultTypePreview))
        checkViewIsDisplayed(withText(R.string.change_record_tag_default_types_hint))

        // Select
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagDefaultType, withText(typeName1))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.tvChangeRecordTagDefaultTypePreview), withText("1")))

        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagDefaultType, withText(typeName2))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.tvChangeRecordTagDefaultTypePreview), withText("2")))

        // Save
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check tag saved
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.tvChangeRecordTagDefaultTypePreview), withText("2")))
    }

    @Test
    fun addRecordTagColorSource() {
        val name = "Test"
        val typeName1 = "Type1"

        // Add activities
        testUtils.addActivity(name = typeName1, color = lastColor, icon = lastIcon)

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()

        // Typing name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)

        // Select color and icon
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnRecyclerItem(changeRecordTagR.id.rvIconSelection, withTag(firstIcon))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))
        pressBack()
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))
        pressBack()

        // Select from activity
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagSelectActivity)
        clickOnViewWithText(typeName1)
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))
        checkPreviewUpdated(withCardColor(lastColor))

        // Save
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check saved
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTag(lastIcon))
        pressBack()

        // Change activity
        val newColor = ColorMapper.getAvailableColors()[1]
        val newIconCategory = iconImageMapper.getAvailableCategories(hasFavourites = false).first()
        val newIcon = iconImageMapper.getAvailableImages(loadSearchHints = false)[newIconCategory]
            .orEmpty()[1].iconResId

        NavUtils.openRunningRecordsScreen()
        longClickOnView(withText(typeName1))
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnRecyclerItem(changeRecordTagR.id.rvIconSelection, withTag(newIcon))
        pressBack()
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColor(newColor))
        pressBack()
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check new color and icon
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()

        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(newColor))
        checkViewIsDisplayed(withTag(newIcon))

        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withText(name)))
        checkPreviewUpdated(withCardColor(newColor))
        checkPreviewUpdated(hasDescendant(withTag(newIcon)))
    }

    @Test
    fun addRecordTagNoIcon() {
        val name = "Test"

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        closeSoftKeyboard()

        // Typing name
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, name)

        // Select icon
        clickOnViewWithText(coreR.string.change_record_type_icon_image_hint)
        clickOnRecyclerItem(changeRecordTagR.id.rvIconSelection, withTag(firstIcon))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Select no icon
        clickOnViewWithText(R.string.change_record_type_no_icon)
        checkViewIsNotDisplayed(
            allOf(
                isDescendantOfA(withId(changeRecordTagR.id.previewChangeRecordTag)),
                withId(coreR.id.ivCategoryItemIcon),
            ),
        )

        // Save
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Check saved
        checkViewIsNotDisplayed(
            allOf(
                isDescendantOfA(withId(coreR.id.viewCategoryItem)),
                withId(coreR.id.ivCategoryItemIcon),
                hasSibling(withText(name)),
            ),
        )
    }

    @Test
    fun addRecordTagTypesEmpty() {
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_record_tag)

        // Open activity chooser
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        checkViewIsDisplayed(allOf(withText(coreR.string.record_types_empty), isCompletelyDisplayed()))
        pressBack()

        // Open activity chooser
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagDefaultType)
        checkViewIsDisplayed(allOf(withText(coreR.string.record_types_empty), isCompletelyDisplayed()))
        pressBack()

        // Color source
        clickOnViewWithId(changeRecordTagR.id.btnChangeRecordTagSelectActivity)
        checkViewIsDisplayed(allOf(withText(coreR.string.record_types_empty), isCompletelyDisplayed()))
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
        clickOnViewWithId(changeRecordTagR.id.fieldChangeRecordTagType)
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagType, withText(typeName))
        clickOnViewWithText(coreR.string.change_record_type_save)

        onView(withId(categoriesR.id.rvCategoriesList)).check(recyclerItemCount(8))

        // Add another general tag
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagNameGeneral)
        closeSoftKeyboard()
        clickOnViewWithText(coreR.string.change_record_type_save)

        onView(withId(categoriesR.id.rvCategoriesList)).check(recyclerItemCount(9))
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
        Thread.sleep(1000)
        tryAction { clickOnViewWithText(typeName) }
        longClickOnView(allOf(isDescendantOfA(withId(baseR.id.viewRunningRecordItem)), withText(typeName)))

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
