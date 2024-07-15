package com.example.util.simpletimetracker.feature_statistics_detail.viewModel.delegate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.ViewModelDelegate
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_statistics_detail.interactor.StatisticsDetailAdjacentActivitiesInteractor
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsDetailNextActivitiesViewModelDelegate @Inject constructor(
    private val adjacentActivitiesInteractor: StatisticsDetailAdjacentActivitiesInteractor,
) : StatisticsDetailViewModelDelegate, ViewModelDelegate() {

    val viewData: LiveData<List<ViewHolderType>> by lazy {
        return@lazy MutableLiveData()
    }

    private var parent: StatisticsDetailViewModelDelegate.Parent? = null

    override fun attach(parent: StatisticsDetailViewModelDelegate.Parent) {
        this.parent = parent
    }

    fun updateViewData() = delegateScope.launch {
        viewData.set(loadViewData())
        parent?.updateContent()
    }

    private suspend fun loadViewData(): List<ViewHolderType> {
        val parent = parent ?: return emptyList()

        return adjacentActivitiesInteractor.getNextActivitiesViewData(
            filter = parent.filter,
            rangeLength = parent.rangeLength,
            rangePosition = parent.rangePosition,
        )
    }
}