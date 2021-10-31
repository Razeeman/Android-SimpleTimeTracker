package com.example.util.simpletimetracker.core.utils

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.properties.ReadOnlyProperty

inline fun <reified T : Parcelable> fragmentArgumentDelegate(
    key: String,
    default: T,
) = ReadOnlyProperty<Fragment, T> { thisRef, _ ->
    thisRef.arguments?.getParcelable(key) ?: default
}

inline fun <reified T : Parcelable> activityArgumentDelegate(
    key: String,
    default: T,
) = ReadOnlyProperty<FragmentActivity, T> { thisRef, _ ->
    thisRef.intent?.getParcelableExtra(key) ?: default
}