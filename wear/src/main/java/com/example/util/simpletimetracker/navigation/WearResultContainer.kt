/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.navigation

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearResultContainer @Inject constructor() {
    private val listeners: MutableMap<String, ResultListener> = mutableMapOf()

    fun setResultListener(key: String, listener: ResultListener) {
        listeners[key] = listener
    }

    fun sendResult(key: String, data: Any?) {
        listeners.remove(key)?.onResult(data)
    }

    fun interface ResultListener {
        fun onResult(data: Any?)
    }
}