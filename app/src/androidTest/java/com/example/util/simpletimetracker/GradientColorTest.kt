package com.example.util.simpletimetracker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkSliderValue
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickLocation
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColorInt
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GradientColorTest : BaseUiTest() {

    @Test
    fun gradientEndpointsAreEditedIndependently() {
        openGradientEditor()
        checkGradientPreview(Color.RED, Color.BLUE, 0)

        selectEndpoint(changeRecordTypeR.id.btnChangeRecordTypeGradientStart)
        selectCustomColor("#00ff00")
        checkGradientControls(Color.GREEN, Color.BLUE, 0)
        checkGradientPreview(Color.GREEN, Color.BLUE, 0)

        selectEndpoint(changeRecordTypeR.id.btnChangeRecordTypeGradientEnd)
        selectCustomColor("#ffff00")
        checkGradientControls(Color.GREEN, Color.YELLOW, 0)
        checkGradientPreview(Color.GREEN, Color.YELLOW, 0)
    }

    @Test
    fun changingGradientAngleUpdatesPreview() {
        openGradientEditor()
        checkGradientPreview(Color.RED, Color.BLUE, 0)

        changeAngle(180)
        checkGradientControls(Color.RED, Color.BLUE, 180)
        checkGradientPreview(Color.RED, Color.BLUE, 180)

        changeAngle(0)
        checkGradientControls(Color.RED, Color.BLUE, 0)
        checkGradientPreview(Color.RED, Color.BLUE, 0)
    }

    @Test
    fun gradientColorsAndAnglePersistAfterReopeningEditor() {
        openGradientEditor()
        changeAngle(180)
        saveActivity()

        assertEquals(
            AppColor(
                colorId = 0,
                colorInt = Color.RED.toString(),
                gradient = AppColor.Gradient(
                    endColorId = 0,
                    endColorInt = Color.BLUE.toString(),
                    angleDegrees = 180,
                ),
            ),
            runBlocking { recordTypeRepo.get(ACTIVITY_NAME).single().color },
        )

        longClickOnView(withText(ACTIVITY_NAME))
        checkGradientPreview(Color.RED, Color.BLUE, 180)
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        checkGradientControls(Color.RED, Color.BLUE, 180)
        checkEndpointCustomColor(changeRecordTypeR.id.btnChangeRecordTypeGradientStart, Color.RED)
        checkEndpointCustomColor(changeRecordTypeR.id.btnChangeRecordTypeGradientEnd, Color.BLUE)
    }

    @Test
    fun togglingGradientRetainsEndColorAndAngleDuringEdit() {
        openGradientEditor()
        changeAngle(180)
        toggleGradient()

        checkViewDoesNotExist(withId(changeRecordTypeR.id.containerChangeRecordTypeGradientControls))
        checkViewIsDisplayed(allOf(withId(baseR.id.layoutColorGradientItem), not(isSelected())))
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(changeRecordTypeR.id.previewChangeRecordType),
                    hasDescendant(withCardColorInt(Color.RED)),
                ),
            )
        }

        // Editing the solid/start color must not overwrite the retained end color or angle.
        selectCustomColor("#00ff00")
        toggleGradient()
        checkGradientControls(Color.GREEN, Color.BLUE, 180)
        checkGradientPreview(Color.GREEN, Color.BLUE, 180)
        checkEndpointCustomColor(changeRecordTypeR.id.btnChangeRecordTypeGradientEnd, Color.BLUE)
    }

    @Test
    fun savedGradientAppearsOnActivityCard() {
        openGradientEditor()
        changeAngle(180)
        saveActivity()

        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                hasDescendant(withText(ACTIVITY_NAME)),
                hasDescendant(withGradientBackground(Color.RED, Color.BLUE, 180)),
            ),
        )
    }

    @Test
    fun savedGradientUsesStartColorOnRunningTimer() {
        openGradientEditor()
        changeAngle(180)
        saveActivity()
        clickOnView(allOf(withId(baseR.id.viewRecordTypeItem), hasDescendant(withText(ACTIVITY_NAME))))

        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRunningRecordItem),
                hasDescendant(withText(ACTIVITY_NAME)),
                withCardColorInt(Color.RED),
            ),
        )
    }

    private fun openGradientEditor() {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        selectCustomColor("#ff0000")
        toggleGradient()
        changeAngle(0)
        selectEndpoint(changeRecordTypeR.id.btnChangeRecordTypeGradientEnd)
        selectCustomColor("#0000ff")
    }

    private fun toggleGradient() {
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(baseR.id.layoutColorGradientItem))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(baseR.id.layoutColorGradientItem))
    }

    private fun selectEndpoint(id: Int) {
        scrollToGradientControls()
        clickOnViewWithId(id)
    }

    private fun openCustomColor() {
        scrollRecyclerToView(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(baseR.id.layoutColorPaletteItem))
        clickOnRecyclerItem(changeRecordTypeR.id.rvChangeRecordTypeColor, withId(baseR.id.layoutColorPaletteItem))
    }

    private fun selectCustomColor(hex: String) {
        openCustomColor()
        typeTextIntoView(dialogsR.id.etColorSelectionHex, hex)
        clickOnViewWithText(coreR.string.duration_dialog_save)
    }

    private fun checkEndpointCustomColor(endpointId: Int, color: Int) {
        selectEndpoint(endpointId)
        openCustomColor()
        checkViewIsDisplayed(allOf(withId(dialogsR.id.cardColorSelectionSelectedColor), withCardColorInt(color)))
        pressBack()
    }

    private fun changeAngle(angle: Int) {
        scrollToGradientControls()
        val location = when (angle) {
            0 -> GeneralLocation.CENTER_LEFT
            180 -> GeneralLocation.CENTER
            else -> error("Unsupported test angle: $angle")
        }
        // Use a touch event: assigning Slider.value does not trigger the fromUser listener.
        onView(withId(changeRecordTypeR.id.sliderChangeRecordTypeGradientAngle)).perform(clickLocation(location))
        checkSliderValue(changeRecordTypeR.id.sliderChangeRecordTypeGradientAngle, angle)
    }

    private fun scrollToGradientControls() {
        scrollRecyclerToView(
            changeRecordTypeR.id.rvChangeRecordTypeColor,
            withId(changeRecordTypeR.id.containerChangeRecordTypeGradientControls),
        )
    }

    private fun checkGradientControls(start: Int, end: Int, angle: Int) {
        scrollToGradientControls()
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.viewChangeRecordTypeGradientStartColor),
                withBackgroundColor(start),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.viewChangeRecordTypeGradientEndColor),
                withBackgroundColor(end),
            ),
        )
        checkViewIsDisplayed(
            allOf(
                withId(changeRecordTypeR.id.tvChangeRecordTypeGradientAngle),
                withText(angle.toString()),
            ),
        )
        checkSliderValue(changeRecordTypeR.id.sliderChangeRecordTypeGradientAngle, angle)
    }

    private fun checkGradientPreview(start: Int, end: Int, angle: Int) {
        // ColorPreviewAnimator runs independently of Espresso's coroutine idling resource.
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(changeRecordTypeR.id.previewChangeRecordType),
                    hasDescendant(withGradientBackground(start, end, angle)),
                ),
            )
        }
    }

    private fun saveActivity() {
        clickOnViewWithText(coreR.string.change_record_type_color_hint)
        typeTextIntoView(changeRecordTypeR.id.etChangeRecordTypeName, ACTIVITY_NAME)
        clickOnViewWithText(coreR.string.change_record_type_save)
    }

    private fun withBackgroundColor(color: Int): Matcher<View> = object : TypeSafeMatcher<View>() {
        override fun matchesSafely(view: View): Boolean {
            return (view.background as? ColorDrawable)?.color == color
        }

        override fun describeTo(description: Description) {
            description.appendText("with background color ").appendValue(color)
        }
    }

    private fun withGradientBackground(start: Int, end: Int, angle: Int): Matcher<View> =
        object : TypeSafeMatcher<View>() {
            override fun matchesSafely(view: View): Boolean {
                val background = view.background ?: return false
                val bounds = background.bounds
                if (bounds.isEmpty) return false
                val orientation = when (angle) {
                    0 -> GradientDrawable.Orientation.LEFT_RIGHT
                    180 -> GradientDrawable.Orientation.RIGHT_LEFT
                    else -> error("Unsupported test angle: $angle")
                }
                val actual = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
                val expected = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
                try {
                    val canvas = Canvas(actual)
                    canvas.translate(-bounds.left.toFloat(), -bounds.top.toFloat())
                    background.draw(canvas)
                    // Use Android's renderer as an independent reference for these cardinal angles.
                    GradientDrawable(orientation, intArrayOf(start, end)).apply {
                        setBounds(0, 0, expected.width, expected.height)
                        draw(Canvas(expected))
                    }
                    // Sample the interior to avoid rounded corners, borders, text, and icons.
                    return listOf(1, 2, 3).all { quarter ->
                        val x = actual.width * quarter / 4
                        val y = actual.height / 2
                        val actualColor = actual.getPixel(x, y)
                        val expectedColor = expected.getPixel(x, y)
                        listOf<(Int) -> Int>(
                            Color::alpha,
                            Color::red,
                            Color::green,
                            Color::blue,
                        ).all { channel ->
                            abs(channel(actualColor) - channel(expectedColor)) <= 2
                        }
                    }
                } finally {
                    actual.recycle()
                    expected.recycle()
                }
            }

            override fun describeTo(description: Description) {
                description.appendText("with rendered gradient from ").appendValue(start)
                    .appendText(" to ").appendValue(end).appendText(" at angle ").appendValue(angle)
            }
        }

    companion object {
        private const val ACTIVITY_NAME = "Gradient activity"
    }
}
