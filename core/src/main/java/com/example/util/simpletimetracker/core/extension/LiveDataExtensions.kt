package com.example.util.simpletimetracker.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(
        owner,
        object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        }
    )
}

fun <T> LiveData<T>.post(data: T) =
    (this as MutableLiveData).postValue(data)

fun <T> LiveData<T>.set(data: T) {
    (this as MutableLiveData).value = data
}