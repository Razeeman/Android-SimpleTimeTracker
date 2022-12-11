package com.example.util.simpletimetracker

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.domain.model.ActivityFilter
import com.example.util.simpletimetracker.feature_widget.universal.activity.view.WidgetUniversalActivity
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Widget
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.withCardColor
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@Widget
@RunWith(AndroidJUnit4::class)
class WidgetUniversal : BaseUiTest() {

    private lateinit var scenarioRule: ActivityScenario<WidgetUniversalActivity>

    override fun after() {
        super.after()
        scenarioRule.close()
    }

    @Test
    fun widgetUniversalActivities() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val name3 = "TypeName3"
        val filter = "Filter"

        // Add data
        testUtils.addActivity(name = name1, color = firstColor)
        testUtils.addActivity(name = name2, color = lastColor)
        testUtils.addActivity(name = name3, archived = true)
        testUtils.addActivityFilter(filter)
        scenarioRule = ActivityScenario.launch(WidgetUniversalActivity::class.java)

        // Check data
        checkType(firstColor, name1)
        checkType(lastColor, name2)
        checkViewDoesNotExist(withText(name3))
        checkViewDoesNotExist(withText(filter))
        checkViewDoesNotExist(withText(R.string.running_records_add_filter))

        // Start activity
        clickOnViewWithText(name1)
        checkType(R.color.colorFiltered, name1)

        // Stop activity
        clickOnViewWithText(name1)
        checkType(firstColor, name1)
    }

    @Test
    fun widgetUniversalMultitasking() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"

        // Add data
        testUtils.addActivity(name = name1, color = firstColor)
        testUtils.addActivity(name = name2, color = lastColor)
        scenarioRule = ActivityScenario.launch(WidgetUniversalActivity::class.java)

        // Start timers
        clickOnViewWithText(name1)
        checkType(R.color.colorFiltered, name1)
        clickOnViewWithText(name2)
        checkType(R.color.colorFiltered, name2)

        // Stop timer
        clickOnViewWithText(name2)
        checkType(R.color.colorFiltered, name1)
        checkType(lastColor, name2)

        // Change setting
        runBlocking { prefsInteractor.setAllowMultitasking(false) }

        // Start another
        clickOnViewWithText(name2)
        checkType(firstColor, name1)
        checkType(R.color.colorFiltered, name2)

        // Start another
        clickOnViewWithText(name1)
        checkType(R.color.colorFiltered, name1)
        checkType(lastColor, name2)
    }

    @Test
    fun widgetUniversalTagSelection() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val tag1 = "TagName1"
        val tag2 = "TagName2"

        // Add data
        testUtils.addActivity(name = name1, color = firstColor)
        testUtils.addActivity(name = name2, color = lastColor)
        testUtils.addRecordTag(tag1, name1)
        testUtils.addRecordTag(tag2, name1, archived = true)
        scenarioRule = ActivityScenario.launch(WidgetUniversalActivity::class.java)

        // Start timers
        tryAction { clickOnViewWithText(name1) }
        checkType(R.color.colorFiltered, name1)
        clickOnViewWithText(name2)
        checkType(R.color.colorFiltered, name2)
        clickOnViewWithText(name1)
        clickOnViewWithText(name2)

        // Change setting
        runBlocking { prefsInteractor.setShowRecordTagSelection(true) }

        // Start timers
        clickOnViewWithText(name1)
        tryAction { checkViewIsDisplayed(withText(R.string.change_record_untagged)) }
        checkViewIsDisplayed(withText(tag1))
        checkViewDoesNotExist(withText(tag2))
        clickOnViewWithText(tag1)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkType(R.color.colorFiltered, name1)

        clickOnViewWithText(name2)
        checkType(R.color.colorFiltered, name2)
    }

    @Test
    fun widgetUniversalFilters() {
        val name1 = "TypeName1"
        val name2 = "TypeName2"
        val filter = "Filter"

        // Add data
        runBlocking { prefsInteractor.setShowActivityFilters(true) }
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivityFilter(
            name = filter,
            type = ActivityFilter.Type.Activity,
            color = firstColor,
            names = listOf(name1)
        )
        scenarioRule = ActivityScenario.launch(WidgetUniversalActivity::class.java)

        // Check filters
        tryAction { checkViewIsDisplayed(withText(name1)) }
        checkViewIsDisplayed(withText(name2))
        checkViewIsDisplayed(allOf(withCardColor(R.color.colorFiltered), hasDescendant(withText(filter))))
        checkViewDoesNotExist(withText(R.string.running_records_add_filter))

        clickOnViewWithText(filter)
        checkViewIsDisplayed(withText(name1))
        checkViewDoesNotExist(withText(name2))
        checkViewIsDisplayed(allOf(withCardColor(firstColor), hasDescendant(withText(filter))))

        clickOnViewWithText(filter)
        checkViewIsDisplayed(withText(name1))
        checkViewIsDisplayed(withText(name2))
        checkViewIsDisplayed(allOf(withCardColor(R.color.colorFiltered), hasDescendant(withText(filter))))
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
