package com.example.util.simpletimetracker

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.typeTextIntoView
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class StatisticsRangesTest : BaseUiTest() {

    @Test
    fun statisticsRanges() {
        val name = "Test"

        // Add activity
        NavUtils.openRunningRecordsScreen()
        clickOnView(withText(R.string.running_records_add_type))
        typeTextIntoView(R.id.etChangeRecordTypeName, name)
        clickOnView(withText(R.string.change_record_type_save))

        // Start timer
        clickOnViewWithText(name)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))

        // Statistics
        NavUtils.openStatisticsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to week range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(R.string.title_select_day))
        clickOnViewWithText(R.string.title_this_week)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to month range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(R.string.title_select_week))
        clickOnViewWithText(R.string.title_this_month)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to overall range
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(R.string.title_select_month))
        clickOnViewWithText(R.string.title_overall)
        Thread.sleep(1000)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsNotDisplayed(withId(R.id.btnStatisticsContainerPrevious))
        checkViewIsNotDisplayed(withId(R.id.btnStatisticsContainerNext))

        // Switch back to day
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        checkViewDoesNotExist(withText(R.string.title_select_day))
        checkViewDoesNotExist(withText(R.string.title_select_week))
        checkViewDoesNotExist(withText(R.string.title_select_month))
        clickOnView(allOf(withId(R.id.btnStatisticsItemRange), withText(R.string.title_today)))
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(R.string.title_select_day))
    }

    @Test
    fun selectNearDateForDays() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.DATE, 1)
        }

        // Check yesterday
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_day)) // unconstrained because buttons have negative margin
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(R.string.title_yesterday),
                isCompletelyDisplayed()
            )
        )

        // Check tomorrow
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_day))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(R.string.title_tomorrow),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun selectFarDateForDays() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1950)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titlePrev = dayTitleFormat.format(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2050)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titleNext = dayTitleFormat.format(calendarNext.timeInMillis)

        // Check prev date
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_day))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titlePrev),
                isCompletelyDisplayed()
            )
        )

        // Check next date
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_day))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titleNext),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun selectNearDateForWeeks() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        val titlePrev = timeMapper.toWeekTitle(-1)
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
        val titleNext = timeMapper.toWeekTitle(1)

        // Check prev week
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_week)
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_week))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titlePrev),
                isCompletelyDisplayed()
            )
        )

        // Check next week
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_week))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titleNext),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun selectFarDateForWeeks() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -2500)
        }
        val titlePrev = timeMapper.toWeekTitle(-2500)
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, 2500)
        }
        val titleNext = timeMapper.toWeekTitle(2500)

        // Check prev date
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_week)
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_week))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titlePrev),
                isCompletelyDisplayed()
            )
        )

        // Check next date
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_week))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titleNext),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun selectNearDateForMonths() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.MONTH, -1)
        }
        val titlePrev = monthTitleFormat.format(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
        }
        val titleNext = monthTitleFormat.format(calendarNext.timeInMillis)

        // Check prev months
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_month)
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_month))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titlePrev),
                isCompletelyDisplayed()
            )
        )

        // Check next month
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_month))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titleNext),
                isCompletelyDisplayed()
            )
        )
    }

    @Test
    fun selectFarDateForMonths() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1950)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titlePrev = monthTitleFormat.format(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2050)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titleNext = monthTitleFormat.format(calendarNext.timeInMillis)

        // Check prev date
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.title_this_month)
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_month))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titlePrev),
                isCompletelyDisplayed()
            )
        )

        // Check next date
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        unconstrainedClickOnView(withText(R.string.title_select_month))
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                PickerActions.setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(
            allOf(
                withText(titleNext),
                isCompletelyDisplayed()
            )
        )
    }

    companion object {
        private val dayTitleFormat = SimpleDateFormat("E, MMM d", Locale.US)
        private val monthTitleFormat = SimpleDateFormat("MMMM", Locale.US)
    }
}
