package com.example.util.simpletimetracker.core.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(
        owner,
        object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        },
    )
}

fun <T> LiveData<T>.set(data: T) {
    if (this is MutableLiveData) value = data
}

fun <T> LiveData<T>.post(data: T) {
    if (this is MutableLiveData) postValue(data)
}

fun <T1, T2, T3> combineLiveData(
    f1: LiveData<T1>,
    f2: LiveData<T2>,
    f3: LiveData<T3>,
): LiveData<Triple<T1?, T2?, T3?>> = MediatorLiveData<Triple<T1?, T2?, T3?>>().also { mediator ->
    mediator.setValueIfNotEqual(Triple(f1.value, f2.value, f3.value))

    mediator.addSource(f1) { t1: T1? ->
        val (_, t2, t3) = mediator.value!!
        mediator.setValueIfNotEqual(Triple(t1, t2, t3))
    }

    mediator.addSource(f2) { t2: T2? ->
        val (t1, _, t3) = mediator.value!!
        mediator.setValueIfNotEqual(Triple(t1, t2, t3))
    }

    mediator.addSource(f3) { t3: T3? ->
        val (t1, t2, _) = mediator.value!!
        mediator.setValueIfNotEqual(Triple(t1, t2, t3))
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

fun <T> ViewModel.lazySuspend(
    initializer: suspend () -> T,
): Lazy<MutableLiveData<T>> = lazy {
    MutableLiveData<T>().let { initial ->
        viewModelScope.launch {
            initial.value = initializer()
        }
        initial
    }
}
