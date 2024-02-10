package com.example.util.simpletimetracker.utils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment
import com.example.util.simpletimetracker.feature_views.pieChart.PieChartView
import com.example.util.simpletimetracker.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.Calendar
import javax.inject.Inject

@HiltAndroidTest
open class BaseUiTest {

    @Inject
    lateinit var testUtils: TestUtils

    @Inject
    lateinit var iconImageMapper: IconImageMapper

    @Inject
    lateinit var iconEmojiMapper: IconEmojiMapper

    @Inject
    lateinit var timeMapper: TimeMapper

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    @Inject
    lateinit var languageInteractor: LanguageInteractor

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val mRetryTestRule = RetryTestRule()

    @get:Rule(order = 2)
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    val firstColor: Int
        get() = ColorMapper.getAvailableColors().first()
    val lastColor: Int
        get() = ColorMapper.getAvailableColors().last()
    val firstIcon: Int
        get() = iconImageMapper.getAvailableImages(loadSearchHints = false)
            .toList().first()
            .let { (_, imagesMap) -> imagesMap.first().iconResId }
    val lastIcon: Int
        get() = iconImageMapper.getAvailableImages(loadSearchHints = false)
            .toList().last()
            .let { (_, imagesMap) -> imagesMap.last().iconResId }
    val firstEmoji: String
        get() = iconEmojiMapper
            .getAvailableEmojis()[iconEmojiMapper.getAvailableEmojiCategories().first()]
            ?.first().orEmpty()
    val lastEmoji: String
        get() = iconEmojiMapper
            .getAvailableEmojis()[iconEmojiMapper.getAvailableEmojiCategories().last()]
            ?.last().orEmpty()

    val hourString: String by lazy { getString(R.string.time_hour) }
    val minuteString: String by lazy { getString(R.string.time_minute) }
    val secondString: String by lazy { getString(R.string.time_second) }

    val calendar: Calendar = Calendar.getInstance()

    init {
        val app = ApplicationProvider.getApplicationContext() as Context
        val config = BundledEmojiCompatConfig(app).setReplaceAll(true)
        EmojiCompat.init(config)
    }

    @Before
    open fun setUp() {
        if (!this::testUtils.isInitialized) hiltRule.inject()
        clearData()
        runBlocking { prefsInteractor.setShowUntrackedInRecords(true) }
        runBlocking { prefsInteractor.setShowUntrackedInStatistics(true) }
        disableAnimations()
        registerIdlingResource()
    }

    @After
    open fun after() {
        enableAnimations()
        unregisterIdlingResource()
    }

    internal fun getString(id: Int): String {
        return InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getString(id)
    }

    internal fun getString(id: Int, vararg args: Any): String {
        return InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getString(id, *args)
    }

    @ColorInt
    internal fun getColor(id: Int): Int {
        return InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getColor(id)
    }

    internal fun Long.formatTime(): String {
        return timeMapper.formatTime(time = this, useMilitaryTime = true, showSeconds = false)
    }

    internal fun Long.formatDateTime(): String {
        return timeMapper.formatDateTime(time = this, useMilitaryTime = true, showSeconds = false)
    }

    internal fun Long.formatInterval(): String {
        return timeMapper.formatInterval(interval = this, forceSeconds = false, useProportionalMinutes = false)
    }

    private fun clearData() {
        testUtils.clearDatabase()
        testUtils.clearPrefs()
    }

    private fun disableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = false
        StatisticsContainerFragment.viewPagerSmoothScroll = false
        PieChartView.disableAnimationsForTest = true
    }

    private fun enableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = true
        StatisticsContainerFragment.viewPagerSmoothScroll = true
        PieChartView.disableAnimationsForTest = false
    }

    private fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(CountingIdlingResourceProvider.countingIdlingResource)
    }

    private fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(CountingIdlingResourceProvider.countingIdlingResource)
    }
}