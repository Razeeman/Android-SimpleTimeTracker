package com.example.util.simpletimetracker.feature_records.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.delegates.dateSelector.mapper.DateSelectorMapper
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate.DateSelectorViewModelDelegate
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.extension.shiftTimeStamp
import com.example.util.simpletimetracker.core.mapper.CalendarToListShiftMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DaysInCalendar
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerMultiselectInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsContainerUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsShareUpdateInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordsUpdateInteractor
import com.example.util.simpletimetracker.domain.statistics.model.RangeLength
import com.example.util.simpletimetracker.feature_records.R
import com.example.util.simpletimetracker.feature_records.api.RecordsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_records.api.RecordsContainerOptionsListItem
import com.example.util.simpletimetracker.feature_records.model.RecordsContainerPosition
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordFromMainParams
import com.example.util.simpletimetracker.navigation.params.screen.ChangeRecordParams
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordsContainerViewModel @Inject constructor(
    val dateSelectorViewModelDelegate: DateSelectorViewModelDelegate,
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordsUpdateInteractor: RecordsUpdateInteractor,
    private val recordsContainerUpdateInteractor: RecordsContainerUpdateInteractor,
    private val recordsShareUpdateInteractor: RecordsShareUpdateInteractor,
    private val calendarToListShiftMapper: CalendarToListShiftMapper,
    private val recordsContainerOptionsListMapper: RecordsContainerOptionsListMapper,
    private val recordsContainerMultiselectInteractor: RecordsContainerMultiselectInteractor,
) : BaseViewModel() {

    val position: LiveData<RecordsContainerPosition>
        by lazySuspend { loadPosition(newPosition = 0, animate = false) }

    private var lastListShift: Int = 0
    private val currentPosition: Int get() = position.value?.position.orZero()

    init {
        dateSelectorViewModelDelegate.attach(getDateSelectorDelegateParent())
        subscribeToUpdates()
    }

    fun initialize() {
        viewModelScope.launch {
            dateSelectorViewModelDelegate.initialize(currentPosition)
        }
    }

    fun onOptionsClick() = viewModelScope.launch {
        val items = recordsContainerOptionsListMapper.map(filterHidden = true)
        when {
            items.isEmpty() -> return@launch
            items.size == 1 -> items.firstOrNull()?.id?.let(::onOptionsItemClick)
            else -> router.navigate(OptionsListParams(items))
        }
    }

    fun onOptionsLongClick() {
        onFilterClick()
    }

    fun onRecordAddClick() = viewModelScope.launch {
        val params = ChangeRecordParams.New(getActualShift())
        router.navigate(ChangeRecordFromMainParams(params))
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        val startOfDayShift = prefsInteractor.getStartOfDayShift()
        when (tag) {
            DATE_TAG -> {
                timeMapper
                    .toTimestampShift(
                        toTime = timestamp.shiftTimeStamp(startOfDayShift),
                        range = RangeLength.Day,
                        firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                    )
                    .toInt()
                    .let { shift ->
                        if (prefsInteractor.getShowRecordsCalendar()) {
                            calendarToListShiftMapper.mapListToCalendarShift(
                                listShift = shift,
                                daysInCalendar = prefsInteractor.getDaysInCalendar(),
                                startOfDayShift = prefsInteractor.getStartOfDayShift(),
                                firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
                            )
                        } else {
                            shift
                        }
                    }
                    .let { updatePosition(it, animate = true) }
            }
        }
    }

    fun onOptionsItemClick(id: OptionsListParams.Item.Id) {
        if (id !is RecordsContainerOptionsListItem) return
        when (id) {
            is RecordsContainerOptionsListItem.CalendarView -> onCalendarSwitchClick()
            is RecordsContainerOptionsListItem.Filter -> onFilterClick()
            is RecordsContainerOptionsListItem.Share -> onShareClick()
            is RecordsContainerOptionsListItem.BackToToday -> onBackToTodayClick()
            is RecordsContainerOptionsListItem.SelectDate -> onSelectDateClick()
        }
    }

    private fun onSelectDateClick() {
        viewModelScope.launch {
            val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
            val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
            val startOfDayShift = prefsInteractor.getStartOfDayShift()
            val current = timeMapper.toTimestampShifted(
                rangesFromToday = getActualShift(),
                range = RangeLength.Day,
                startOfDayShift = startOfDayShift,
            )

            router.navigate(
                DateTimeDialogParams(
                    tag = DATE_TAG,
                    type = DateTimeDialogType.DATE,
                    timestamp = current,
                    useMilitaryTime = useMilitaryTime,
                    firstDayOfWeek = firstDayOfWeek,
                ),
            )
        }
    }

    private fun onBackToTodayClick() {
        updatePosition(0, animate = true)
    }

    private fun onFilterClick() = viewModelScope.launch {
        val params = ChartFilterDialogParams(
            chartFilterType = prefsInteractor.getListFilterType(),
            filteredTypeIds = prefsInteractor.getFilteredTypesOnList(),
            filteredCategoryIds = prefsInteractor.getFilteredCategoriesOnList(),
            filteredTagIds = prefsInteractor.getFilteredTagsOnList(),
        )
        router.navigate(params)
    }

    private fun onShareClick() = viewModelScope.launch {
        recordsShareUpdateInteractor.sendShareClicked()
    }

    private fun onCalendarSwitchClick() = viewModelScope.launch {
        val newValue = !prefsInteractor.getShowRecordsCalendar()
        prefsInteractor.setShowRecordsCalendar(newValue)
        recalculateRangeOnCalendarViewSwitched()
        // Update record fragment that on the same page as was calendar,
        // other pages will be updated on becoming visible,
        // this one will not because it is already visible.
        recordsUpdateInteractor.send()
    }

    private fun subscribeToUpdates() {
        viewModelScope.launch {
            recordsContainerUpdateInteractor.showCalendarUpdated
                .collect { recalculateRangeOnCalendarViewSwitched() }
        }
        viewModelScope.launch {
            recordsContainerUpdateInteractor.calendarDaysUpdated
                .collect { recalculateRangeOnCalendarDaysChanged() }
        }
        viewModelScope.launch {
            recordsContainerUpdateInteractor.dateSelectorUpdate
                .collect {
                    viewModelScope.launch {
                        dateSelectorViewModelDelegate.setup()
                        dateSelectorViewModelDelegate.updatePosition(currentPosition)
                    }
                }
        }
        viewModelScope.launch {
            recordsContainerMultiselectInteractor.stateChanged
                .collect { onMultiselectEnabled() }
        }
    }

    private suspend fun recalculateRangeOnCalendarViewSwitched() {
        val wasCalendar = position.value?.isCalendar.orFalse()
        val nowIsCalendar = prefsInteractor.getShowRecordsCalendar()
        if (wasCalendar == nowIsCalendar) return

        val newPosition = calendarToListShiftMapper.recalculateRangeOnCalendarViewSwitched(
            currentPosition = currentPosition,
            lastListPosition = lastListShift,
            showCalendar = prefsInteractor.getShowRecordsCalendar(),
            daysInCalendar = prefsInteractor.getDaysInCalendar(),
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
        dateSelectorViewModelDelegate.setup()
        updatePosition(newPosition, animate = false)
    }

    private suspend fun recalculateRangeOnCalendarDaysChanged() {
        val isCalendar = position.value?.isCalendar.orFalse()
        if (!isCalendar) return
        val prevDaysInCalendar = position.value?.daysInCalendar ?: DaysInCalendar.ONE
        val newDaysInCalendar = prefsInteractor.getDaysInCalendar()
        if (prevDaysInCalendar == newDaysInCalendar) return

        val newPosition = calendarToListShiftMapper.recalculateRangeOnCalendarDaysChanged(
            currentPosition = currentPosition,
            currentDaysInCalendar = prevDaysInCalendar,
            newDaysInCalendar = newDaysInCalendar,
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
        dateSelectorViewModelDelegate.setup()
        updatePosition(newPosition, animate = false)
    }

    private suspend fun getActualShift(): Int {
        val shift = currentPosition
        // Zero shift contains today, so return today.
        if (shift == 0) return 0
        return if (prefsInteractor.getShowRecordsCalendar()) {
            calendarToListShiftMapper.mapCalendarToListShift(
                calendarShift = shift,
                daysInCalendar = prefsInteractor.getDaysInCalendar(),
                startOfDayShift = prefsInteractor.getStartOfDayShift(),
                firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
            ).end
        } else {
            shift
        }
    }

    private fun onMultiselectEnabled() {
        if (recordsContainerMultiselectInteractor.isEnabled) {
            val params = SnackBarParams(
                message = resourceRepo.getString(R.string.change_record_multiselect_hint),
                duration = SnackBarParams.Duration.Long,
                marginBottomDp = resourceRepo.getDimenInDp(R.dimen.button_height),
            )
            router.show(params)
        }
    }

    private fun getDateSelectorDelegateParent(): DateSelectorViewModelDelegate.Parent {
        return object : DateSelectorViewModelDelegate.Parent {
            override val currentPosition: Int
                get() = this@RecordsContainerViewModel.currentPosition

            override fun onDateClick() =
                this@RecordsContainerViewModel.onSelectDateClick()

            override fun updatePosition(newPosition: Int) =
                this@RecordsContainerViewModel.updatePosition(newPosition, true)

            override suspend fun getSetupData(): DateSelectorMapper.SetupData.Type {
                return DateSelectorMapper.SetupData.Type.Records(
                    optionsButton = dateSelectorViewModelDelegate.getOptionsButton(
                        options = recordsContainerOptionsListMapper.map(filterHidden = true),
                    ),
                    isCalendarView = prefsInteractor.getShowRecordsCalendar(),
                    daysInCalendar = prefsInteractor.getDaysInCalendar(),
                )
            }
        }
    }

    private fun updatePosition(
        shift: Int,
        animate: Boolean,
    ) {
        viewModelScope.launch {
            val data = loadPosition(shift, animate)
            dateSelectorViewModelDelegate.updatePosition(shift)
            position.set(data)
        }
    }

    private suspend fun loadPosition(
        newPosition: Int,
        animate: Boolean,
    ): RecordsContainerPosition {
        val isCalendar = prefsInteractor.getShowRecordsCalendar()
        if (!isCalendar) lastListShift = newPosition

        return RecordsContainerPosition(
            position = newPosition,
            isCalendar = isCalendar,
            daysInCalendar = prefsInteractor.getDaysInCalendar(),
            animate = animate,
        )
    }

    companion object {
        private const val DATE_TAG = "records_date_tag"
    }
}
