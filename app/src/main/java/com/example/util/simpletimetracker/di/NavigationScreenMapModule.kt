package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.R
import com.example.util.simpletimetracker.feature_change_category.view.ChangeCategoryFragment
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import com.example.util.simpletimetracker.feature_change_record_tag.view.ChangeRecordTagFragment
import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import com.example.util.simpletimetracker.feature_change_running_record.view.ChangeRunningRecordFragment
import com.example.util.simpletimetracker.feature_records_all.view.RecordsAllFragment
import com.example.util.simpletimetracker.feature_statistics_detail.view.StatisticsDetailFragment
import com.example.util.simpletimetracker.navigation.bundleCreator.BundleCreator
import com.example.util.simpletimetracker.navigation.bundleCreator.bundleCreatorDelegate
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeCategoryParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromRecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTagParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordTypeParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRunningRecordParams
import com.example.util.simpletimetracker.navigation.NavigationData
import com.example.util.simpletimetracker.navigation.params.screen.RecordsAllParams
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
class NavigationScreenMapModule {

    @IntoMap
    @Provides
    @ClassKey(ChangeRecordTypeParams.Change::class)
    fun changeRecordTypeChange(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordTypeFragment,
            bundleCreatorDelegate(ChangeRecordTypeFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ChangeRecordTypeParams.New::class)
    fun changeRecordTypeNew(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordTypeFragment,
            bundleCreatorDelegate(ChangeRecordTypeFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ChangeRunningRecordParams::class)
    fun changeRecordRunning(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRunningRecordFragment,
            bundleCreatorDelegate(ChangeRunningRecordFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ChangeRecordFromMainParams::class)
    fun changeRecordFromMain(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_changeRecordFragment,
            bundleCreatorDelegate(ChangeRecordFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ChangeRecordFromRecordsAllParams::class)
    fun changeRecordFromRecordsAll(): NavigationData {
        return NavigationData(
            R.id.action_recordsAllFragment_to_changeRecordFragment,
            bundleCreatorDelegate(ChangeRecordFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(StatisticsDetailParams::class)
    fun statisticsDetail(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_statisticsDetailFragment,
            bundleCreatorDelegate(StatisticsDetailFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(RecordsAllParams::class)
    fun recordsAll(): NavigationData {
        return NavigationData(
            R.id.action_statisticsDetailFragment_to_recordsAllFragment,
            bundleCreatorDelegate(RecordsAllFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(CategoriesParams::class)
    fun categories(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_categoriesFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ArchiveParams::class)
    fun archive(): NavigationData {
        return NavigationData(
            R.id.action_mainFragment_to_archiveFragment,
            BundleCreator.empty()
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ChangeCategoryParams::class)
    fun changeCategory(): NavigationData {
        return NavigationData(
            R.id.action_categoriesFragment_to_changeCategoryFragment,
            bundleCreatorDelegate(ChangeCategoryFragment::createBundle)
        )
    }

    @IntoMap
    @Provides
    @ClassKey(ChangeRecordTagParams::class)
    fun changeRecordTag(): NavigationData {
        return NavigationData(
            R.id.action_categoriesFragment_to_changeRecordTagFragment,
            bundleCreatorDelegate(ChangeRecordTagFragment::createBundle)
        )
    }
}