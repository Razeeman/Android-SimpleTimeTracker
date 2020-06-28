package com.example.util.simpletimetracker

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.ui.MainActivity
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnRecyclerItem
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnView
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import com.example.util.simpletimetracker.utils.withTag
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ChangeRecordTypeTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Inject
    lateinit var iconMapper: IconMapper

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext() as TimeTrackerApp
        DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .build()
            .inject(this)

        testUtils.clearDatabase()
    }

    @Test
    fun test() {
        val name = "Test"
        val newName = "Updated"
        val firstColor = ColorMapper.availableColors.first()
        val lastColor = ColorMapper.availableColors.last()
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()

        // Add item
        NavUtils.openRunningRecordsScreen()
        NavUtils.addActivity(name, firstColor, firstIcon)

        longClickOnView(withText(name))

        // View is set up
        checkViewIsDisplayed(withId(R.id.btnChangeRecordTypeDelete))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))
        checkViewIsDisplayed(allOf(withId(R.id.etChangeRecordTypeName), withText(name)))

        // Preview is updated
        checkPreviewUpdated(withText(name))
        checkPreviewUpdated(withCardColor(firstColor))
        checkPreviewUpdated(withTag(firstIcon))

        // Change item
        typeTextIntoView(R.id.etChangeRecordTypeName, newName)
        checkPreviewUpdated(withText(newName))

        clickOnViewWithText(R.string.change_record_type_color_hint)
        scrollRecyclerToView(R.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))

        clickOnViewWithText(R.string.change_record_type_icon_hint)
        scrollRecyclerToView(R.id.rvChangeRecordTypeIcon, hasDescendant(withTag(lastIcon)))
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTag(lastIcon))
        checkPreviewUpdated(withTag(lastIcon))

        clickOnViewWithText(R.string.change_record_type_save)

        // Record type updated
        checkViewIsDisplayed(withText(newName))
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTag(lastIcon))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.previewChangeRecordType)), matcher)
        )
}
