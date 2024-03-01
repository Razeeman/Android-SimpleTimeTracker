package com.example.util.simpletimetracker.wearrpc

class MockWearCommunicationAPI : WearCommunicationAPI {

    var activities: Array<Activity> = arrayOf()
    var currentActivities: Array<CurrentActivity> = arrayOf()
    var tags: Map<Long, Array<Tag>> = mapOf()
    lateinit var settings: Settings

    override suspend fun queryActivities(): Array<Activity> {
        return activities
    }

    fun mock_queryActivities(activities: Array<Activity>) {
        this.activities = activities
    }

    override suspend fun queryCurrentActivities(): Array<CurrentActivity> {
        return currentActivities
    }

    fun mock_queryCurrentActivities(activities: Array<CurrentActivity>) {
        this.currentActivities = activities
    }

    override suspend fun setCurrentActivities(activities: Array<CurrentActivity>) {
        TODO("Not yet implemented")
    }

    override suspend fun queryTagsForActivity(activityId: Long): Array<Tag> {
        return this.tags[activityId] ?: arrayOf()
    }

    fun mock_queryTagsForActivity(tags: Map<Long, Array<Tag>>) {
        this.tags = tags
    }

    override suspend fun querySettings(): Settings {
        return settings
    }

    fun mock_querySettings(settings: Settings) {
        this.settings = settings
    }

    fun mockReset() {
        this.activities = arrayOf()
        this.currentActivities = arrayOf()
        this.tags = mapOf()
    }

}