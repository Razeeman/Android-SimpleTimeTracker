package com.example.util.simpletimetracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_QUERY_ACTIVITIES
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_QUERY_RUNNING
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_RESPONSE_ACTIVITIES
import com.example.util.simpletimetracker.core.utils.ACTION_EXTERNAL_RESPONSE_RUNNING
import com.example.util.simpletimetracker.core.utils.EXTRA_ANSWER_TYPE
import com.example.util.simpletimetracker.core.utils.EXTRA_DATA
import com.example.util.simpletimetracker.utils.BaseUiTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the ACTION_EXTERNAL_QUERY_ACTIVITIES / ACTION_EXTERNAL_QUERY_RUNNING broadcast API
 * end to end: sends the query broadcast and asserts on the actual response broadcast content,
 * instead of only checking the settings toggle.
 */
@RunWith(AndroidJUnit4::class)
class ExternalQueryBroadcastTest : BaseUiTest() {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun queryIgnoredWhenSettingDisabled() {
        testUtils.addActivity("work")

        val data = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_ACTIVITIES,
            responseAction = ACTION_EXTERNAL_RESPONSE_ACTIVITIES,
            timeoutSeconds = 2,
        )

        assertNull(data)
    }

    @Test
    fun queryActivitiesSimple() {
        enableReceiveQueries()
        testUtils.addActivity("work")

        val data = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_ACTIVITIES,
            responseAction = ACTION_EXTERNAL_RESPONSE_ACTIVITIES,
        ).orEmpty()

        assertTrue(data.startsWith("work:"))
        val hex = data.substringAfter("work:")
        assertTrue(hex.matches(Regex("[0-9a-f]{6}")))
    }

    @Test
    fun queryActivitiesHiddenExcluded() {
        enableReceiveQueries()
        testUtils.addActivity("work")
        testUtils.addActivity("archived", archived = true)

        val data = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_ACTIVITIES,
            responseAction = ACTION_EXTERNAL_RESPONSE_ACTIVITIES,
        ).orEmpty()

        assertTrue(data.contains("work:"))
        assertTrue(!data.contains("archived:"))
    }

    @Test
    fun queryActivitiesJsonMatchesSimpleColor() {
        enableReceiveQueries()
        testUtils.addActivity("work")

        val simpleData = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_ACTIVITIES,
            responseAction = ACTION_EXTERNAL_RESPONSE_ACTIVITIES,
        ).orEmpty()
        val jsonData = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_ACTIVITIES,
            responseAction = ACTION_EXTERNAL_RESPONSE_ACTIVITIES,
            answerType = "json",
        ).orEmpty()

        val simpleHex = simpleData.substringAfter("work:")
        val jsonColor = Regex("\"color\":(-?\\d+)").find(jsonData)?.groupValues?.get(1)?.toLong()

        assertTrue(jsonData.contains("\"name\":\"work\""))
        assertEquals(simpleHex, jsonColor?.let { String.format("%06x", it and 0xFFFFFF) })
    }

    @Test
    fun queryRunningSimple() {
        enableReceiveQueries()
        testUtils.addActivity("work")
        val timeStarted = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5)
        testUtils.addRunningRecord("work", timeStarted = timeStarted)

        val data = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_RUNNING,
            responseAction = ACTION_EXTERNAL_RESPONSE_RUNNING,
        )

        assertEquals("work:${timeStarted / 1000}", data)
    }

    @Test
    fun queryRunningEmptyWhenNothingRunning() {
        enableReceiveQueries()

        val data = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_RUNNING,
            responseAction = ACTION_EXTERNAL_RESPONSE_RUNNING,
        )

        assertEquals("", data)
    }

    @Test
    fun queryRunningJsonContainsId() {
        enableReceiveQueries()
        testUtils.addActivity("work")
        testUtils.addRunningRecord("work")

        val jsonData = sendAndAwaitResponse(
            queryAction = ACTION_EXTERNAL_QUERY_RUNNING,
            responseAction = ACTION_EXTERNAL_RESPONSE_RUNNING,
            answerType = "json",
        ).orEmpty()

        assertTrue(jsonData.contains("\"startedAt\""))
        assertTrue(jsonData.contains("\"id\""))
    }

    private fun enableReceiveQueries() = runBlocking {
        prefsInteractor.setAutomatedTrackingReceiveQueries(true)
    }

    private fun sendAndAwaitResponse(
        queryAction: String,
        responseAction: String,
        answerType: String? = null,
        timeoutSeconds: Long = 5,
    ): String? {
        val latch = CountDownLatch(1)
        var data: String? = null
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                data = intent?.getStringExtra(EXTRA_DATA)
                latch.countDown()
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(responseAction),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        try {
            Intent(queryAction).apply {
                setPackage(context.packageName)
                if (answerType != null) putExtra(EXTRA_ANSWER_TYPE, answerType)
            }.let(context::sendBroadcast)
            latch.await(timeoutSeconds, TimeUnit.SECONDS)
        } finally {
            context.unregisterReceiver(receiver)
        }
        return data
    }
}
