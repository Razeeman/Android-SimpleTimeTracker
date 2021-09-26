package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.feature_change_category.view.ChangeCategoryFragment
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import com.example.util.simpletimetracker.feature_change_record_tag.view.ChangeRecordTagFragment
import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import com.example.util.simpletimetracker.feature_change_running_record.view.ChangeRunningRecordFragment
import com.example.util.simpletimetracker.feature_records_all.view.RecordsAllFragment
import com.example.util.simpletimetracker.feature_statistics_detail.view.StatisticsDetailFragment
import com.example.util.simpletimetracker.navigation.Screen
import com.example.util.simpletimetracker.navigation.params.NavigationData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
class NavigationScreenMapModule {

    @IntoMap
    @Provides
    @ScreenKey(Screen.CHANGE_RECORD_TYPE)
    fun changeRecordType(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordTypeFragment,
            ChangeRecordTypeFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CHANGE_RECORD_RUNNING)
    fun changeRecordRunning(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRunningRecordFragment,
            ChangeRunningRecordFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CHANGE_RECORD_FROM_MAIN)
    fun changeRecordFromMain(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordFragment,
            ChangeRecordFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CHANGE_RECORD_FROM_RECORDS_ALL)
    fun changeRecordFromRecordsAll(): NavigationData {
        return NavigationData(
            R.id.action_recordsAllFragment_to_changeRecordFragment,
            ChangeRecordFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.STATISTICS_DETAIL)
    fun statisticsDetail(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_statisticsDetailFragment,
            StatisticsDetailFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.RECORDS_ALL)
    fun recordsAll(): NavigationData {
        return NavigationData(
            R.id.action_statisticsDetailFragment_to_recordsAllFragment,
            RecordsAllFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CATEGORIES)
    fun categories(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_categoriesFragment,
            null
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.ARCHIVE)
    fun archive(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_archiveFragment,
            null
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CHANGE_CATEGORY)
    fun changeCategory(): NavigationData {
        return NavigationData(
            R.id.action_categoriesFragment_to_changeCategoryFragment,
            ChangeCategoryFragment::createBundle
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(Screen.CHANGE_RECORD_TAG)
    fun changeRecordTag(): NavigationData {
        return NavigationData(
            R.id.action_categoriesFragment_to_changeRecordTagFragment,
            ChangeRecordTagFragment::createBundle
        )
    }
}