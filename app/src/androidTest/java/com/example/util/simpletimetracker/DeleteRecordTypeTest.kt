package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteRecordTypeTest : BaseUiTest() {

    @Test
    fun test() {
        val name = "Test"
        val color = ColorMapper.getAvailableColors().first()
        val icon = iconMapper.availableIconsNames.values.first()

        clickOnViewWithText(R.string.running_records_add_type)
        checkViewIsNotDisplayed(withId(R.id.btnChangeRecordTypeDelete))
        closeSoftKeyboard()
        pressBack()

        // Add item
        NavUtils.addActivity(name, color, icon)

        // Delete item
        longClickOnView(withText(name))
        checkViewIsDisplayed(withId(R.id.btnChangeRecordTypeDelete))
        clickOnViewWithId(R.id.btnChangeRecordTypeDelete)

        // TODO check message

        // Record type is deleted
        checkViewDoesNotExist(withText(name))
        checkViewDoesNotExist(withCardColor(color))
        checkViewDoesNotExist(withTag(icon))
    }
}
