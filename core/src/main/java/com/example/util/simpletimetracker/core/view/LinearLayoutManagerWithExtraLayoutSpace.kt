package com.example.util.simpletimetracker.core.view

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.feature_views.extension.dpToPx

class LinearLayoutManagerWithExtraLayoutSpace(context: Context) : LinearLayoutManager(context) {

    override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
        val extraLayoutSpaceTotal = getExtraLayoutSpace(state)
        extraLayoutSpace[0] = extraLayoutSpaceTotal
        extraLayoutSpace[1] = extraLayoutSpaceTotal
    }

    // Need more laid out space outside of the screen,
    // otherwise on move animation items would just appear out of nowhere.
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
        return if (state.hasTargetScrollPosition()) {
            super.getExtraLayoutSpace(state)
        } else {
            24.dpToPx()
        }
    }
}