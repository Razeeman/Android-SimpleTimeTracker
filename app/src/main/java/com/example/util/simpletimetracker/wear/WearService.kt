/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.wear

import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.wearrpc.WearRPCServer
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Landing point for messages coming to Mobile from Wear.
 *
 * Implemented as a Service so that it can receive messages even when the Mobile app is stopped or
 * in the background.
 */
@AndroidEntryPoint
class WearService : WearableListenerService() {

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor

    @Inject
    lateinit var recordTagInteractor: RecordTagInteractor

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor

    @Inject
    lateinit var appColorMapper: AppColorMapper

    override fun onRequest(nodeId: String, path: String, request: ByteArray): Task<ByteArray>? {
        val rpc = WearRPCServer(
            DomainAPI(
                prefsInteractor,
                recordTypeInteractor,
                recordTagInteractor,
                runningRecordInteractor,
                appColorMapper,
            ),
        )
        return runBlocking { Tasks.forResult(rpc.onRequest(path, request)) }
    }
}
