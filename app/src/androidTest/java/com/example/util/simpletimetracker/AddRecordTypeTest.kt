package com.example.util.simpletimetracker

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
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
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.scrollToPosition
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.withCardColor
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AddRecordTypeTest {

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
    fun addTest() {
        val name = "Test"
        val firstColor = ColorMapper.availableColors.first()
        val lastColor = ColorMapper.availableColors.last()
        val lastColorPosition = ColorMapper.availableColors.size - 1
        val firstIcon = iconMapper.availableIconsNames.values.first()
        val lastIcon = iconMapper.availableIconsNames.values.last()
        val lastIconPosition = iconMapper.availableIconsNames.size - 1

        NavUtils.openRunningRecordsScreen()
        clickOnView(withText(R.string.running_records_add_type))

        // Choosers are empty
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Typing name
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        checkPreviewUpdated(withText(name))

        // Hide keyboard
        pressBack()

        // Open color chooser
        clickOnView(withText(R.string.change_record_type_color_hint))
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Selecting color
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(firstColor))
        checkPreviewUpdated(withCardColor(firstColor))

        // Selecting color
        scrollToPosition(R.id.rvChangeRecordTypeColor, lastColorPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeColor, withCardColor(lastColor))
        checkPreviewUpdated(withCardColor(lastColor))

        // Open icon chooser
        clickOnView(withText(R.string.change_record_type_icon_hint))
        checkViewIsNotDisplayed(withId(R.id.rvChangeRecordTypeColor))
        checkViewIsDisplayed(withId(R.id.rvChangeRecordTypeIcon))

        // Selecting icon
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTagValue(equalTo(firstIcon)))
        checkPreviewUpdated(withTagValue(equalTo(firstIcon)))

        // Selecting icon
        scrollToPosition(R.id.rvChangeRecordTypeIcon, lastIconPosition)
        clickOnRecyclerItem(R.id.rvChangeRecordTypeIcon, withTagValue(equalTo(lastIcon)))
        checkPreviewUpdated(withTagValue(equalTo(lastIcon)))

        clickOnView(withText(R.string.change_record_type_save))

        // Record type added
        checkViewIsDisplayed(withText(name))
        checkViewIsDisplayed(withCardColor(lastColor))
        checkViewIsDisplayed(withTagValue(equalTo(lastIcon)))
    }

    private fun checkPreviewUpdated(matcher: Matcher<View>) =
        checkViewIsDisplayed(
            allOf(isDescendantOfA(withId(R.id.previewChangeRecordType)), matcher)
        )
}
