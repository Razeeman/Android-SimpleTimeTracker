package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.mapper.OptionsListItemMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class StatisticsContainerOptionsListMapperImpl @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val optionsListItemMapper: OptionsListItemMapper,
    private val rangeViewDataMapper: RangeViewDataMapper,
) : StatisticsContainerOptionsListMapper {

    override suspend fun map(
        filterHidden: Boolean,
        rangeLength: RangeLength,
    ): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()

        val filterType = prefsInteractor.getChartFilterType()
        val hiddenContainerOptions = prefsInteractor.getHiddenContainerOptions()
            .filterIsInstance<ContainerOptionsModel.Statistics>()
            .map(::mapItemFromModel)
        val selectDateName = rangeViewDataMapper.mapToSelectDateName(rangeLength)?.text

        result += listOfNotNull(
            StatisticsContainerOptionsListItem.Share,
            StatisticsContainerOptionsListItem.Filter,
            StatisticsContainerOptionsListItem.SelectDate
                .takeIf { selectDateName != null },
            StatisticsContainerOptionsListItem.SelectRange,
            // Back to today will not work on overall range because of only one item in the list.
            StatisticsContainerOptionsListItem.BackToToday
                .takeIf { rangeLength != RangeLength.All },
        ).filter {
            !filterHidden || it !in hiddenContainerOptions
        }.mapNotNull { id ->
            optionsListItemMapper.mapCommonItem(
                id = id,
                isIconCheckVisible = if (id is StatisticsContainerOptionsListItem.Filter) {
                    optionsListItemMapper.isIconCheckVisible(
                        filteredIds = prefsInteractor.getChartFilteredIds(filterType),
                        existingIds = optionsListItemMapper.getExistingIds(filterType),
                    )
                } else {
                    false
                },
            ).let {
                if (id is StatisticsContainerOptionsListItem.SelectDate && selectDateName != null) {
                    it?.copy(text = selectDateName)
                } else {
                    it
                }
            }
        }

        return result
    }

    override fun mapItemFromModel(model: ContainerOptionsModel.Statistics): OptionsListParams.Item.Id {
        return when (model) {
            is ContainerOptionsModel.Statistics.Filter -> StatisticsContainerOptionsListItem.Filter
            is ContainerOptionsModel.Statistics.Share -> StatisticsContainerOptionsListItem.Share
            is ContainerOptionsModel.Statistics.BackToToday -> StatisticsContainerOptionsListItem.BackToToday
            is ContainerOptionsModel.Statistics.SelectDate -> StatisticsContainerOptionsListItem.SelectDate
            is ContainerOptionsModel.Statistics.SelectRange -> StatisticsContainerOptionsListItem.SelectRange
        }
    }

    override fun mapItemToModel(id: StatisticsContainerOptionsListItem): ContainerOptionsModel? {
        return when (id) {
            is StatisticsContainerOptionsListItem.Filter -> ContainerOptionsModel.Statistics.Filter
            is StatisticsContainerOptionsListItem.Share -> ContainerOptionsModel.Statistics.Share
            is StatisticsContainerOptionsListItem.BackToToday -> ContainerOptionsModel.Statistics.BackToToday
            is StatisticsContainerOptionsListItem.SelectDate -> ContainerOptionsModel.Statistics.SelectDate
            is StatisticsContainerOptionsListItem.SelectRange -> ContainerOptionsModel.Statistics.SelectRange
        }
    }
}