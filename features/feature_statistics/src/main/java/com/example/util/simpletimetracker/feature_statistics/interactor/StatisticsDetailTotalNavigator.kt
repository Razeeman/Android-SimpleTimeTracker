package com.example.util.simpletimetracker.feature_statistics.interactor

import com.example.util.simpletimetracker.core.extension.toParams
import com.example.util.simpletimetracker.core.interactor.GetStatisticsDetailRangeInteractor
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RecordsFilter
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.StatisticsDetailParams
import javax.inject.Inject

class StatisticsDetailTotalNavigator @Inject constructor(
    private val router: Router,
    private val prefsInteractor: PrefsInteractor,
    private val getStatisticsDetailRangeInteractor: GetStatisticsDetailRangeInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    suspend fun execute(
        shift: Int,
    ) {
        val filter = when (prefsInteractor.getChartFilterType()) {
            ChartFilterType.ACTIVITY -> {
                val typeIds = recordTypeInteractor.getAll()
                    .map(RecordType::id)
                RecordsFilter.Activity(typeIds)
            }
            ChartFilterType.CATEGORY -> {
                val categoryIds = categoryInteractor.getAll()
                    .map(Category::id)
                val items = categoryIds
                    .map(RecordsFilter.CategoryItem::Categorized) +
                    RecordsFilter.CategoryItem.Uncategorized
                RecordsFilter.Category(items)
            }
            ChartFilterType.RECORD_TAG -> {
                val tagIds = recordTagInteractor.getAll()
                    .map(RecordTag::id)
                val items = tagIds
                    .map(RecordsFilter.TagItem::Tagged) +
                    RecordsFilter.TagItem.Untagged
                RecordsFilter.SelectedTags(items)
            }
        }

        val params = StatisticsDetailParams(
            transitionName = "",
            filter = filter.let(::listOf).map(RecordsFilter::toParams),
            range = getStatisticsDetailRangeInteractor.execute(),
            shift = shift,
            preview = null,
        )

        router.navigate(params)
    }
}