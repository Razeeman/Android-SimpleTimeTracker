package com.example.util.simpletimetracker.navigation

import androidx.fragment.app.Fragment
import com.example.util.simpletimetracker.feature_tag_selection.view.RecordTagSelectionFragment
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams
import javax.inject.Inject

class ScreenFactoryImpl @Inject constructor() : ScreenFactory {

    override fun getFragment(
        data: ScreenParams,
    ): Fragment? = when (data) {
        is RecordTagSelectionParams -> RecordTagSelectionFragment.newInstance(data)
        else -> null
    }
}