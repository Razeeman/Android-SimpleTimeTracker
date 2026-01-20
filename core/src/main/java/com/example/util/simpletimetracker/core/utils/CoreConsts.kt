package com.example.util.simpletimetracker.core.utils

// Same values written in manifest.
const val SHORTCUT_NAVIGATION_KEY = "shortcutNavigationTab"
const val SHORTCUT_NAVIGATION_RECORDS = "recordsTab"
const val SHORTCUT_NAVIGATION_STATISTICS = "statisticsTab"
const val SHORTCUT_NAVIGATION_SETTINGS = "settingsTab"

// Same values written in manifest.
const val ACTION_EXTERNAL_START_ACTIVITY = "com.razeeman.util.simpletimetracker.ACTION_START_ACTIVITY"
const val ACTION_EXTERNAL_STOP_ACTIVITY = "com.razeeman.util.simpletimetracker.ACTION_STOP_ACTIVITY"
const val ACTION_EXTERNAL_STOP_ALL_ACTIVITIES = "com.razeeman.util.simpletimetracker.ACTION_STOP_ALL_ACTIVITIES"
const val ACTION_EXTERNAL_STOP_SHORTEST_ACTIVITY = "com.razeeman.util.simpletimetracker.ACTION_STOP_SHORTEST_ACTIVITY"
const val ACTION_EXTERNAL_STOP_LONGEST_ACTIVITY = "com.razeeman.util.simpletimetracker.ACTION_STOP_LONGEST_ACTIVITY"
const val ACTION_EXTERNAL_RESTART_ACTIVITY = "com.razeeman.util.simpletimetracker.ACTION_RESTART_ACTIVITY"
const val ACTION_EXTERNAL_ADD_RECORD = "com.razeeman.util.simpletimetracker.ACTION_ADD_RECORD"
const val ACTION_EXTERNAL_CHANGE_RECORD = "com.razeeman.util.simpletimetracker.ACTION_CHANGE_RECORD"
const val ACTION_EXTERNAL_CREATE_RECORD_TAG = "com.razeeman.util.simpletimetracker.ACTION_CREATE_TAG"

const val EVENT_STARTED_ACTIVITY = "com.razeeman.util.simpletimetracker.EVENT_STARTED_ACTIVITY"
const val EVENT_STOPPED_ACTIVITY = "com.razeeman.util.simpletimetracker.EVENT_STOPPED_ACTIVITY"
const val EVENT_COMPLETED_GOAL = "com.razeeman.util.simpletimetracker.EVENT_COMPLETED_GOAL"

const val EXTRA_ACTIVITY_NAME = "extra_activity_name"
const val EXTRA_CATEGORY_NAME = "extra_category_name"
const val EXTRA_RECORD_COMMENT = "extra_record_comment"
const val EXTRA_RECORD_TAG_NAME = "extra_record_tag"
const val EXTRA_RECORD_TYPE_NOTE = "extra_record_type_note"
const val EXTRA_RECORD_TYPE_ICON = "extra_record_type_icon"
const val EXTRA_RECORD_TIME_STARTED = "extra_record_time_started"
const val EXTRA_RECORD_TIME_ENDED = "extra_record_time_ended"
const val EXTRA_RECORD_COMMENT_MODE = "extra_record_comment_mode" // set, append, prefix
const val EXTRA_FIND_RECORD_MODE = "extra_find_record_mode" // current_or_last, current, last
const val EXTRA_FIND_RECORD_WITH_ACTIVITY_NAME = "extra_find_record_with_activity_name"
const val EXTRA_GOAL_TYPE = "extra_goal_type" // duration, count
const val EXTRA_GOAL_VALUE = "extra_goal_value"

const val DELAY_DATA_LOAD_MS = 300L // Same as @integer/screen_animation_time