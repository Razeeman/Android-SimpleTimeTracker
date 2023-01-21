package com.example.util.simpletimetracker.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

fun <T> LiveData<T>.set(data: T) {
    if (this is MutableLiveData) value = data
}

fun <T> LiveData<T>.post(data: T) {
    if (this is MutableLiveData) postValue(data)
}

fun <T1, T2> combineTuple(
    f1: LiveData<T1>,
    f2: LiveData<T2>,
): LiveData<Pair<T1?, T2?>> = MediatorLiveData<Pair<T1?, T2?>>().also { mediator ->
    mediator.setValueIfNotEqual(Pair(f1.value, f2.value))

    mediator.addSource(f1) { t1: T1? ->
        val (_, t2) = mediator.value!!
        mediator.setValueIfNotEqual(Pair(t1, t2))
    }

    mediator.addSource(f2) { t2: T2? ->
        val (t1, _) = mediator.value!!
        mediator.setValueIfNotEqual(Pair(t1, t2))
    }
}

private fun <T : Any?> MutableLiveData<T>.setValueIfNotEqual(arg: T) {
    @Suppress("SuspiciousEqualsCombination")
    fun objectsEquals(a: T?, b: T?): Boolean {
        return (a === b) || (a != null && a == b)
    }

    val value = value
    if (!objectsEquals(value, arg)) {
        this.value = arg
    }
}