package com.example.util.simpletimetracker.core.sharedViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.model.NavigationTab
import javax.inject.Inject

class MainTabsViewModel @Inject constructor() : ViewModel() {

    val tabReselected: LiveData<NavigationTab?> = MutableLiveData()

    fun onTabReselected(tab: NavigationTab) {
        tabReselected.set(tab)
    }

    fun onHandled() {
        tabReselected.set(null)
    }
}