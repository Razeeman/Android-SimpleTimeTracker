package com.example.util.simpletimetracker.core.extension

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun Activity.hideKeyboard(flags: Int = 0) {
    val currentFocus = currentFocus ?: return
    getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(currentFocus.windowToken, flags)
}

fun Activity.showKeyboard(view: View) {
    currentFocus?.clearFocus()
    view.requestFocus()
    getSystemService<InputMethodManager>()?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun FragmentActivity.getAllFragments(): List<Fragment> {
    val fm = supportFragmentManager
    return fm.fragments + fm.fragments.map(Fragment::getAllFragments).flatten()
}