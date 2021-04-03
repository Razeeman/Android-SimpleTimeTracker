package com.example.util.simpletimetracker.utils

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.`is`

fun recyclerItemCount(expectedCount: Int) =
    ViewAssertion { view, noViewFoundException ->
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val itemCount = (view as RecyclerView).adapter!!.itemCount
        assertThat(itemCount, `is`(expectedCount))
    }