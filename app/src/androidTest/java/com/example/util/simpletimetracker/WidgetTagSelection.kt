package com.example.util.simpletimetracker

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_widget.widget.WidgetTagSelectionActivity
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetTagSelection : BaseUiTest() {

    private lateinit var scenarioRule: ActivityScenario<WidgetTagSelectionActivity>

    override fun after() {
        super.after()
        scenarioRule.close()
    }

    @Test
    fun widgetTagSelection() {
        val name1 = "TypeName1"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val tag3 = "Tag3"
        val tag4 = "Tag4"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1, archived = true)
        testUtils.addRecordTag(tag3)
        testUtils.addRecordTag(tag4, archived = true)

        val intent = WidgetTagSelectionActivity.getStartIntent(
            ApplicationProvider.getApplicationContext(),
            RecordTagSelectionParams(testUtils.getTypeId(name1))
        )
        scenarioRule = ActivityScenario.launch(intent)

        // Check data
        checkViewIsDisplayed(withText(R.string.change_record_untagged))
        checkViewIsDisplayed(withText(tag1))
        checkViewDoesNotExist(withText(tag2))
    }
}
