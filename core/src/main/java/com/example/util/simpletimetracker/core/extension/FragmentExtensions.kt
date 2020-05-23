package com.example.util.simpletimetracker.core.extension

import android.view.View
import androidx.fragment.app.Fragment

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
