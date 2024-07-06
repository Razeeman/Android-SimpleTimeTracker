package com.example.util.simpletimetracker

import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import com.example.util.simpletimetracker.core.R as coreR
import com.example.util.simpletimetracker.feature_dialogs.R as dialogsR
import com.example.util.simpletimetracker.feature_pomodoro.R as pomodoroR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PomodoroTest : BaseUiTest() {

    private val focusState by lazy { getString(R.string.pomodoro_state_focus) }
    private val breakState by lazy { getString(R.string.pomodoro_state_break) }
    private val longBreakState by lazy { getString(R.string.pomodoro_state_long_break) }

    override fun setUp() {
        super.setUp()
        runBlocking { prefsInteractor.setEnablePomodoroMode(true) }
    }

    @Test
    fun pomodoroTimeState() {
        NavUtils.openPomodoro()

        // Hours not visible
        checkViewIsNotDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerHours)))
        checkViewIsNotDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerHoursLegend)))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerMinutes), withText("25")))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerMinutesLegend)))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerSeconds), withText("00")))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerSecondsLegend)))

        // Add hours
        NavUtils.openPomodoroSettings()
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_focus)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        pressBack()

        // Hours visible
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerHours), withText("02")))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerHoursLegend)))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerMinutes), withText("50")))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerMinutesLegend)))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerSeconds), withText("00")))
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerSecondsLegend)))
    }

    @Test
    fun settingsFocusTime() {
        NavUtils.openPomodoro()
        NavUtils.openPomodoroSettings()

        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_focus)
        checkViewIsNotDisplayed(withText(R.string.duration_dialog_disable))
        repeat(4) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard2)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard5)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("25$secondString"))
        pressBack()
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerSeconds), withText("25")))
    }

    @Test
    fun settingsBreakTime() {
        NavUtils.openPomodoro()
        NavUtils.openPomodoroSettings()
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_break)
        repeat(4) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard5)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("5$secondString"))
        pressBack()
        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
        clickOnViewWithId(pomodoroR.id.btnPomodoroNext)
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerSeconds), withText("04")))
        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
    }

    @Test
    fun settingsLongBreak() {
        NavUtils.openPomodoro()
        NavUtils.openPomodoroSettings()

        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_long_break)
        repeat(4) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard5)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("15$secondString"))
        pressBack()
        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
        repeat(7) { clickOnViewWithId(pomodoroR.id.btnPomodoroNext) }
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerSeconds), withText("14")))
        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
    }

    @Test
    fun settingsPeriods() {
        NavUtils.openPomodoro()
        NavUtils.openPomodoroSettings()

        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_settings_periods_until_long_break)
        repeat(4) { clickOnViewWithId(dialogsR.id.btnNumberKeyboardDelete) }
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard2)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        checkViewIsDisplayed(withText("2"))
        pressBack()
        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
        repeat(3) { clickOnViewWithId(pomodoroR.id.btnPomodoroNext) }
        checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerMinutes), withText("14")))
        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
    }

    @Test
    fun settingsDisabled() {
        runBlocking { prefsInteractor.setPomodoroPeriodsUntilLongBreak(2) }
        NavUtils.openPomodoro()

        // Disable break
        NavUtils.openPomodoroSettings()
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_break)
        clickOnViewWithText(R.string.duration_dialog_disable)
        checkViewIsDisplayed(withText("0$secondString"))
        pressBack()
        checkStates(
            "24" to focusState,
            "24" to focusState,
            "14" to longBreakState,
            "24" to focusState,
        )

        // Disable long break
        NavUtils.openPomodoroSettings()
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_break)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard5)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_long_break)
        clickOnViewWithText(R.string.duration_dialog_disable)
        checkViewIsDisplayed(withText("0$secondString"))
        pressBack()
        checkStates(
            "24" to focusState,
            "04" to breakState,
            "24" to focusState,
            "24" to focusState,
            "04" to breakState,
        )

        // Disable periods until long break
        NavUtils.openPomodoroSettings()
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_long_break)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard1)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard5)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard0)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_settings_periods_until_long_break)
        clickOnViewWithText(R.string.duration_dialog_disable)
        checkViewIsDisplayed(withText(R.string.settings_inactivity_reminder_disabled))
        pressBack()
        checkStates(
            "24" to focusState,
            "04" to breakState,
            "24" to focusState,
            "04" to breakState,
        )

        // Disable all durations
        NavUtils.openPomodoroSettings()
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_settings_periods_until_long_break)
        clickOnViewWithId(dialogsR.id.tvNumberKeyboard2)
        clickOnViewWithText(coreR.string.duration_dialog_save)
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_break)
        clickOnViewWithId(dialogsR.id.btnDurationPickerDisable)
        clickOnSettingsSelectorBesideText(coreR.string.pomodoro_state_long_break)
        clickOnViewWithId(dialogsR.id.btnDurationPickerDisable)
        pressBack()
        checkStates(
            "24" to focusState,
            "24" to focusState,
            "24" to focusState,
            "24" to focusState,
        )
    }

    @Test
    fun pomodoroButtonsVisibility() {
        NavUtils.openPomodoro()

        // Check visibility
        checkViewIsDisplayed(withId(pomodoroR.id.btnPomodoroStart))
        checkViewIsNotDisplayed(withId(pomodoroR.id.btnPomodoroRestart))
        checkViewIsNotDisplayed(withId(pomodoroR.id.btnPomodoroNext))

        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
        checkViewIsDisplayed(withId(pomodoroR.id.btnPomodoroRestart))
        checkViewIsDisplayed(withId(pomodoroR.id.btnPomodoroNext))

        clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
        checkViewIsNotDisplayed(withId(pomodoroR.id.btnPomodoroRestart))
        checkViewIsNotDisplayed(withId(pomodoroR.id.btnPomodoroNext))
    }

    @Test
    fun pomodoroButtons() {
        runBlocking { prefsInteractor.setPomodoroPeriodsUntilLongBreak(3) }
        NavUtils.openPomodoro()

        checkStates(
            "24" to focusState,
            "04" to breakState,
            "24" to focusState,
            "04" to breakState,
            "24" to focusState,
            "14" to longBreakState,
            startBefore = true,
            stopAfter = false,
        )
        clickOnViewWithId(pomodoroR.id.btnPomodoroRestart)
        clickOnViewWithId(pomodoroR.id.btnPomodoroRestart)
        checkStates(
            "24" to focusState,
            "04" to breakState,
            "24" to focusState,
            "04" to breakState,
            "24" to focusState,
            "14" to longBreakState,
            startBefore = false,
            stopAfter = false,
        )
    }

    private fun checkStates(
        vararg states: Pair<String, String>, // time to focus name
        startBefore: Boolean = true,
        stopAfter: Boolean = true,
    ) {
        states.forEachIndexed { index, state ->
            if (index == 0 && startBefore) {
                clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
            } else {
                clickOnViewWithId(pomodoroR.id.btnPomodoroNext)
            }
            checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroTimerMinutes), withText(state.first)))
            checkViewIsDisplayed(allOf(withId(pomodoroR.id.tvPomodoroCycleHint), withText(state.second)))
        }
        if (stopAfter) clickOnViewWithId(pomodoroR.id.btnPomodoroStart)
    }
}
