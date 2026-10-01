package com.example.util.simpletimetracker.navigation

interface WearNavigationRoute<T> {
    val key: String
    val baseRoute: String

    fun wrappedKey(): String {
        return "{$key}"
    }

    object Activities : WearNavigationRoute<Nothing> {
        override val key: String = ""
        override val baseRoute: String = "activities"
    }

    object Tags : WearNavigationRoute<Long> {
        override val key: String = "id"
        override val baseRoute: String = "activities/${wrappedKey()}/tags"
    }

    object TagValue : WearNavigationRoute<Long> {
        override val key: String = "id"
        override val baseRoute: String = "activities/tag/${wrappedKey()}/value/"
    }

    object Statistics : WearNavigationRoute<Nothing> {
        override val key: String = ""
        override val baseRoute: String = "statistics"
    }

    object Records : WearNavigationRoute<Nothing> {
        override val key: String = ""
        override val baseRoute: String = "records"
    }

    object Settings : WearNavigationRoute<Nothing> {
        override val key: String = ""
        override val baseRoute: String = "settings"
    }

    object Alert : WearNavigationRoute<Int> {
        override val key: String = "textResId"
        override val baseRoute: String = "alert/${wrappedKey()}"
    }

    object DatePicker : WearNavigationRoute<Long> {
        override val key: String = "timestamp"
        override val baseRoute: String = "datePicker/${wrappedKey()}"
    }
}