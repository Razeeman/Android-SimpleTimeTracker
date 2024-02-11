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
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_change_record_type.R as changeRecordTypeR
import com.example.util.simpletimetracker.feature_records.R as recordsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MessagesTest : BaseUiTest() {

    @Test
    fun messageRecordTypeName() {
        tryAction { clickOnViewWithText(coreR.string.running_records_add_type) }
        clickOnViewWithId(changeRecordTypeR.id.fieldChangeRecordTypeColor)
        clickOnViewWithText(coreR.string.change_record_type_save)
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.change_record_message_choose_name),
                withId(com.google.android.material.R.id.snackbar_text),
            ),
        )
    }

    @Test
    fun messageRecordActivity() {
        NavUtils.openRecordsScreen()
        clickOnViewWithId(recordsR.id.btnRecordAdd)
        clickOnViewWithText(coreR.string.change_record_save)
        checkViewIsDisplayed(
            allOf(
                withText(coreR.string.change_record_message_choose_type),
                withId(com.google.android.material.R.id.snackbar_text),
            ),
        )
    }
}
