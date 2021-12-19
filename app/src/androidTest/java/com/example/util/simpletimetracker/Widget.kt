package com.example.util.simpletimetracker

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.feature_widget.configure.view.WidgetConfigureActivity
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Widget
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@Widget
@RunWith(AndroidJUnit4::class)
class Widget : BaseUiTest() {

    private lateinit var scenarioRule: ActivityScenario<WidgetConfigureActivity>

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
        scenarioRule = ActivityScenario.launch(WidgetConfigureActivity::class.java)

        // Check data
        checkType(firstColor, name1)
        checkType(lastColor, name2)
        checkViewDoesNotExist(withText(name3))
    }

    private fun checkType(color: Int, name: String) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordTypeItem),
                withCardColor(color),
                hasDescendant(withText(name))
            )
        )
    }
}
