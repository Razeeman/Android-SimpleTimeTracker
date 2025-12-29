package com.example.util.simpletimetracker.core.delegates.dateSelector.mapper

import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.RangeTitleMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.mapper.DaysInCalendarMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.statistics.extension.canBeSwiped
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorDayViewData
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorRangeViewData
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorSingleViewData
import javax.inject.Inject

class DateSelectorMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val rangeTitleMapper: RangeTitleMapper,
    private val daysInCalendarMapper: DaysInCalendarMapper,
    private val calendarToListShiftMapper: CalendarToListShiftMapper,
) : InfiniteRecyclerAdapter.DataProvider {

    var currentSelectedPosition: Int = 0

    private var isInitialized: Boolean = false
    private var setupData: SetupData = SetupData.Empty
    private var currentItem: InfiniteRecyclerAdapter.Data = getItem(0)

    override fun isInitialized(): Boolean {
        return isInitialized
    }

    override fun getCount(): InfiniteRecyclerAdapter.DataProvider.Count {
        val type = setupData.type
        val isOneItem = type is SetupData.Type.Statistics &&
            !type.rangeLength.canBeSwiped()

        return if (isOneItem) {
            InfiniteRecyclerAdapter.DataProvider.Count.One
        } else {
            InfiniteRecyclerAdapter.DataProvider.Count.Infinite
        }
    }

    override fun getCurrentItem(): InfiniteRecyclerAdapter.Data {
        return currentItem
    }

    override fun getItem(
        position: Int,
    ): InfiniteRecyclerAdapter.Data {
        return when (val type = setupData.type) {
            is SetupData.Type.Records -> {
                val isCalendar = isCalendar(
                    isCalendarView = type.isCalendarView,
                    daysInCalendar = type.daysInCalendar,
                )
                if (isCalendar) {
                    getCalendarViewDataData(
                        position = position,
                        isSelected = isSelected(position),
                        type = type,
                    )
                } else {
                    mapToViewData(
                        data = rangeTitleMapper.mapToDateSelectorData(
                            rangeLength = RangeLength.Day,
                            position = position,
                            isSelected = isSelected(position),
                            startOfDayShift = setupData.startOfDayShift,
                            firstDayOfWeek = setupData.firstDayOfWeek,
                        ),
                        position = position,
                    )
                }
            }
            is SetupData.Type.Statistics -> {
                mapToViewData(
                    data = rangeTitleMapper.mapToDateSelectorData(
                        rangeLength = type.rangeLength,
                        position = position,
                        isSelected = isSelected(position),
                        startOfDayShift = setupData.startOfDayShift,
                        firstDayOfWeek = setupData.firstDayOfWeek,
                    ),
                    position = position,
                )
            }
        }
    }

    fun setup(
        setupData: SetupData,
    ) {
        this.setupData = setupData
        currentItem = getItem(0)

        this.isInitialized = true
    }

    private fun getCalendarViewDataData(
        position: Int,
        isSelected: Boolean,
        type: SetupData.Type.Records,
    ): DateSelectorRangeViewData {
        val calendarRange = calendarToListShiftMapper.mapCalendarToListShift(
            calendarShift = position,
            daysInCalendar = type.daysInCalendar,
            startOfDayShift = setupData.startOfDayShift,
            firstDayOfWeek = setupData.firstDayOfWeek,
        )

        fun getDayViewData(position: Int): RangeTitleMapper.DateSelectorData.Data {
            val timestamp = timeMapper.toDayDateTimestamp(
                daysFromToday = position,
                startOfDayShift = setupData.startOfDayShift,
            )
            return rangeTitleMapper.mapToDateSelectorDayOfMonthData(
                isSelected = isSelected,
                timestamp = timestamp,
            )
        }

        return DateSelectorRangeViewData(
            position = position,
            dayMonth1 = getDayViewData(calendarRange.start).toViewData(),
            dayMonth2 = getDayViewData(calendarRange.end).toViewData(),
            cardData = getCardViewData(position),
        )
    }

    private fun getCardViewData(
        position: Int,
    ): DateSelectorDayViewData.CardData {
        val isToday = position == 0
        val isFuture = position > 0
        val isSelected = isSelected(position)

        return DateSelectorDayViewData.CardData(
            isToday = isToday,
            isSelected = isSelected,
            isFuture = isFuture,
            increasedTextSize = isSelected,
        )
    }

    private fun isSelected(
        position: Int,
    ): Boolean {
        return position == currentSelectedPosition
    }

    private fun mapToViewData(
        data: RangeTitleMapper.DateSelectorData,
        position: Int,
    ): InfiniteRecyclerAdapter.Data {
        val cardData = getCardViewData(position)

        return when (data) {
            is RangeTitleMapper.DateSelectorData.Single -> {
                DateSelectorDayViewData(
                    position = position,
                    dayMonth = data.data.toViewData(),
                    cardData = cardData,
                )
            }
            is RangeTitleMapper.DateSelectorData.Double -> {
                DateSelectorRangeViewData(
                    position = position,
                    dayMonth1 = data.data1.toViewData(),
                    dayMonth2 = data.data2.toViewData(),
                    cardData = cardData,
                )
            }
            is RangeTitleMapper.DateSelectorData.Wide -> {
                DateSelectorSingleViewData(
                    position = position,
                    dayMonth = data.data.toViewData(),
                    cardData = cardData.copy(isToday = false),
                )
            }
        }
    }

    // TODO DATE backToToday not working while list is flung
    private fun RangeTitleMapper.DateSelectorData.Data.toViewData(): DateSelectorDayViewData.DayMonth {
        return DateSelectorDayViewData.DayMonth(
            additionalHint = this.additionalHint,
            topText = this.topText,
            bottomText = this.bottomText,
        )
    }

    private fun isCalendar(
        isCalendarView: Boolean,
        daysInCalendar: DaysInCalendar,
    ): Boolean {
        val calendarDayCount = daysInCalendarMapper.mapDaysCount(daysInCalendar)
        return isCalendarView && calendarDayCount > 1
    }

    data class SetupData(
        val type: Type,
        val startOfDayShift: Long,
        val firstDayOfWeek: DayOfWeek,
    ) {
        sealed interface Type {
            data class Records(
                val isCalendarView: Boolean,
                val daysInCalendar: DaysInCalendar,
            ) : Type

            data class Statistics(
                val rangeLength: RangeLength,
            ) : Type
        }

        companion object {
            val Empty = SetupData(
                type = Type.Statistics(RangeLength.Day),
                startOfDayShift = 0,
                firstDayOfWeek = DayOfWeek.SUNDAY,
            )
        }
    }
}