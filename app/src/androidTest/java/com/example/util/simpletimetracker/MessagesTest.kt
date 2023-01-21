package com.example.util.simpletimetracker

import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MessagesTest : BaseUiTest() {

    @Test
    fun messageRecordTypeName() {
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }
        clickOnViewWithId(R.id.fieldChangeRecordTypeColor)
        clickOnViewWithText(R.string.change_record_type_save)
        checkViewIsDisplayed(
            allOf(
                withText(R.string.change_record_message_choose_name),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )
    }

    @Test
    fun messageRecordActivity() {
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_save)
        checkViewIsDisplayed(
            allOf(
                withText(R.string.change_record_message_choose_type),
                withId(com.google.android.material.R.id.snackbar_text)
            )
        )
    }
}
