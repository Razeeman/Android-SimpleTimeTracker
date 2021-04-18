package com.example.util.simpletimetracker.core.repo

import android.content.Context
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.domain.di.AppContext
import javax.inject.Inject

class IconImageRepo @Inject constructor(
    @AppContext private val context: Context
) {

    fun getImages(arrayResId: Int): Map<String, Int> {
        val res = mutableMapOf<String, Int>()

        val ta = context.resources.obtainTypedArray(arrayResId)
        (0 until ta.length()).forEach {
            ta.getResourceId(it, R.drawable.unknown).let { resId ->
                res[context.resources.getResourceEntryName(resId)] = resId
            }
        }
        ta.recycle()

        return res
    }
}