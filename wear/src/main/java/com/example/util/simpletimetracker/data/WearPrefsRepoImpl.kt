/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import android.content.SharedPreferences
import com.example.util.simpletimetracker.domain.repo.WearPrefsRepo
import com.example.util.simpletimetracker.utils.delegate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearPrefsRepoImpl @Inject constructor(
    prefs: SharedPreferences,
) : WearPrefsRepo {

    override var wearShowCompactList: Boolean by prefs.delegate(
        KEY_WEAR_SHOW_COMPACT_LIST, false,
    )

    @Suppress("unused")
    companion object {
        private const val KEY_WEAR_SHOW_COMPACT_LIST = "wearShowCompactList"
    }
}