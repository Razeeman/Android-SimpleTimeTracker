package com.example.util.simpletimetracker.feature_settings.customizeOptionsMenu

import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel.DetailedStatistics
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel.Records
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel.Statistics
import com.example.util.simpletimetracker.feature_records.api.RecordsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_records.api.RecordsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListItem
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListMapper
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class CustomizeOptionsMenuMapper @Inject constructor(
    private val recordsContainerOptionsListMapper: RecordsContainerOptionsListMapper,
    private val statisticsContainerOptionsListMapper: StatisticsContainerOptionsListMapper,
    private val statisticsDetailOptionsListMapper: StatisticsDetailOptionsListMapper,
) {

    fun mapBlockFromModel(model: ContainerOptionsModel): SettingsBlock {
        return when (model) {
            is Records.CalendarView -> SettingsBlock.CustomizeOptionsMenuRecordsCalendar
            is Records.Filter -> SettingsBlock.CustomizeOptionsMenuRecordsFilter
            is Records.Share -> SettingsBlock.CustomizeOptionsMenuRecordsShare
            is Records.BackToToday -> SettingsBlock.CustomizeOptionsMenuRecordsBackToToday
            is Records.SelectDate -> SettingsBlock.CustomizeOptionsMenuRecordsSelectDate
            is Statistics.Filter -> SettingsBlock.CustomizeOptionsMenuStatisticsFilter
            is Statistics.Share -> SettingsBlock.CustomizeOptionsMenuStatisticsShare
            is Statistics.BackToToday -> SettingsBlock.CustomizeOptionsMenuStatisticsBackToToday
            is Statistics.SelectDate -> SettingsBlock.CustomizeOptionsMenuStatisticsSelectDate
            is Statistics.SelectRange -> SettingsBlock.CustomizeOptionsMenuStatisticsSelectRange
            is DetailedStatistics.Compare -> SettingsBlock.CustomizeOptionsMenuDetailedStatisticsCompare
            is DetailedStatistics.Filter -> SettingsBlock.CustomizeOptionsMenuDetailedStatisticsFilter
            is DetailedStatistics.SelectDate -> SettingsBlock.CustomizeOptionsMenuDetailedStatisticsSelectDate
            is DetailedStatistics.SelectRange -> SettingsBlock.CustomizeOptionsMenuDetailedStatisticsSelectRange
            is DetailedStatistics.BackToToday -> SettingsBlock.CustomizeOptionsMenuDetailedStatisticsBackToToday
        }
    }

    fun mapBlockToModel(block: SettingsBlock): ContainerOptionsModel? {
        return when (block) {
            SettingsBlock.CustomizeOptionsMenuRecordsCalendar -> Records.CalendarView
            SettingsBlock.CustomizeOptionsMenuRecordsFilter -> Records.Filter
            SettingsBlock.CustomizeOptionsMenuRecordsShare -> Records.Share
            SettingsBlock.CustomizeOptionsMenuRecordsBackToToday -> Records.BackToToday
            SettingsBlock.CustomizeOptionsMenuRecordsSelectDate -> Records.SelectDate
            SettingsBlock.CustomizeOptionsMenuStatisticsFilter -> Statistics.Filter
            SettingsBlock.CustomizeOptionsMenuStatisticsShare -> Statistics.Share
            SettingsBlock.CustomizeOptionsMenuStatisticsBackToToday -> Statistics.BackToToday
            SettingsBlock.CustomizeOptionsMenuStatisticsSelectDate -> Statistics.SelectDate
            SettingsBlock.CustomizeOptionsMenuStatisticsSelectRange -> Statistics.SelectRange
            SettingsBlock.CustomizeOptionsMenuDetailedStatisticsCompare -> DetailedStatistics.Compare
            SettingsBlock.CustomizeOptionsMenuDetailedStatisticsFilter -> DetailedStatistics.Filter
            SettingsBlock.CustomizeOptionsMenuDetailedStatisticsSelectDate -> DetailedStatistics.SelectDate
            SettingsBlock.CustomizeOptionsMenuDetailedStatisticsSelectRange -> DetailedStatistics.SelectRange
            SettingsBlock.CustomizeOptionsMenuDetailedStatisticsBackToToday -> DetailedStatistics.BackToToday
            else -> null
        }
    }

    fun mapItemFromModel(model: ContainerOptionsModel): OptionsListParams.Item.Id {
        return when (model) {
            is Records -> recordsContainerOptionsListMapper.mapItemFromModel(model)
            is Statistics -> statisticsContainerOptionsListMapper.mapItemFromModel(model)
            is DetailedStatistics -> statisticsDetailOptionsListMapper.mapItemFromModel(model)
        }
    }

    fun mapItemToModel(id: OptionsListParams.Item.Id): ContainerOptionsModel? {
        return when (id) {
            is RecordsContainerOptionsListItem -> recordsContainerOptionsListMapper.mapItemToModel(id)
            is StatisticsContainerOptionsListItem -> statisticsContainerOptionsListMapper.mapItemToModel(id)
            is StatisticsDetailOptionsListItem -> statisticsDetailOptionsListMapper.mapItemToModel(id)
            else -> null
        }
    }
}