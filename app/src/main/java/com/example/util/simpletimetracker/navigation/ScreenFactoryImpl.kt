package com.example.util.simpletimetracker.navigation

import androidx.fragment.app.Fragment
import com.example.util.simpletimetracker.feature_tag_selection.view.RecordTagSelectionFragment
import javax.inject.Inject

class ScreenFactoryImpl @Inject constructor() : ScreenFactory {

    override fun getFragment(
        screen: Screen,
        data: Any?
    ): Fragment? = when (screen) {
        Screen.RECORD_TAG_SELECTION -> RecordTagSelectionFragment.newInstance(data)
        else -> null
    }
}