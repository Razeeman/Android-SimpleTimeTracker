package com.example.util.simpletimetracker

import android.view.View
import androidx.annotation.ColorInt
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
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
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithIdOnPager
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColorInt
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CustomColorTest : BaseUiTest() {

    @Test
    fun colorSelectionDialog() {
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

        // Open color selection dialog
        clickOnViewWithText(R.string.change_record_type_color_hint)
        scrollRecyclerToView(R.id.rvChangeRecordTypeColor, withId(R.id.layoutColorPaletteItem))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withId(R.id.layoutColorPaletteItem))

        checkViewIsDisplayed(withText(R.string.color_selection_base_color_hint))
        checkViewIsDisplayed(withText(R.string.color_selection_adjust_color_hint))
        checkViewIsDisplayed(withText(R.string.color_selection_final_color_hint))

        // Change slider hue
        onView(withId(R.id.sliderColorSelectionHue)).perform(swipeLeft())
        checkSliderValue(R.id.sliderColorSelectionHue, 0)
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionHue), withText("0")))

        onView(withId(R.id.sliderColorSelectionHue)).perform(swipeRight())
        checkSliderValue(R.id.sliderColorSelectionHue, 360)
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionHue), withText("360")))

        // Change hex
        typeTextIntoView(R.id.etColorSelectionHex, "#FF0000")
        checkColorState(
            finalColorInt = 0xffff0000.toInt(),
            colorRed = 255,
        )

        typeTextIntoView(R.id.etColorSelectionHex, "#00FF00")
        checkColorState(
            finalColorInt = 0xff00ff00.toInt(),
            colorGreen = 255,
            colorHue = 120
        )

        typeTextIntoView(R.id.etColorSelectionHex, "#0000FF")
        checkColorState(
            finalColorInt = 0xff0000ff.toInt(),
            colorBlue = 255,
            colorHue = 240
        )

        typeTextIntoView(R.id.etColorSelectionHex, "#29a674")
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
        typeTextIntoView(R.id.etColorSelectionRed, "255")
        typeTextIntoView(R.id.etColorSelectionGreen, "0")
        typeTextIntoView(R.id.etColorSelectionBlue, "0")
        checkColorState(
            finalColorInt = 0xffff0000.toInt(),
            colorRed = 255,
        )

        typeTextIntoView(R.id.etColorSelectionRed, "0")
        typeTextIntoView(R.id.etColorSelectionGreen, "255")
        typeTextIntoView(R.id.etColorSelectionBlue, "0")
        checkColorState(
            finalColorInt = 0xff00ff00.toInt(),
            colorGreen = 255,
            colorHue = 120
        )

        typeTextIntoView(R.id.etColorSelectionRed, "0")
        typeTextIntoView(R.id.etColorSelectionGreen, "0")
        typeTextIntoView(R.id.etColorSelectionBlue, "255")
        checkColorState(
            finalColorInt = 0xff0000ff.toInt(),
            colorBlue = 255,
            colorHue = 240
        )

        typeTextIntoView(R.id.etColorSelectionRed, "41")
        typeTextIntoView(R.id.etColorSelectionGreen, "166")
        typeTextIntoView(R.id.etColorSelectionBlue, "116")
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )

        // Change HSV

        typeTextIntoView(R.id.etColorSelectionHue, "0")
        typeTextIntoView(R.id.etColorSelectionSaturation, "100")
        typeTextIntoView(R.id.etColorSelectionValue, "100")
        checkColorState(
            finalColorInt = 0xffff0000.toInt(),
            colorRed = 255,
        )

        typeTextIntoView(R.id.etColorSelectionHue, "120")
        checkColorState(
            finalColorInt = 0xff00ff00.toInt(),
            colorGreen = 255,
            colorHue = 120
        )

        typeTextIntoView(R.id.etColorSelectionHue, "240")
        checkColorState(
            finalColorInt = 0xff0000ff.toInt(),
            colorBlue = 255,
            colorHue = 240
        )

        typeTextIntoView(R.id.etColorSelectionHue, "156")
        typeTextIntoView(R.id.etColorSelectionSaturation, "75")
        typeTextIntoView(R.id.etColorSelectionValue, "65")
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
    fun colorTransferRecordType() {
        fun checkPreviewUpdated(matcher: Matcher<View>) =
            checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordType), matcher))

        val name = "name"
        val tagName = "tag"
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red
        val customColorInt = 0xff29a674.toInt()

        tryAction { clickOnViewWithText(R.string.running_records_add_type) }

        // Select color
        clickOnViewWithText(R.string.change_record_type_color_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColorInt(colorInt))
        checkPreviewUpdated(withCardColorInt(colorInt))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))

        // Check selected color is preselected on color selection
        scrollRecyclerToView(R.id.rvChangeRecordTypeColor, withId(R.id.layoutColorPaletteItem))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withId(R.id.layoutColorPaletteItem))
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
        typeTextIntoView(R.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(R.string.duration_dialog_save)

        // Check new color selected
        checkPreviewUpdated(withCardColorInt(customColorInt))
        checkViewIsNotDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))
        checkViewIsDisplayed(
            allOf(withId(R.id.viewColorPaletteItemSelected), withParent(withId(R.id.layoutColorPaletteItem)))
        )

        // Save record type
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        clickOnViewWithText(R.string.change_record_type_save)

        // Record type saved
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(name)),
            )
        )
        longClickOnView(withText(name))
        checkPreviewUpdated(withCardColorInt(customColorInt))
        clickOnViewWithText(R.string.change_record_type_color_hint)
        checkViewIsDisplayed(
            allOf(withId(R.id.viewColorPaletteItemSelected), withParent(withId(R.id.layoutColorPaletteItem)))
        )
        scrollRecyclerToView(R.id.rvChangeRecordTypeColor, withId(R.id.layoutColorPaletteItem))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withId(R.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )
        pressBack()
        pressBack()

        // Check record
        testUtils.addRecord(name)
        testUtils.addRecordTag(tagName = tagName, typeName = name)
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check statistics
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check activity record tag
        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(tagName)),
            )
        )
    }

    @Test
    fun colorTransferActivityTag() {
        fun checkPreviewUpdated(matcher: Matcher<View>) =
            checkViewIsDisplayed(allOf(withId(R.id.previewChangeCategory), matcher))

        val name = "name"
        val tagName = "tag"
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red
        val customColorInt = 0xff29a674.toInt()

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(R.string.categories_add_activity_tag)

        // Select color
        clickOnViewWithText(R.string.change_category_color_hint)
        clickOnRecyclerItem(R.id.rvChangeCategoryColor, withCardColorInt(colorInt))
        checkPreviewUpdated(withCardColorInt(colorInt))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))

        // Check selected color is preselected on color selection
        scrollRecyclerToView(R.id.rvChangeCategoryColor, withId(R.id.layoutColorPaletteItem))
        clickOnRecyclerItem(R.id.rvChangeCategoryColor, withId(R.id.layoutColorPaletteItem))
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
        typeTextIntoView(R.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(R.string.duration_dialog_save)

        // Check new color selected
        checkPreviewUpdated(withCardColorInt(customColorInt))
        checkViewIsNotDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))
        checkViewIsDisplayed(
            allOf(withId(R.id.viewColorPaletteItemSelected), withParent(withId(R.id.layoutColorPaletteItem)))
        )

        // Save tag
        typeTextIntoView(R.id.etChangeCategoryName, tagName)
        clickOnViewWithText(R.string.change_category_save)

        // Tag saved
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(tagName)),
            )
        )
        longClickOnView(withText(tagName))
        checkPreviewUpdated(withCardColorInt(customColorInt))
        clickOnViewWithText(R.string.change_category_color_hint)
        checkViewIsNotDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))
        checkViewIsDisplayed(
            allOf(withId(R.id.viewColorPaletteItemSelected), withParent(withId(R.id.layoutColorPaletteItem)))
        )
        scrollRecyclerToView(R.id.rvChangeCategoryColor, withId(R.id.layoutColorPaletteItem))
        clickOnRecyclerItem(R.id.rvChangeCategoryColor, withId(R.id.layoutColorPaletteItem))
        checkColorState(
            finalColorInt = 0xff29a674.toInt(),
            colorRed = 41,
            colorGreen = 166,
            colorBlue = 116,
            colorHue = 156,
            colorSaturation = 75,
            colorValue = 65,
        )
        pressBack()
        pressBack()
        pressBack()

        // Check statistics
        testUtils.addActivity(name = name, categories = listOf(tagName))
        testUtils.addRecord(name)
        NavUtils.openStatisticsScreen()
        clickOnViewWithIdOnPager(R.id.btnStatisticsChartFilter)
        clickOnViewWithText(R.string.chart_filter_type_category)
        pressBack()
        Thread.sleep(1000)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(tagName)),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun colorTransferGeneralRecordTag() {
        fun checkPreviewUpdated(matcher: Matcher<View>) =
            checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordTag), matcher))

        val tagName = "tag"
        val colorId = ColorMapper.getAvailableColors()[1]
        val colorInt = colorId.let(::getColor) // red
        val customColorInt = 0xff29a674.toInt()

        NavUtils.openSettingsScreen()
        NavUtils.openCategoriesScreen()
        clickOnViewWithText(R.string.categories_add_record_tag)

        // Select color
        clickOnViewWithText(R.string.change_category_color_hint)
        clickOnRecyclerItem(R.id.rvChangeRecordTagColor, withCardColorInt(colorInt))
        checkPreviewUpdated(withCardColorInt(colorInt))
        checkViewIsDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))

        // Check selected color is preselected on color selection
        scrollRecyclerToView(R.id.rvChangeRecordTagColor, withId(R.id.layoutColorPaletteItem))
        clickOnRecyclerItem(R.id.rvChangeRecordTagColor, withId(R.id.layoutColorPaletteItem))
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
        typeTextIntoView(R.id.etColorSelectionHex, "#29a674")
        clickOnViewWithText(R.string.duration_dialog_save)

        // Check new color selected
        checkPreviewUpdated(withCardColorInt(customColorInt))
        checkViewIsNotDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))
        checkViewIsDisplayed(
            allOf(withId(R.id.viewColorPaletteItemSelected), withParent(withId(R.id.layoutColorPaletteItem)))
        )

        // Save tag
        typeTextIntoView(R.id.etChangeRecordTagName, tagName)
        clickOnViewWithText(R.string.change_category_save)

        // Tag saved
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewCategoryItem),
                withCardColorInt(customColorInt),
                hasDescendant(withText(tagName)),
            )
        )
        longClickOnView(withText(tagName))
        checkPreviewUpdated(withCardColorInt(customColorInt))
        clickOnViewWithText(R.string.change_category_color_hint)
        checkViewIsNotDisplayed(allOf(withId(R.id.viewColorItemSelected), withParent(withCardColorInt(colorInt))))
        checkViewIsDisplayed(
            allOf(withId(R.id.viewColorPaletteItemSelected), withParent(withId(R.id.layoutColorPaletteItem)))
        )
        scrollRecyclerToView(R.id.rvChangeRecordTagColor, withId(R.id.layoutColorPaletteItem))
        clickOnRecyclerItem(R.id.rvChangeRecordTagColor, withId(R.id.layoutColorPaletteItem))
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

    private fun checkColorState(
        @ColorInt finalColorInt: Int,
        colorRed: Int = 0,
        colorGreen: Int = 0,
        colorBlue: Int = 0,
        colorHue: Int = 0,
        colorSaturation: Int = 100,
        colorValue: Int = 100,
    ) {
        checkSliderValue(R.id.sliderColorSelectionHue, colorHue)
        checkViewIsDisplayed(allOf(withId(R.id.cardColorSelectionSelectedColor), withCardColorInt(finalColorInt)))
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionRed), withText(colorRed.toString())))
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionGreen), withText(colorGreen.toString())))
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionBlue), withText(colorBlue.toString())))
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionHue), withText(colorHue.toString())))
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionSaturation), withText(colorSaturation.toString())))
        checkViewIsDisplayed(allOf(withId(R.id.etColorSelectionValue), withText(colorValue.toString())))
    }
}
