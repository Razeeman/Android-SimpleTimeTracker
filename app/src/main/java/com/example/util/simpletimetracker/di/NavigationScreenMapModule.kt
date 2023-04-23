package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.feature_change_activity_filter.view.ChangeActivityFilterFragment
import com.example.util.simpletimetracker.feature_change_category.view.ChangeCategoryFragment
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import com.example.util.simpletimetracker.feature_change_record_tag.view.ChangeRecordTagFragment
import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import com.example.util.simpletimetracker.feature_change_running_record.view.ChangeRunningRecordFragment
import com.example.util.simpletimetracker.feature_records_all.view.RecordsAllFragment
import com.example.util.simpletimetracker.feature_statistics_detail.view.StatisticsDetailFragment
import com.example.util.simpletimetracker.navigation.NavigationData
import com.example.util.simpletimetracker.navigation.bundleCreator.BundleCreator
import com.example.util.simpletimetracker.navigation.bundleCreator.bundleCreatorDelegate
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeActivityFilterParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromChangeActivityParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryFromTagsParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromRecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagFromTagsParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.DataEditParams
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
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
    @ScreenKey(ChangeRecordTypeParams.Change::class)
    fun changeRecordTypeChange(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordTypeFragment,
            bundleCreatorDelegate(ChangeRecordTypeFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeRecordTypeParams.New::class)
    fun changeRecordTypeNew(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordTypeFragment,
            bundleCreatorDelegate(ChangeRecordTypeFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeRunningRecordParams::class)
    fun changeRecordRunning(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRunningRecordFragment,
            bundleCreatorDelegate(ChangeRunningRecordFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeRecordFromMainParams::class)
    fun changeRecordFromMain(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordFragment,
            bundleCreatorDelegate(ChangeRecordFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeRecordFromRecordsAllParams::class)
    fun changeRecordFromRecordsAll(): NavigationData {
        return NavigationData(
            R.id.action_recordsAllFragment_to_changeRecordFragment,
            bundleCreatorDelegate(ChangeRecordFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(StatisticsDetailParams::class)
    fun statisticsDetail(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_statisticsDetailFragment,
            bundleCreatorDelegate(StatisticsDetailFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(RecordsAllParams::class)
    fun recordsAll(): NavigationData {
        return NavigationData(
            R.id.action_statisticsDetailFragment_to_recordsAllFragment,
            bundleCreatorDelegate(RecordsAllFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(CategoriesParams::class)
    fun categories(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_categoriesFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ArchiveParams::class)
    fun archive(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_archiveFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(DataEditParams::class)
    fun dataEdit(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_dataEditFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeCategoryFromTagsParams::class)
    fun changeCategoryFromTags(): NavigationData {
        return NavigationData(
            R.id.action_categoriesFragment_to_changeCategoryFragment,
            bundleCreatorDelegate(ChangeCategoryFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeCategoryFromChangeActivityParams::class)
    fun changeCategoryFromChangeActivity(): NavigationData {
        return NavigationData(
            R.id.action_changeRecordTypeFragment_to_changeCategoryFragment,
            bundleCreatorDelegate(ChangeCategoryFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeRecordTagFromTagsParams::class)
    fun changeRecordTagFromTags(): NavigationData {
        return NavigationData(
            R.id.action_categoriesFragment_to_changeRecordTagFragment,
            bundleCreatorDelegate(ChangeRecordTagFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeRecordTagFromChangeRecordParams::class)
    fun changeRecordTagFromChangeRecord(): NavigationData {
        return NavigationData(
            R.id.action_changeRecordFragment_to_changeRecordTagFragment,
            bundleCreatorDelegate(ChangeRecordTagFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeRecordTagFromChangeRunningRecordParams::class)
    fun changeRecordTagFromChangeRunningRecord(): NavigationData {
        return NavigationData(
            R.id.action_changeRunningRecordFragment_to_changeRecordTagFragment,
            bundleCreatorDelegate(ChangeRecordTagFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeActivityFilterParams.Change::class)
    fun changeActivityFilterChange(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeActivityFilterFragment,
            bundleCreatorDelegate(ChangeActivityFilterFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ScreenKey(ChangeActivityFilterParams.New::class)
    fun changeActivityFilterNew(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeActivityFilterFragment,
            bundleCreatorDelegate(ChangeActivityFilterFragment::createBundle)
        )
    }
}