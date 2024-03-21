/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.repo.WearPrefsRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WearPrefsInteractor @Inject constructor(
    private val wearPrefsRepo: WearPrefsRepo,
) {

    suspend fun getWearShowCompactList(): Boolean = withContext(Dispatchers.IO) {
        wearPrefsRepo.wearShowCompactList
    }

    suspend fun setWearShowCompactList(enabled: Boolean) = withContext(Dispatchers.IO) {
        wearPrefsRepo.wearShowCompactList = enabled
    }
}