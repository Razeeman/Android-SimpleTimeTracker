package com.example.util.simpletimetracker.core.extension

import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.feature_views.extension.addOnScrollListenerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

val BottomSheetDialogFragment.behavior: BottomSheetBehavior<View>?
    get() = dialog?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        ?.let { bottomSheet -> BottomSheetBehavior.from(bottomSheet) }

fun BottomSheetDialogFragment.setSkipCollapsed() {
    behavior?.apply {
        peekHeight = 0
        skipCollapsed = true
        state = BottomSheetBehavior.STATE_EXPANDED
    }
}

fun BottomSheetDialogFragment.setFullScreen() {
    // Dialog parent is R.id.design_bottom_sheet from android material.
    // It's a wrapper created around dialog to set bottom sheet behavior. By default it's created
    // with wrap_content height, so we replace it here.
    (view?.parent as? FrameLayout)?.apply {
        layoutParams?.height = CoordinatorLayout.LayoutParams.MATCH_PARENT
        requestLayout() // TODO necessary?
    }
}

// Disable sheet swipe on content scroll to avoid accidentally closing the sheet when scrolling items.
fun BottomSheetDialogFragment.blockContentScroll(recyclerView: RecyclerView) {
    recyclerView.addOnScrollListenerAdapter(
        onScrolled = { _, _, dy ->
            if (dy != 0) behavior?.isDraggable = false
        },
        onScrollStateChanged = { _, newState ->
            if (newState == RecyclerView.SCROLL_STATE_IDLE) behavior?.isDraggable = true
        },
    )
}