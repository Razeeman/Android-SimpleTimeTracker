package com.example.util.simpletimetracker.core.extension

import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.example.util.simpletimetracker.core.utils.doOnEnd
import com.example.util.simpletimetracker.feature_views.extension.animateScaleBoop

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

inline fun Fragment.setSharedTransitions(
    additionalCondition: () -> Boolean = { true },
    transitionName: String,
    sharedView: View,
) {
    val context = this.context ?: return
    if (additionalCondition.invoke()) {
        val transition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        transition?.apply {
            interpolator = DecelerateInterpolator()
            doOnEnd { sharedView.animateScaleBoop(scale = 1.03f, duration = 100) }
        }
        sharedElementEnterTransition = transition
    }
    ViewCompat.setTransitionName(sharedView, transitionName)
}

fun Fragment.addOnBackPressedListener(
    isEnabled: Boolean = true,
    action: () -> Unit,
): OnBackPressedCallback {
    val callback = object : OnBackPressedCallback(isEnabled) {
        override fun handleOnBackPressed() = action()
    }
    // Using fragment lifecycle to avoid listener removed on view destroy event.
    activity?.onBackPressedDispatcher?.addCallback(this, callback)
    return callback
}

fun Fragment.getDrawable(@DrawableRes resId: Int): Drawable? {
    return context?.let { ResourcesCompat.getDrawable(resources, resId, it.theme) }
}
