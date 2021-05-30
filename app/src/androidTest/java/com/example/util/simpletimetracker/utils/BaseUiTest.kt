package com.example.util.simpletimetracker.utils

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.util.simpletimetracker.TimeTrackerApp
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerTestAppComponent
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment
import com.example.util.simpletimetracker.ui.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

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

    @Rule
    @JvmField
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val mRetryTestRule = RetryTestRule()

    val firstColor: Int
        get() = ColorMapper.getAvailableColors().first()
    val lastColor: Int
        get() = ColorMapper.getAvailableColors().last()
    val firstIcon: Int
        get() = iconImageMapper.availableIconsNames.values.first()
    val lastIcon: Int
        get() = iconImageMapper.availableIconsNames.values.last()
    val firstEmoji: String
        get() = iconEmojiMapper
            .getAvailableEmojis()[iconEmojiMapper.getAvailableEmojiCategories().first()]
            ?.first().orEmpty()
    val lastEmoji: String
        get() = iconEmojiMapper
            .getAvailableEmojis()[iconEmojiMapper.getAvailableEmojiCategories().last()]
            ?.last().orEmpty()

    init {
        inject()
    }

    @Before
    open fun setUp() {
        clearData()
        disableAnimations()
        registerIdlingResource()
    }

    @After
    open fun after() {
        enableAnimations()
        unregisterIdlingResource()
    }

    private fun inject() {
        val app = ApplicationProvider.getApplicationContext() as TimeTrackerApp
        DaggerTestAppComponent.builder()
            .appModule(AppModule(app))
            .build()
            .inject(this)
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