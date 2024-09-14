package com.example.util.simpletimetracker.core.sharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.util.simpletimetracker.core.base.BaseViewModel
import com.example.util.simpletimetracker.core.extension.lazySuspend
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import javax.inject.Inject

class MainTabsViewModel @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
) : BaseViewModel() {

    val tabReselected: LiveData<NavigationTab?> = MutableLiveData()
    val isScrolling: LiveData<Boolean> = MutableLiveData(false)
    val isNavBatAtTheBottom: LiveData<Boolean> by lazySuspend { loadIsNavBatAtTheBottom() }

    fun onTabReselected(tab: NavigationTab) {
        tabReselected.set(tab)
    }

    fun onHandled() {
        tabReselected.set(null)
    }

    fun onScrollStateChanged(isScrolling: Boolean) {
        this.isScrolling.set(isScrolling)
    }

    private suspend fun loadIsNavBatAtTheBottom(): Boolean {
        return prefsInteractor.getIsNavBarAtTheBottom()
    }
}