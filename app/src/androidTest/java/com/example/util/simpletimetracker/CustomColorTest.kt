package com.example.util.simpletimetracker

import android.view.View
import androidx.annotation.ColorInt
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkSliderValue
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickLocation
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColorInt
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_category.R as changeCategoryR
import com.example.util.simpletimetracker.feature_change_record_tag.R as changeRecordTagR
import com.example.util.simpletimetracker.feature_change_activity_filter.R as changeActivityFilterTagR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CustomColorTest : BaseUiTest() {

    @Test
    fun colorSelectionDialog() {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Open color selection dialog
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(baseR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(baseR.id.layoutColorPaletteItem))

        checkViewIsDisplayed(withText(coreR.string.color_selection_base_color_hint))
        checkViewIsDisplayed(withText(coreR.string.color_selection_adjust_color_hint))
        checkViewIsDisplayed(withText(coreR.string.color_selection_final_color_hint))

        // Change slider hue
        onView(withId(dialogsR.id.sliderColorSelectionHue)).perform(clickLocation(GeneralLocation.CENTER_LEFT))
        checkSliderValue(dialogsR.id.sliderColorSelectionHue, 0)
        checkViewIsDisplayed(allOf(withId(dialogsR.id.etColorSelectionHue), withText("0")))

        onView(withId(dialogsR.id.sliderColorSelectionHue)).perform(clickLocation(GeneralLocation.CENTER_RIGHT))
        checkSliderValue(dialogsR.id.sliderColorSelectionHue, 360)
        checkViewIsDisplayed(allOf(withId(dialogsR.id.etColorSelectionHue), withText("360")))

        // Change hex
        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#FF0000")
        checkColorState(
            finalColorInt = 0xffff0000.toInt(),
            colorRed = 255,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#00FF00")
        checkColorState(
            finalColorInt = 0xff00ff00.toInt(),
            colorGreen = 255,
            colorHue = 120,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#0000FF")
        checkColorState(
            finalColorInt = 0xff0000ff.toInt(),
            colorBlue = 255,
            colorHue = 240,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#29a674")
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )

        // Change RGB
        typeTextIntoView(dialogsR.id.etColorSelectionRed, "255")
        typeTextIntoView(dialogsR.id.etColorSelectionGreen, "0")
        typeTextIntoView(dialogsR.id.etColorSelectionBlue, "0")
        checkColorState(
            finalColorInt = 0xffff0000.toInt(),
            colorRed = 255,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionRed, "0")
        typeTextIntoView(dialogsR.id.etColorSelectionGreen, "255")
        typeTextIntoView(dialogsR.id.etColorSelectionBlue, "0")
        checkColorState(
            finalColorInt = 0xff00ff00.toInt(),
            colorGreen = 255,
            colorHue = 120,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionRed, "0")
        typeTextIntoView(dialogsR.id.etColorSelectionGreen, "0")
        typeTextIntoView(dialogsR.id.etColorSelectionBlue, "255")
        checkColorState(
            finalColorInt = 0xff0000ff.toInt(),
            colorBlue = 255,
            colorHue = 240,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionRed, "41")
        typeTextIntoView(dialogsR.id.etColorSelectionGreen, "166")
        typeTextIntoView(dialogsR.id.etColorSelectionBlue, "116")
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionRed, "999")
        typeTextIntoView(dialogsR.id.etColorSelectionGreen, "999")
        typeTextIntoView(dialogsR.id.etColorSelectionBlue, "999")
        checkColorState(
            finalColorInt = 0xffffffff.toInt(),
            colorRed = 255,
            colorGreen = 255,
            colorBlue = 255,
            colorHue = 0,
            colorSaturation = 0,
            colorValue = 100,
        )

        // Change HSV
        typeTextIntoView(dialogsR.id.etColorSelectionHue, "0")
        typeTextIntoView(dialogsR.id.etColorSelectionSaturation, "100")
        typeTextIntoView(dialogsR.id.etColorSelectionValue, "100")
        checkColorState(
            finalColorInt = 0xffff0000.toInt(),
            colorRed = 255,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionHue, "120")
        checkColorState(
            finalColorInt = 0xff00ff00.toInt(),
            colorGreen = 255,
            colorHue = 120,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionHue, "240")
        checkColorState(
            finalColorInt = 0xff0000ff.toInt(),
            colorBlue = 255,
            colorHue = 240,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionHue, "156")
        typeTextIntoView(dialogsR.id.etColorSelectionSaturation, "75")
        typeTextIntoView(dialogsR.id.etColorSelectionValue, "65")
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )

        typeTextIntoView(dialogsR.id.etColorSelectionHue, "999")
        typeTextIntoView(dialogsR.id.etColorSelectionSaturation, "999")
        typeTextIntoView(dialogsR.id.etColorSelectionValue, "999")
        checkColorState(
            finalColorInt = 0xffff0000.toInt(),
            colorRed = 255,
            colorGreen = 0,
            colorBlue = 0,
            colorHue = 360,
            colorSaturation = 100,
            colorValue = 100,
        )

        // Check random
        clickOnViewWithId(dialogsR.id.btnColorSelectionRandom)
        clickOnViewWithId(dialogsR.id.btnColorSelectionRandom)
        clickOnViewWithId(dialogsR.id.btnColorSelectionRandom)
    }

    @Test
    fun colorTransferRecordType() {
        fun checkPreviewUpdated(matcher: Matcher<View>) =
            checkViewIsDisplayed(allOf(withId(changeRecordTypeR.id.previewChangeRecordType), matcher))

        val name = "name"
        val tagName = "tag"
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red
        val customColorInt = 0xff29a674.toInt()

        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }

        // Select color
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withCardColorInt(colorInt))
        checkPreviewUpdated(hasDescendant(withCardColorInt(colorInt)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))

        // Check selected color is preselected on color selection
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(dialogsR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(dialogsR.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xfff53639.toInt(),
            colorRed = 245,
            colorGreen = 54,
            colorBlue = 57,
            colorHue = 359,
            colorSaturation = 77,
            colorValue = 96,
        )

        // Select different color
        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Check new color selected
        checkPreviewUpdated(hasDescendant(withCardColorInt(customColorInt)))
        checkViewIsNotDisplayed(
            allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))),
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )

        // Save record type
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, name)
        clickOnViewWithText(coreR.string.change_record_type_save)

        // Record type saved
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewRecordTypeItem),
                hasDescendant(withCardColorInt(customColorInt)),
                hasDescendant(withText(name)),
            ),
        )
        longClickOnView(withText(name))
        checkPreviewUpdated(hasDescendant(withCardColorInt(customColorInt)))
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(dialogsR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(dialogsR.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )
        pressBack() // Close custom color
        pressBack() // Close color dropdown
        pressBack() // Close edit screen

        // Check record
        testUtils.addRecord(name)
        testUtils.addRecordTag(tagName = tagName, typeName = name)
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewRecordItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )

        // Check statistics
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewStatisticsItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(name)),
                isCompletelyDisplayed(),
            ),
        )

        // Check activity record tag
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewCategoryItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(tagName)),
            ),
        )
    }

    @Test
    fun colorTransferCategory() {
        fun checkPreviewUpdated(matcher: Matcher<View>) =
            checkViewIsDisplayed(allOf(withId(changeCategoryR.id.previewChangeCategory), matcher))

        val name = "name"
        val categoryName = "category"
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red
        val customColorInt = 0xff29a674.toInt()

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_category)

        // Select color
        clickOnViewWithText(coreR.string.change_category_color_hint)
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryColor, withCardColorInt(colorInt))
        checkPreviewUpdated(withCardColorInt(colorInt))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))

        // Check selected color is preselected on color selection
        scrollRecyclerToView(changeCategoryR.id.rvChangeCategoryColor, withId(dialogsR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryColor, withId(dialogsR.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xfff53639.toInt(),
            colorRed = 245,
            colorGreen = 54,
            colorBlue = 57,
            colorHue = 359,
            colorSaturation = 77,
            colorValue = 96,
        )

        // Select different color
        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Check new color selected
        checkPreviewUpdated(withCardColorInt(customColorInt))
        checkViewIsNotDisplayed(
            allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))),
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )
        clickOnViewWithText(coreR.string.change_category_color_hint)

        // Save tag
        typeTextIntoView(changeCategoryR.id.etChangeCategoryName, categoryName)
        clickOnViewWithText(coreR.string.change_category_save)

        // Tag saved
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewCategoryItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(categoryName)),
            ),
        )
        longClickOnView(withText(categoryName))
        checkPreviewUpdated(withCardColorInt(customColorInt))
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsNotDisplayed(
            allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))),
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )
        scrollRecyclerToView(changeCategoryR.id.rvChangeCategoryColor, withId(dialogsR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeCategoryR.id.rvChangeCategoryColor, withId(dialogsR.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )
        pressBack() // Close custom color
        pressBack() // Close color dropdown
        pressBack() // Close edit screen
        pressBack() // Close categories screen

        // Check statistics
        testUtils.addActivity(name = name, categories = listOf(categoryName))
        testUtils.addRecord(name)
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(statisticsR.id.btnStatisticsChartFilter)
        clickOnViewWithText(coreR.string.category_hint)
        pressBack()
        Thread.sleep(1000)
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewStatisticsItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(categoryName)),
                isCompletelyDisplayed(),
            ),
        )
    }

    @Test
    fun colorTransferGeneralRecordTag() {
        fun checkPreviewUpdated(matcher: Matcher<View>) =
            checkViewIsDisplayed(allOf(withId(changeRecordTagR.id.previewChangeRecordTag), matcher))

        val tagName = "tag"
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red
        val customColorInt = 0xff29a674.toInt()

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_record_tag)

        // Select color
        clickOnViewWithText(coreR.string.change_category_color_hint)
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColorInt(colorInt))
        checkPreviewUpdated(withCardColorInt(colorInt))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))

        // Check selected color is preselected on color selection
        scrollRecyclerToView(changeRecordTagR.id.rvChangeRecordTagColor, withId(dialogsR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withId(dialogsR.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xfff53639.toInt(),
            colorRed = 245,
            colorGreen = 54,
            colorBlue = 57,
            colorHue = 359,
            colorSaturation = 77,
            colorValue = 96,
        )

        // Select different color
        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Check new color selected
        checkPreviewUpdated(withCardColorInt(customColorInt))
        checkViewIsNotDisplayed(
            allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))),
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )
        clickOnViewWithText(coreR.string.change_category_color_hint)

        // Save tag
        typeTextIntoView(changeRecordTagR.id.etChangeRecordTagName, tagName)
        clickOnViewWithText(coreR.string.change_category_save)

        // Tag saved
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewCategoryItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(tagName)),
            ),
        )
        longClickOnView(withText(tagName))
        checkPreviewUpdated(withCardColorInt(customColorInt))
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsNotDisplayed(
            allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))),
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )
        scrollRecyclerToView(changeRecordTagR.id.rvChangeRecordTagColor, withId(dialogsR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeRecordTagR.id.rvChangeRecordTagColor, withId(dialogsR.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )
    }

    @Test
    fun colorTransferActivityFilter() {
        fun checkPreviewUpdated(matcher: Matcher<View>) =
            checkViewIsDisplayed(allOf(withId(changeActivityFilterTagR.id.previewChangeActivityFilter), matcher))

        val filterName = "filter"
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red
        val customColorInt = 0xff29a674.toInt()

        runBlocking { prefsInteractor.setShowActivityFilters(true) }
        tryAction { clickOnViewWithText(coreR.string.running_records_add_filter) }

        // Select color
        clickOnViewWithText(coreR.string.change_category_color_hint)
        clickOnRecyclerItem(changeActivityFilterTagR.id.rvChangeActivityFilterColor, withCardColorInt(colorInt))
        checkPreviewUpdated(withCardColorInt(colorInt))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))

        // Check selected color is preselected on color selection
        scrollRecyclerToView(
            changeActivityFilterTagR.id.rvChangeActivityFilterColor,
            withId(dialogsR.id.layoutColorPaletteItem),
        )
        clickOnRecyclerItem(
            changeActivityFilterTagR.id.rvChangeActivityFilterColor,
            withId(dialogsR.id.layoutColorPaletteItem),
        )
        checkColorState(
            finalColorInt = 0xfff53639.toInt(),
            colorRed = 245,
            colorGreen = 54,
            colorBlue = 57,
            colorHue = 359,
            colorSaturation = 77,
            colorValue = 96,
        )

        // Select different color
        typeTextIntoView(dialogsR.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(coreR.string.duration_dialog_save)

        // Check new color selected
        checkPreviewUpdated(withCardColorInt(customColorInt))
        checkViewIsNotDisplayed(
            allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))),
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )
        clickOnViewWithText(coreR.string.change_category_color_hint)

        // Save tag
        typeTextIntoView(changeActivityFilterTagR.id.etChangeActivityFilterName, filterName)
        clickOnViewWithText(coreR.string.change_category_save)

        // Tag saved
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewActivityFilterItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(filterName)),
            ),
        )
        longClickOnView(withText(filterName))
        checkPreviewUpdated(withCardColorInt(customColorInt))
        clickOnViewWithText(coreR.string.change_category_color_hint)
        checkViewIsNotDisplayed(
            allOf(withId(dialogsR.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))),
        )
        checkViewIsDisplayed(
            allOf(
                withId(dialogsR.id.viewColorPaletteItemSelected),
                withParent(withId(dialogsR.id.layoutColorPaletteItem)),
            ),
        )
        scrollRecyclerToView(
            changeActivityFilterTagR.id.rvChangeActivityFilterColor,
            withId(dialogsR.id.layoutColorPaletteItem),
        )
        clickOnRecyclerItem(
            changeActivityFilterTagR.id.rvChangeActivityFilterColor,
            withId(dialogsR.id.layoutColorPaletteItem),
        )
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )
    }

    @Test
    fun favouriteColors() {
        val customColorInt = 0xff29a674.toInt()
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red

        fun clickOnItem(id: Int, matcher: Matcher<View>): ViewInteraction {
            scrollRecyclerToView(id, matcher)
            return clickOnRecyclerItem(id, matcher)
        }

        fun check(recyclerId: Int) {
            // Fav button visibility
            clickOnItem(recyclerId, withCardColorInt(colorInt))
            checkViewDoesNotExist(withId(R.id.layoutColorFavouriteItem))
            checkViewIsDisplayed(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))
            clickOnItem(recyclerId, withCardColorInt(customColorInt))
            checkViewIsDisplayed(withId(R.id.layoutColorFavouriteItem))

            // Fav button click
            clickOnItem(recyclerId, withId(R.id.layoutColorFavouriteItem))
            checkViewDoesNotExist(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))
            clickOnItem(recyclerId, withId(R.id.layoutColorFavouriteItem))
            checkViewIsDisplayed(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))
        }

        // Check type
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewDoesNotExist(withId(R.id.layoutColorFavouriteItem))
        checkViewDoesNotExist(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))

        // Select custom color
        clickOnItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(R.id.layoutColorPaletteItem))
        typeTextIntoView(R.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withId(R.id.layoutColorFavouriteItem))
        checkViewDoesNotExist(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))
        clickOnItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(R.id.layoutColorFavouriteItem))
        checkViewIsDisplayed(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))

        // Preview updated
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.previewChangeRecordType),
                hasDescendant(withCardColorInt(customColorInt)),
            ),
        )

        // Check
        check(changeRecordTypeR.id.rvChangeRecordTypeColor)

        pressBack()
        pressBack()

        // Check category
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(coreR.string.categories_add_category)
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewDoesNotExist(withId(R.id.layoutColorFavouriteItem))
        checkViewIsDisplayed(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))

        // Preview updated
        clickOnItem(changeCategoryR.id.rvChangeCategoryColor, withCardColorInt(customColorInt))
        checkViewIsDisplayed(
            allOf(
                withId(changeCategoryR.id.previewChangeCategory),
                withCardColorInt(customColorInt),
            ),
        )

        // Check
        check(changeCategoryR.id.rvChangeCategoryColor)

        pressBack()
        pressBack()

        // Check tag
        clickOnViewWithText(coreR.string.categories_add_record_tag)
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewDoesNotExist(withId(R.id.layoutColorFavouriteItem))
        checkViewIsDisplayed(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))

        // Preview updated
        clickOnItem(changeRecordTagR.id.rvChangeRecordTagColor, withCardColorInt(customColorInt))
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTagR.id.previewChangeRecordTag),
                withCardColorInt(customColorInt),
            ),
        )

        // Check
        check(changeRecordTagR.id.rvChangeRecordTagColor)

        pressBack()
        pressBack()
        pressBack()

        // Check filter
        runBlocking { prefsInteractor.setShowActivityFilters(true) }
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(coreR.string.running_records_add_filter)
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkViewDoesNotExist(withId(R.id.layoutColorFavouriteItem))
        checkViewIsDisplayed(allOf(withId(R.id.layoutColorItem), withCardColorInt(customColorInt)))

        // Preview updated
        clickOnItem(changeActivityFilterTagR.id.rvChangeActivityFilterColor, withCardColorInt(customColorInt))
        checkViewIsDisplayed(
            allOf(
                withId(changeActivityFilterTagR.id.previewChangeActivityFilter),
                withCardColorInt(customColorInt),
            ),
        )

        // Check
        check(changeActivityFilterTagR.id.rvChangeActivityFilterColor)
    }

    private fun checkColorState(
        @ColorInt finalColorInt: Int,
        colorRed: Int = 0,
        colorGreen: Int = 0,
        colorBlue: Int = 0,
        colorHue: Int = 0,
        colorSaturation: Int = 100,
        colorValue: Int = 100,
    ) {
        checkSliderValue(dialogsR.id.sliderColorSelectionHue, colorHue)
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.cardColorSelectionSelectedColor), withCardColorInt(finalColorInt)),
        )
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.etColorSelectionRed), withText(colorRed.toString())),
        )
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.etColorSelectionGreen), withText(colorGreen.toString())),
        )
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.etColorSelectionBlue), withText(colorBlue.toString())),
        )
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.etColorSelectionHue), withText(colorHue.toString())),
        )
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.etColorSelectionSaturation), withText(colorSaturation.toString())),
        )
        checkViewIsDisplayed(
            allOf(withId(dialogsR.id.etColorSelectionValue), withText(colorValue.toString())),
        )
    }
}
