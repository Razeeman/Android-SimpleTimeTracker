package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.mapper.OptionsListItemMapper
import com.example.util.simpletimetracker.core.mapper.RangeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.CommonOptionsListItem
import com.example.util.simpletimetracker.domain.base.ContainerOptionsModel
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListItem
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class StatisticsDetailOptionsListMapperImpl @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val resourceRepo: ResourceRepo,
    private val rangeViewDataMapper: RangeViewDataMapper,
    private val optionsListItemMapper: OptionsListItemMapper,
) : StatisticsDetailOptionsListMapper {

    override suspend fun map(
        filterHidden: Boolean,
        rangeLength: RangeLength,
    ): List<OptionsListParams.Item> {
        val result = mutableListOf<OptionsListParams.Item>()

        val hiddenContainerOptions = prefsInteractor.getHiddenContainerOptions()
            .filterIsInstance<ContainerOptionsModel.DetailedStatistics>()
            .map(::mapItemFromModel)
        val selectDateName = rangeViewDataMapper.mapToSelectDateName(rangeLength)?.text

        result += listOfNotNull(
            StatisticsDetailOptionsListItem.Compare,
            StatisticsDetailOptionsListItem.Filter,
            StatisticsDetailOptionsListItem.SelectDate
                .takeIf { selectDateName != null },
            StatisticsDetailOptionsListItem.SelectRange,
            // Back to today will not work on overall range because of only one item in the list.
            StatisticsDetailOptionsListItem.BackToToday
                .takeIf { rangeLength != RangeLength.All },
        ).filter {
            !filterHidden || it !in hiddenContainerOptions
        }.mapNotNull { id ->
            when (id) {
                is StatisticsDetailOptionsListItem.Filter -> mapCommonItem(id)
                is StatisticsDetailOptionsListItem.SelectDate -> mapCommonItem(id)
                is StatisticsDetailOptionsListItem.SelectRange -> mapCommonItem(id)
                is StatisticsDetailOptionsListItem.BackToToday -> mapCommonItem(id)
                is StatisticsDetailOptionsListItem.Compare,
                -> OptionsListParams.Item(
                    id = StatisticsDetailOptionsListItem.Compare,
                    text = resourceRepo.getString(R.string.types_compare_hint),
                    icon = R.drawable.compare,
                )
            }.let {
                if (id is StatisticsDetailOptionsListItem.SelectDate && selectDateName != null) {
                    it?.copy(text = selectDateName)
                } else {
                    it
                }
            }
        }

        return result
    }

    override fun mapItemFromModel(model: ContainerOptionsModel.DetailedStatistics): OptionsListParams.Item.Id {
        return when (model) {
            is ContainerOptionsModel.DetailedStatistics.Compare -> StatisticsDetailOptionsListItem.Compare
            is ContainerOptionsModel.DetailedStatistics.Filter -> StatisticsDetailOptionsListItem.Filter
            is ContainerOptionsModel.DetailedStatistics.SelectDate -> StatisticsDetailOptionsListItem.SelectDate
            is ContainerOptionsModel.DetailedStatistics.SelectRange -> StatisticsDetailOptionsListItem.SelectRange
            is ContainerOptionsModel.DetailedStatistics.BackToToday -> StatisticsDetailOptionsListItem.BackToToday
        }
    }

    override fun mapItemToModel(id: StatisticsDetailOptionsListItem): ContainerOptionsModel? {
        return when (id) {
            is StatisticsDetailOptionsListItem.Compare -> ContainerOptionsModel.DetailedStatistics.Compare
            is StatisticsDetailOptionsListItem.Filter -> ContainerOptionsModel.DetailedStatistics.Filter
            is StatisticsDetailOptionsListItem.SelectDate -> ContainerOptionsModel.DetailedStatistics.SelectDate
            is StatisticsDetailOptionsListItem.SelectRange -> ContainerOptionsModel.DetailedStatistics.SelectRange
            is StatisticsDetailOptionsListItem.BackToToday -> ContainerOptionsModel.DetailedStatistics.BackToToday
        }
    }

    private fun <T> mapCommonItem(
        id: T,
    ): OptionsListParams.Item?
        where T : CommonOptionsListItem, T : OptionsListParams.Item.Id {
        return optionsListItemMapper.mapCommonItem(id)
    }
}