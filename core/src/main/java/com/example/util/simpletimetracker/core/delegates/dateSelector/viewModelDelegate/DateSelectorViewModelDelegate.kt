package com.example.util.simpletimetracker.core.delegates.dateSelector.viewModelDelegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.delegates.dateSelector.mapper.DateSelectorMapper
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewData.DateSelectorButtonsViewData
import com.example.util.simpletimetracker.core.delegates.dateSelector.viewData.DateSelectorScrollViewData
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.R
import com.example.util.simpletimetracker.navigation.params.screen.OptionsListParams
import javax.inject.Inject

class DateSelectorViewModelDelegate @Inject constructor(
    val dataProvider: DateSelectorMapper,
    private val prefsInteractor: PrefsInteractor,
) : ViewModelDelegate() {

    val dateScrollPosition: LiveData<DateSelectorScrollViewData> =
        SingleLiveEvent<DateSelectorScrollViewData>()
    val buttonsViewData: LiveData<DateSelectorButtonsViewData> = MutableLiveData()
    val updateDatesViewData: LiveData<Unit> = SingleLiveEvent<Unit>()
    val borderShadowsVisibility: LiveData<Boolean> = MutableLiveData()

    private var parent: Parent? = null
    private var animateScroll: Boolean = true

    fun attach(parent: Parent) {
        this.parent = parent
    }

    suspend fun initialize(position: Int) {
        setup()
        updatePosition(position)
    }

    suspend fun setup() {
        setupDatesSelector()
    }

    fun onDateClick(item: InfiniteRecyclerAdapter.Data) {
        if (parent?.currentPosition == item.position) {
            throttle { parent?.onDateClick() }.invoke()
        } else {
            animateScroll = false
            parent?.updatePosition(item.position)
        }
    }

    fun onDateLongClick(item: InfiniteRecyclerAdapter.Data) {
        if (parent?.currentPosition == item.position) {
            parent?.updatePosition(0)
        } else {
            onDateClick(item)
        }
    }

    fun onScrolledToDate(position: Int) {
        if (position != parent?.currentPosition) {
            parent?.updatePosition(position)
        }
    }

    fun updatePosition(shift: Int) {
        dataProvider.currentSelectedPosition = shift
        updateDatesViewData.set(Unit)
        val scrollData = DateSelectorScrollViewData(position = shift, animate = animateScroll)
        animateScroll = true
        dateScrollPosition.set(scrollData)
    }

    fun getOptionsButton(
        options: List<OptionsListParams.Item>,
    ): DateSelectorMapper.SetupData.Button {
        return if (options.isEmpty()) {
            DateSelectorMapper.SetupData.Button.Hidden
        } else {
            val defaultIcon = R.drawable.more
            val icon = options.firstOrNull()?.icon?.takeIf { options.size == 1 } ?: defaultIcon
            DateSelectorMapper.SetupData.Button.Visible(icon)
        }
    }

    private suspend fun setupDatesSelector() {
        val setupData = DateSelectorMapper.SetupData(
            type = parent?.getSetupData() ?: return,
            startOfDayShift = prefsInteractor.getStartOfDayShift(),
            firstDayOfWeek = prefsInteractor.getFirstDayOfWeek(),
        )
        dataProvider.setup(setupData)

        // Updates after setup.
        val shadowsVisibility = dataProvider.getCount() ==
            InfiniteRecyclerAdapter.DataProvider.Count.Infinite
        borderShadowsVisibility.set(shadowsVisibility)

        val buttonsData = DateSelectorButtonsViewData(
            addButton = when (setupData.type) {
                is DateSelectorMapper.SetupData.Type.Records -> {
                    DateSelectorButtonsViewData.Button.Visible(iconResId = R.drawable.plus)
                }
                is DateSelectorMapper.SetupData.Type.Statistics -> {
                    DateSelectorButtonsViewData.Button.Hidden
                }
            },
            optionsButton = when (val optionsButton = setupData.type.optionsButton) {
                is DateSelectorMapper.SetupData.Button.Visible -> {
                    DateSelectorButtonsViewData.Button.Visible(iconResId = optionsButton.iconResId)
                }
                is DateSelectorMapper.SetupData.Button.Hidden -> {
                    DateSelectorButtonsViewData.Button.Hidden
                }
            },
        )
        buttonsViewData.set(buttonsData)
    }

    interface Parent {
        val currentPosition: Int

        fun onDateClick()
        fun updatePosition(newPosition: Int)
        suspend fun getSetupData(): DateSelectorMapper.SetupData.Type
    }
}