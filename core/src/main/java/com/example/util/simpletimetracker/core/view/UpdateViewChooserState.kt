package com.example.util.simpletimetracker.core.view

import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.core.utils.setChooserColor
import com.example.util.simpletimetracker.feature_views.extension.rotateDown
import com.example.util.simpletimetracker.feature_views.extension.rotateUp

object UpdateViewChooserState {

    inline fun <STATE, reified T : STATE, reified CLOSED : STATE> updateChooser(
        stateCurrent: STATE,
        statePrevious: STATE,
        chooserData: View,
        chooserView: CardView,
        chooserArrow: View,
    ) {
        val opened = stateCurrent is T
        val opening = statePrevious is CLOSED && stateCurrent is T
        val closing = statePrevious is T && stateCurrent is CLOSED

        chooserData.isVisible = opened
        chooserView.setChooserColor(opened)
        chooserArrow.apply {
            if (opening) rotateDown()
            if (closing) rotateUp()
        }
    }
}