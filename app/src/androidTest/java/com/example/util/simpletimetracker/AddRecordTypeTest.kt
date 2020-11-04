package com.example.util.simpletimetracker

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.scrollRecyclerToPosition
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddRecordTypeTest : BaseUiTest() {

    @Test
    fun addRecordType() {
        val name = "Test"
        val firstColor = ColorMapper.getAvailableColors().first()
        val lastColor = ColorMapper.getAvailableColors().last()
        val lastColorPosition = ColorMapper.getAvailableColors().size - 1
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()
        val lastIconPosition = iconMapper.availableIconsNames.size - 1

        clickOnViewWithText(R.string.running_records_add_type)

        // View is set up
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordTypeDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Name is not selected
        clickOnViewWithText(R.string.change_record_type_save)

        // Typing name
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        checkPreviewUpdated(hasDescendant(withText(name)))

        // Open color chooser
        clickOnViewWithText(R.string.change_record_type_color_hint)
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Selecting color
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))

        // Selecting color
        scrollRecyclerToPosition(R.id.rvChangeRecordTypeColor, lastColorPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))

        // Open icon chooser
        clickOnViewWithText(R.string.change_record_type_icon_hint)
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Selecting icon
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTag(firstIcon))
        checkPreviewUpdated(hasDescendant(withTag(firstIcon)))

        // Selecting icon
        scrollRecyclerToPosition(R.id.rvChangeRecordTypeIcon, lastIconPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTag(lastIcon))
        checkPreviewUpdated(hasDescendant(withTag(lastIcon)))

        clickOnViewWithText(R.string.change_record_type_save)

        // Record type added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTag(lastIcon))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(allOf(withId(R.id.previewChangeRecordType), matcher))
}
