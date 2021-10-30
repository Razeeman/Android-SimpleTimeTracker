package com.example.util.simpletimetracker.core.extension

import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.utils.BuildVersions

fun Fragment.hideKeyboard() {
    activity?.hideKeyboard()
}

fun Fragment.showKeyboard(view: View) {
    activity?.showKeyboard(view)
}

fun Fragment.getAllFragments(): List<Fragment> {
    val fm = childFragmentManager
    return fm.fragments + fm.fragments.map(Fragment::getAllFragments).flatten()
}

fun Fragment.setSharedTransitions(
    additionalCondition: () -> Boolean = { true },
    transitionName: String,
    sharedView: View,
) {
    if (BuildVersions.isLollipopOrHigher() && additionalCondition.invoke()) {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }
    ViewCompat.setTransitionName(sharedView, transitionName)
}
