package com.example.util.simpletimetracker

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
    fun messageRecordTypeName() {
        tryAction { clickOnViewWithText(R.string.running_records_add_type) }
        clickOnViewWithText(R.string.change_record_type_save)
        toastTextShowing(R.string.change_record_message_choose_name)
    }

    @Test
    fun messageRecordActivity() {
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordAdd)
        clickOnViewWithText(R.string.change_record_save)
        toastTextShowing(R.string.message_choose_type)
    }
}
