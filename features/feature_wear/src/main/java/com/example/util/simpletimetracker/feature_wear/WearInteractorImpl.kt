/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import javax.inject.Inject

class WearInteractorImpl @Inject constructor(
    private val wearRPCServer: WearRPCServer,
) : WearInteractor {

    override suspend fun update() {
        wearRPCServer.updateData()
    }
}