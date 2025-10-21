package com.example.util.simpletimetracker.api

import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import com.example.util.simpletimetracker.wear_api.WearStartActivityRequest
import com.example.util.simpletimetracker.wear_api.WearStopActivityRequest
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class WebApiAdapter @Inject constructor(
    // Reuse the SAME interface that Wear OS uses!
    private val wearApi: WearCommunicationAPI,
) : NanoHTTPD(8080) {

    override fun serve(session: IHTTPSession): Response {
        val headers = mutableMapOf(
            "Access-Control-Allow-Origin" to "*",
            "Access-Control-Allow-Methods" to "GET, POST, OPTIONS",
            "Access-Control-Allow-Headers" to "Content-Type"
        )

        if (session.method == Method.OPTIONS) {
            return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "")
                .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
        }

        return try {
            when {
                // GET /api/activities
                session.uri == "/api/activities" && session.method == Method.GET -> {
                    getAllActivities(headers)
                }
                // GET /api/running
                session.uri == "/api/running" && session.method == Method.GET -> {
                    getRunningActivities(headers)
                }
                // POST /api/start/:id
                session.uri.startsWith("/api/start/") && session.method == Method.POST -> {
                    val id = session.uri.substringAfterLast("/").toLongOrNull()
                    startActivity(id, headers)
                }
                // POST /api/stop/:id
                session.uri.startsWith("/api/stop/") && session.method == Method.POST -> {
                    val id = session.uri.substringAfterLast("/").toLongOrNull()
                    stopActivity(id, headers)
                }
                else -> {
                    newFixedLengthResponse(
                        Response.Status.NOT_FOUND,
                        "application/json",
                        """{"error": "Not found"}"""
                    ).apply { headers.forEach { (k, v) -> addHeader(k, v) } }
                }
            }
        } catch (e: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "application/json",
                """{"error": "${e.message}"}"""
            ).apply { headers.forEach { (k, v) -> addHeader(k, v) } }
        }
    }

    private fun getAllActivities(headers: Map<String, String>): Response = runBlocking {
        // Reuse the EXACT same method Wear OS uses!
        val activities = wearApi.queryActivities()
        val currentState = wearApi.queryCurrentActivities()
        val runningIds = currentState.currentActivities.map { it.id }.toSet()

        val json = JSONArray()
        activities.forEach { activity ->
            json.put(JSONObject().apply {
                put("id", activity.id)
                put("name", activity.name)
                put("icon", activity.icon)
                put("color", activity.color)
                put("isRunning", runningIds.contains(activity.id))
            })
        }

        newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        ).apply {
            headers.forEach { (k, v) -> addHeader(k, v) }
        }
    }

    private fun getRunningActivities(headers: Map<String, String>): Response = runBlocking {
        // Reuse the EXACT same method Wear OS uses!
        val currentState = wearApi.queryCurrentActivities()
        val activities = wearApi.queryActivities().associateBy { it.id }

        val json = JSONArray()
        currentState.currentActivities.forEach { current ->
            val activity = activities[current.id]
            json.put(JSONObject().apply {
                put("id", current.id)
                put("name", activity?.name ?: "Unknown")
                put("timeStarted", current.startedAt)
                put("duration", System.currentTimeMillis() - current.startedAt)
            })
        }

        newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        ).apply {
            headers.forEach { (k, v) -> addHeader(k, v) }
        }
    }

    private fun startActivity(id: Long?, headers: Map<String, String>): Response = runBlocking {
        if (id == null) {
            return@runBlocking newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                "application/json",
                """{"error": "Invalid ID"}"""
            )
        }

        // Reuse the EXACT same method Wear OS uses!
        wearApi.startActivity(WearStartActivityRequest(id = id, tags = null))

        newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            """{"success": true}"""
        ).apply {
            headers.forEach { (k, v) -> addHeader(k, v) }
        }
    }

    private fun stopActivity(id: Long?, headers: Map<String, String>): Response = runBlocking {
        if (id == null) {
            return@runBlocking newFixedLengthResponse(
                Response.Status.BAD_REQUEST,
                "application/json",
                """{"error": "Invalid ID"}"""
            )
        }

        // Reuse the EXACT same method Wear OS uses!
        wearApi.stopActivity(WearStopActivityRequest(id = id))

        newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            """{"success": true}"""
        ).apply {
            headers.forEach { (k, v) -> addHeader(k, v) }
        }
    }
}