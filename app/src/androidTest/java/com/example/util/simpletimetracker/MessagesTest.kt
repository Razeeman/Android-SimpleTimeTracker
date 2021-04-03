package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.toastTextShowing
import com.example.util.simpletimetracker.utils.tryAction
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessagesTest : BaseUiTest() {

    @Test
    fun messages() {
        // Activity with no name
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }
        clickOnViewWithText(R.string.change_record_type_save)
        toastTextShowing(R.string.change_record_message_choose_name)
        closeSoftKeyboard()
        pressBack()

        // Add record
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)

        // Record with no activity
        // Wait for previous message to disappear
        Thread.sleep(5000)
        clickOnViewWithText(R.string.change_record_save)
        toastTextShowing(R.string.message_choose_type)
    }
}
