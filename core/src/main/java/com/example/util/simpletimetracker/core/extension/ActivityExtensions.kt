package com.example.util.simpletimetracker.core.extension

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity

fun FragmentActivity.hideKeyboard() {
    this.currentFocus?.apply {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(windowToken, 0)
    }
}

fun Activity?.showKeyboard(view: View) {
    (this?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
        ?.apply {
            currentFocus?.clearFocus()
            view.requestFocus()
            showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
}