package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AddDefaultRecordTypeTest : BaseUiTest() {

    @Test
    fun addRecordType() {
        val name1 = "Games"
        val color1 = ColorMapper.getAvailableColors()[1]
        val name2 = "Work"
        val color2 = ColorMapper.getAvailableColors()[10]

        tryAction { checkViewIsDisplayed(withText(R.string.running_records_types_empty)) }
        checkViewIsDisplayed(withText(R.string.running_records_add_type))
        checkViewIsDisplayed(withText(R.string.running_records_add_default))

        // Open dialog
        clickOnViewWithText(R.string.running_records_add_default)
        checkActivity(name1, color1)
        checkActivity(name2, color2)

        // Close without saving
        pressBack()
        checkViewIsDisplayed(withText(R.string.running_records_add_type))
        checkViewIsDisplayed(withText(R.string.running_records_add_default))
        checkViewDoesNotExist(withText(name1))
        checkViewDoesNotExist(withText(name2))

        // Check selection
        clickOnViewWithText(R.string.running_records_add_default)
        clickOnViewWithText(name1)
        checkActivity(name1, R.color.colorFiltered)
        checkActivity(name2, color2)

        clickOnViewWithText(name1)
        clickOnViewWithText(name2)
        checkActivity(name1, color1)
        checkActivity(name2, R.color.colorFiltered)

        clickOnViewWithText(R.string.types_filter_show_all)
        checkActivity(name1, color1)
        checkActivity(name2, color2)

        clickOnViewWithText(R.string.types_filter_hide_all)
        checkActivity(name1, R.color.colorFiltered)
        checkActivity(name2, R.color.colorFiltered)

        // Try to save when nothing selected
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText(R.string.duration_dialog_save))

        // Save
        clickOnViewWithText(name1)
        clickOnViewWithText(name2)
        clickOnViewWithText(R.string.duration_dialog_save)
        Thread.sleep(1000)

        // Types added
        checkViewIsDisplayed(withText(R.string.running_records_add_type))
        checkViewDoesNotExist(withText(R.string.running_records_add_default))
        checkActivity(name1, color1)
        checkActivity(name2, color2)
    }

    private fun checkActivity(
        name: String,
        color: Int,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                withCardColor(color),
                hasDescendant(withText(name))
            )
        )
    }
}
