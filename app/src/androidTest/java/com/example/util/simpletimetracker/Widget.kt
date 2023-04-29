package com.example.util.simpletimetracker

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_widget.single.settings.WidgetSingleSettingsActivity
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Widget
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR

@HiltAndroidTest
@Widget
@RunWith(AndroidJUnit4::class)
class Widget : BaseUiTest() {

    private lateinit var scenarioRule: ActivityScenario<WidgetSingleSettingsActivity>

    override fun after() {
        super.after()
        scenarioRule.close()
    }

    @Test
    fun widgetConfigure() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"

        // Add data
        testUtils.addActivity(name = name1, color = firstColor)
        testUtils.addActivity(name = name2, color = lastColor)
        testUtils.addActivity(name = name3, archived = true)
        scenarioRule = ActivityScenario.launch(WidgetSingleSettingsActivity::class.java)

        // Check data
        checkType(firstColor, name1)
        checkType(lastColor, name2)
        checkViewDoesNotExist(withText(name3))
    }

    private fun checkType(color: Int, name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewRecordTypeItem),
                withCardColor(color),
                hasDescendant(withText(name))
            )
        )
    }
}
