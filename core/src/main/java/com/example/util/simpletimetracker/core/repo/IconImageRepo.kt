package com.example.util.simpletimetracker.core.repo

import android.content.Context
import com.example.util.simpletimetracker.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IconImageRepo @Inject constructor(
    @ApplicationContext private val context: Context
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

    fun getHints(arrayResId: Int): List<String> {
        val res = mutableListOf<String>()

        val ta = context.resources.obtainTypedArray(arrayResId)
        (0 until ta.length()).forEach {
            ta.getString(it).orEmpty().let(res::add)
        }
        ta.recycle()

        return res
    }
}