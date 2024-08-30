package com.example.util.simpletimetracker.core.view

import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp

object ViewChooserStateDelegate {

    inline fun <reified T> updateChooser(
        state: States,
        chooserData: View,
        chooserView: CardView,
        chooserArrow: View,
    ) {
        val opened = state.current is T
        val opening = state.previous is State.Closed && state.current is T
        val closing = state.previous is T && state.current is State.Closed

        chooserData.isVisible = opened
        chooserView.setChooserColor(opened)
        chooserArrow.apply {
            if (opening) rotateDown()
            if (closing) rotateUp()
        }
    }

    data class States(
        val current: State,
        val previous: State,
    )

    interface State {
        interface Closed : State
    }
}