package com.example.util.simpletimetracker.utils

import android.content.Context
import androidx.annotation.ColorInt
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment
import com.example.util.simpletimetracker.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
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
        get() = iconImageMapper.getAvailableImages().toList().first().let { (_, imagesMap) ->
            imagesMap.toList().first().let { (_, resId) -> resId }
        }
    val lastIcon: Int
        get() = iconImageMapper.getAvailableImages().toList().last().let { (_, imagesMap) ->
            imagesMap.toList().last().let { (_, resId) -> resId }
        }
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

    init {
        val app = ApplicationProvider.getApplicationContext() as Context
        val config = BundledEmojiCompatConfig(app).setReplaceAll(true)
        EmojiCompat.init(config)
    }

    @Before
    open fun setUp() {
        if (!this::testUtils.isInitialized) hiltRule.inject()
        clearData()
        disableAnimations()
        registerIdlingResource()
    }

    @After
    open fun after() {
        enableAnimations()
        unregisterIdlingResource()
    }

    internal fun getString(id: Int): String {
        return InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
    }

    internal fun getString(id: Int, vararg args: Any): String {
        return InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)
    }

    @ColorInt internal fun getColor(id: Int): Int {
        return InstrumentationRegistry.getInstrumentation().targetContext.getColor(id)
    }

    private fun clearData() {
        testUtils.clearDatabase()
        testUtils.clearPrefs()
    }

    private fun disableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = false
        StatisticsContainerFragment.viewPagerSmoothScroll = false
    }

    private fun enableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = true
        StatisticsContainerFragment.viewPagerSmoothScroll = true
    }

    private fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(CountingIdlingResourceProvider.countingIdlingResource)
    }

    private fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(CountingIdlingResourceProvider.countingIdlingResource)
    }
}