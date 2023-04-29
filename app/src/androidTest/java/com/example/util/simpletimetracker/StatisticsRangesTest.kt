package com.example.util.simpletimetracker

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.tryAction
import dagger.hilt.android.testing.HiltAndroidTest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_base_adapter.R as baseR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_statistics.R as statisticsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StatisticsRangesTest : BaseUiTest() {

    @Test
    fun statisticsRanges() {
        val name = "Test"
        val calendar = Calendar.getInstance()

        // Add data
        testUtils.addActivity(name)
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis - TimeUnit.MINUTES.toMillis(1),
            timeEnded = calendar.timeInMillis
        )

        // Statistics day range
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to week range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(coreR.string.range_select_day))
        clickOnViewWithText(coreR.string.range_week)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to month range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(coreR.string.range_select_week))
        clickOnViewWithText(coreR.string.range_month)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to year range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(coreR.string.range_select_month))
        clickOnViewWithText(coreR.string.range_year)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerPrevious)
        longClickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerNext)
        checkViewIsDisplayed(allOf(withText(coreR.string.statistics_empty), isCompletelyDisplayed()))

        // Switch to overall range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(coreR.string.range_select_year))
        clickOnViewWithText(coreR.string.range_overall)
        Thread.sleep(1000)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsNotDisplayed(withId(statisticsR.id.btnStatisticsContainerPrevious))
        checkViewIsNotDisplayed(withId(statisticsR.id.btnStatisticsContainerNext))

        // Switch to custom range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewDoesNotExist(withText(coreR.string.range_select_day))
        checkViewDoesNotExist(withText(coreR.string.range_select_week))
        checkViewDoesNotExist(withText(coreR.string.range_select_month))
        checkViewDoesNotExist(withText(coreR.string.range_select_year))
        clickOnViewWithText(coreR.string.range_custom)
        tryAction { clickOnViewWithId(dialogsR.id.btnCustomRangeSelection) }
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsNotDisplayed(withId(statisticsR.id.btnStatisticsContainerPrevious))
        checkViewIsNotDisplayed(withId(statisticsR.id.btnStatisticsContainerNext))

        // Switch to last days range
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewDoesNotExist(withText(coreR.string.range_select_day))
        checkViewDoesNotExist(withText(coreR.string.range_select_week))
        checkViewDoesNotExist(withText(coreR.string.range_select_month))
        checkViewDoesNotExist(withText(coreR.string.range_select_year))
        clickOnViewWithText(coreR.string.range_last)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
        checkViewIsNotDisplayed(withId(statisticsR.id.btnStatisticsContainerPrevious))
        checkViewIsNotDisplayed(withId(statisticsR.id.btnStatisticsContainerNext))

        // Switch back to day
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewDoesNotExist(withText(coreR.string.range_select_day))
        checkViewDoesNotExist(withText(coreR.string.range_select_week))
        checkViewDoesNotExist(withText(coreR.string.range_select_month))
        checkViewDoesNotExist(withText(coreR.string.range_select_year))
        clickOnViewWithText(coreR.string.range_day)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        checkViewIsDisplayed(withText(coreR.string.range_select_day))
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
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_day)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(coreR.string.title_yesterday), isCompletelyDisplayed()))

        // Check tomorrow
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_day)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(coreR.string.title_tomorrow), isCompletelyDisplayed()))
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
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_day)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_day)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    @Test
    fun selectNearDateForWeeks() {
        testUtils.setFirstDayOfWeek(DayOfWeek.SUNDAY)
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        val titlePrev = timeMapper.toWeekTitle(-1, 0, DayOfWeek.SUNDAY)
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
        val titleNext = timeMapper.toWeekTitle(1, 0, DayOfWeek.SUNDAY)

        // Check prev week
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next week
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    @Test
    fun selectFarDateForWeeks() {
        testUtils.setFirstDayOfWeek(DayOfWeek.SUNDAY)
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, -2500)
        }
        val titlePrev = timeMapper.toWeekTitle(-2500, 0, DayOfWeek.SUNDAY)
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, 2500)
        }
        val titleNext = timeMapper.toWeekTitle(2500, 0, DayOfWeek.SUNDAY)

        // Check prev date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    @Test
    fun selectLastWeekOfYear() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1960)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 31)
        }
        val titlePrev = toWeekDateTitle(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2060)
            set(Calendar.MONTH, 11)
            set(Calendar.DAY_OF_MONTH, 31)
        }
        val titleNext = toWeekDateTitle(calendarNext.timeInMillis)

        // Check prev date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    @Test
    fun selectFirstWeekOfYear() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1961)
            set(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titlePrev = toWeekDateTitle(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2061)
            set(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titleNext = toWeekDateTitle(calendarNext.timeInMillis)

        // Check prev date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_week)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_week)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
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
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_month)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_month)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next month
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_month)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
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
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_month)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_month)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_month)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    @Test
    fun selectNearDateForYears() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.YEAR, -1)
        }
        val titlePrev = yearTitleFormat.format(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.YEAR, 1)
        }
        val titleNext = yearTitleFormat.format(calendarNext.timeInMillis)

        // Check prev months
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_year)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_year)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next month
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_year)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    @Test
    fun selectFarDateForYears() {
        NavUtils.openStatisticsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1950)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titlePrev = yearTitleFormat.format(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2050)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titleNext = yearTitleFormat.format(calendarNext.timeInMillis)

        // Check prev date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_year)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_year)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarPrev.get(Calendar.YEAR),
                calendarPrev.get(Calendar.MONTH) + 1,
                calendarPrev.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_select_year)
        onView(withClassName(equalTo(DatePicker::class.java.name))).perform(
            setDate(
                calendarNext.get(Calendar.YEAR),
                calendarNext.get(Calendar.MONTH) + 1,
                calendarNext.get(Calendar.DAY_OF_MONTH)
            )
        )
        clickOnViewWithId(dialogsR.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    @Test
    fun customRange() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add data
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        var calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
            set(Calendar.HOUR_OF_DAY, 15)
        }
        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )
        calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -2)
            set(Calendar.HOUR_OF_DAY, 15)
        }
        testUtils.addRecord(
            typeName = name2,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        )

        // Check custom range not selected
        NavUtils.openStatisticsScreen()
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_day)
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_custom)
        pressBack()
        tryAction {
            checkViewIsDisplayed(
                allOf(withId(statisticsR.id.btnStatisticsContainerToday), withText(coreR.string.title_today))
            )
        }

        // Select custom range default
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_custom)
        clickOnViewWithId(dialogsR.id.btnCustomRangeSelection)
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.btnStatisticsContainerToday), withText(coreR.string.range_custom))
        )
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))

        // Select custom range yesterday
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_custom)
        calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        NavUtils.setCustomRange(
            yearStarted = calendar.get(Calendar.YEAR),
            monthStarted = calendar.get(Calendar.MONTH),
            dayStarted = calendar.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendar.get(Calendar.YEAR),
            monthEnded = calendar.get(Calendar.MONTH),
            dayEnded = calendar.get(Calendar.DAY_OF_MONTH),
        )

        // Check statistics
        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 23)
        checkStatisticsItem(name = name1, hours = 1)
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))

        // Check time set
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_custom)
        var timeStarted = calendar.timeInMillis.let(timeMapper::formatDateYear)
        var timeEnded = timeStarted

        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCustomRangeSelectionTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCustomRangeSelectionTimeEnded), withText(timeEnded)))

        // Select custom range three days
        var calendarStart = Calendar.getInstance().apply {
            add(Calendar.DATE, -3)
        }
        NavUtils.setCustomRange(
            yearStarted = calendarStart.get(Calendar.YEAR),
            monthStarted = calendarStart.get(Calendar.MONTH),
            dayStarted = calendarStart.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendar.get(Calendar.YEAR),
            monthEnded = calendar.get(Calendar.MONTH),
            dayEnded = calendar.get(Calendar.DAY_OF_MONTH),
        )

        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 69)
        checkStatisticsItem(name = name1, hours = 1)
        checkStatisticsItem(name = name2, hours = 2)

        // Check time set
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_custom)
        timeStarted = calendarStart.timeInMillis.let(timeMapper::formatDateYear)
        timeEnded = calendar.timeInMillis.let(timeMapper::formatDateYear)

        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCustomRangeSelectionTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCustomRangeSelectionTimeEnded), withText(timeEnded)))

        // Select custom range long time ago
        calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -11)
        }
        calendarStart = Calendar.getInstance().apply {
            add(Calendar.DATE, -20)
        }
        NavUtils.setCustomRange(
            yearStarted = calendarStart.get(Calendar.YEAR),
            monthStarted = calendarStart.get(Calendar.MONTH),
            dayStarted = calendarStart.get(Calendar.DAY_OF_MONTH),
            yearEnded = calendar.get(Calendar.YEAR),
            monthEnded = calendar.get(Calendar.MONTH),
            dayEnded = calendar.get(Calendar.DAY_OF_MONTH),
        )

        checkStatisticsItem(nameResId = coreR.string.untracked_time_name, hours = 240)
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))

        // Check time set
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_custom)
        timeStarted = calendarStart.timeInMillis.let(timeMapper::formatDateYear)
        timeEnded = calendar.timeInMillis.let(timeMapper::formatDateYear)

        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCustomRangeSelectionTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(dialogsR.id.tvCustomRangeSelectionTimeEnded), withText(timeEnded)))
    }

    @Test
    fun lastDaysRange() {
        val name1 = "Test1"

        // Add data
        testUtils.addActivity(name1)
        var calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
        }
        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )
        calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -6)
            set(Calendar.HOUR_OF_DAY, 15)
        }
        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )
        calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -7)
            set(Calendar.HOUR_OF_DAY, 15)
        }
        testUtils.addRecord(
            typeName = name1,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Select range
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(statisticsR.id.btnStatisticsContainerToday)
        clickOnViewWithText(coreR.string.range_last)
        checkViewIsDisplayed(
            allOf(withId(statisticsR.id.btnStatisticsContainerToday), withText(coreR.string.range_last))
        )
        checkStatisticsItem(name = name1, hours = 2)
    }

    private fun checkStatisticsItem(
        name: String = "",
        nameResId: Int? = null,
        hours: Int,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(baseR.id.viewStatisticsItem),
                hasDescendant(if (nameResId != null) withText(nameResId) else withText(name)),
                hasDescendant(withSubstring("$hours$hourString 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
    }

    private fun toWeekDateTitle(timestamp: Long): String {
        val calendar = Calendar.getInstance()

        calendar.apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            setWeekToFirstDay()
        }
        val rangeStart = calendar.timeInMillis
        val rangeEnd = calendar.apply { add(Calendar.DATE, 6) }.timeInMillis

        return weekTitleFormat.format(rangeStart) + " - " + weekTitleFormat.format(rangeEnd)
    }

    companion object {
        private val dayTitleFormat = SimpleDateFormat("E, MMM d", Locale.getDefault())
        private val weekTitleFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        private val monthTitleFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        private val yearTitleFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    }
}
