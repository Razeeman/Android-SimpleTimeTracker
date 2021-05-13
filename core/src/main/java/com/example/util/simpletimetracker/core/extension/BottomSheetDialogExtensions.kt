package com.example.util.simpletimetracker.core.extension

import android.view.View
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.util.simpletimetracker.core.R
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