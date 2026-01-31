package com.example.util.simpletimetracker.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.ColorInt
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.base.DelayLoadHandler
import com.example.util.simpletimetracker.core.interactor.LanguageInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.utils.CountingIdlingResourceProvider
import com.example.util.simpletimetracker.core.utils.TestUtils
import com.example.util.simpletimetracker.domain.backup.repo.BackupRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.complexRule.repo.ComplexRuleRepo
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.mapper.RangeMapper
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeRepo
import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_settings.viewModel.delegate.SettingsFileWorkDelegate
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment
import com.example.util.simpletimetracker.feature_views.pieChart.PieChartView
import com.example.util.simpletimetracker.navigation.ScreenResolver
import com.example.util.simpletimetracker.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.Calendar
import javax.inject.Inject

@Suppress("ReplaceGetOrSet")
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
    lateinit var rangeMapper: RangeMapper

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    @Inject
    lateinit var languageInteractor: LanguageInteractor

    @Inject
    lateinit var complexRuleRepo: ComplexRuleRepo

    @Inject
    lateinit var backupRepo: BackupRepo

    @Inject
    lateinit var recordTypeRepo: RecordTypeRepo

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val mRetryTestRule = RetryTestRule()

    @get:Rule(order = 2)
    val permissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            GrantPermissionRule.grant()
        }

    @get:Rule(order = 3)
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    val firstColor: Int
        get() = ColorMapper.getAvailableColors().first()
    val lastColor: Int
        get() = ColorMapper.getAvailableColors().last()
    val firstIcon: Int
        get() = iconImageMapper.getAvailableImages(loadSearchHints = false)
            .get(iconImageMapper.getAvailableCategories(hasFavourites = false).first())
            .orEmpty().first().iconResId
    val lastIcon: Int
        get() = iconImageMapper.getAvailableImages(loadSearchHints = false)
            .get(iconImageMapper.getAvailableCategories(hasFavourites = false).last())
            .orEmpty().last().iconResId
    val firstEmoji: String
        get() = iconEmojiMapper
            .getAvailableEmojis(loadSearchHints = false)
            .get(iconEmojiMapper.getAvailableEmojiCategories(hasFavourites = false).first())
            ?.first()?.emojiCode.orEmpty()
    val lastEmoji: String
        get() = iconEmojiMapper
            .getAvailableEmojis(loadSearchHints = false)
            .get(iconEmojiMapper.getAvailableEmojiCategories(hasFavourites = false).last())
            ?.last()?.emojiCode.orEmpty()

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
        testUtils.clearDatabase()
        testUtils.clearPrefs()
        runBlocking {
            prefsInteractor.setShowUntrackedInRecords(true)
            prefsInteractor.setShowUntrackedInStatistics(true)
            prefsInteractor.setTypeAdditionalFieldsShown(true)
            prefsInteractor.setCategoryAdditionalFieldsShown(true)
            prefsInteractor.setTagAdditionalFieldsShown(true)
        }
        disableAnimations()
        registerIdlingResource()
    }

    @After
    open fun after() {
        testUtils.clearPrefs()
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

    internal fun getQuantityString(id: Int, quantity: Int, vararg args: Any): String {
        return InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getQuantityString(id, quantity, *args)
    }

    @ColorInt
    internal fun getColor(id: Int): Int {
        return InstrumentationRegistry.getInstrumentation().targetContext.resources
            .getColor(id)
    }

    internal fun Long.formatTime(): String {
        return timeMapper.formatTime(time = this, useMilitaryTime = true, showSeconds = false)
    }

    internal fun Long.formatDate(): String {
        return timeMapper.formatDate(time = this)
    }

    internal fun Long.formatDateTime(): String {
        return timeMapper.formatDateTime(time = this, useMilitaryTime = true, showSeconds = false)
    }

    internal fun Long.formatDateTimeYear(): String {
        return timeMapper.formatDateTimeYear(time = this, useMilitaryTime = true)
    }

    internal fun Long.formatInterval(): String {
        return timeMapper.formatInterval(interval = this, forceSeconds = false, durationFormat = DurationFormat.HOURS)
    }

    private fun disableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = false
        StatisticsContainerFragment.viewPagerSmoothScroll = false
        PieChartView.disableAnimationsForTest = true
        ScreenResolver.disableAnimationsForTest = true
        SettingsFileWorkDelegate.restartAppIsBlocked = true
        DelayLoadHandler.disableForTest = true
    }

    private fun enableAnimations() {
        RecordsContainerFragment.viewPagerSmoothScroll = true
        StatisticsContainerFragment.viewPagerSmoothScroll = true
        PieChartView.disableAnimationsForTest = false
        ScreenResolver.disableAnimationsForTest = false
        SettingsFileWorkDelegate.restartAppIsBlocked = false
        DelayLoadHandler.disableForTest = false
    }

    private fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(CountingIdlingResourceProvider.countingIdlingResource)
    }

    private fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(CountingIdlingResourceProvider.countingIdlingResource)
    }
}