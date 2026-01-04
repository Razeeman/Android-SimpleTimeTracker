package com.example.util.simpletimetracker.data_local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
class AppDatabaseMigrations {

    companion object {
        val migrations: List<Migration>
            get() = listOf(
                migration_1_2,
                migration_2_3,
                migration_3_4,
                migration_4_5,
                migration_5_6,
                migration_6_7,
                migration_7_8,
                migration_8_9,
                migration_9_10,
                migration_10_11,
                migration_11_12,
                migration_12_13,
                migration_13_14,
                migration_14_15,
                migration_15_16,
                migration_16_17,
                migration_17_18,
                migration_18_19,
                migration_19_20,
                migration_20_21,
                migration_21_22,
                migration_22_23,
                migration_23_24,
                migration_24_25,
                migration_25_26,
                migration_26_27,
                migration_27_28,
                migration_28_29,
                migration_29_30,
            )

        private val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL(
                    "CREATE TABLE recordTypes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, icon TEXT NOT NULL, color INTEGER NOT NULL, hidden INTEGER NOT NULL)",
                )
                // Copy the data
                database.execSQL(
                    "INSERT INTO recordTypes_new (id, name, icon, color, hidden) SELECT id, name, '', color, hidden FROM recordTypes",
                )
                // Remove the old table
                database.execSQL("DROP TABLE recordTypes")
                // Change the table name to the correct one
                database.execSQL("ALTER TABLE recordTypes_new RENAME TO recordTypes")
            }
        }

        private val migration_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL)",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordTypeCategory` (`record_type_id` INTEGER NOT NULL, `category_id` INTEGER NOT NULL, PRIMARY KEY(`record_type_id`, `category_id`))",
                )
            }
        }

        private val migration_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN goal_time INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE records ADD COLUMN comment TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        private val migration_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE runningRecords ADD COLUMN comment TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        private val migration_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordTags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `archived` INTEGER NOT NULL)",
                )
                database.execSQL(
                    "ALTER TABLE records ADD COLUMN tag_id INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE runningRecords ADD COLUMN tag_id INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val migration_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN color INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordToRecordTag` (`record_id` INTEGER NOT NULL, `record_tag_id` INTEGER NOT NULL, PRIMARY KEY(`record_id`, `record_tag_id`))",
                )
                database.execSQL(
                    "INSERT INTO recordToRecordTag (record_id, record_tag_id) SELECT id, tag_id from records WHERE tag_id != 0",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `runningRecordToRecordTag` (`running_record_id` INTEGER NOT NULL, `record_tag_id` INTEGER NOT NULL, PRIMARY KEY(`running_record_id`, `record_tag_id`))",
                )
            }
        }

        private val migration_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN color_int TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE categories ADD COLUMN color_int TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN color_int TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        private val migration_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `activityFilters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `selectedIds` TEXT NOT NULL, `type` INTEGER NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, `color_int` TEXT NOT NULL, `selected` INTEGER NOT NULL)",
                )
            }
        }

        private val migration_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN daily_goal_time INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN weekly_goal_time INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val migration_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN monthly_goal_time INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val migration_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favouriteComments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `comment` TEXT NOT NULL)",
                )
            }
        }

        private val migration_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table for goals
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordTypeGoals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type_id` INTEGER NOT NULL, `range` INTEGER NOT NULL, `type` INTEGER NOT NULL, `value` INTEGER NOT NULL)",
                )
                // Migrate goals
                // session range = 0, daily range = 1, weekly range = 2, monthly range = 3
                database.execSQL(
                    "INSERT INTO recordTypeGoals (type_id, range, type, value) SELECT id, 0, 0, goal_time FROM recordTypes WHERE goal_time != 0",
                )
                database.execSQL(
                    "INSERT INTO recordTypeGoals (type_id, range, type, value) SELECT id, 1, 0, daily_goal_time FROM recordTypes WHERE daily_goal_time != 0",
                )
                database.execSQL(
                    "INSERT INTO recordTypeGoals (type_id, range, type, value) SELECT id, 2, 0, weekly_goal_time FROM recordTypes WHERE weekly_goal_time != 0",
                )
                database.execSQL(
                    "INSERT INTO recordTypeGoals (type_id, range, type, value) SELECT id, 3, 0, monthly_goal_time FROM recordTypes WHERE monthly_goal_time != 0",
                )
                // Create the new table
                database.execSQL(
                    "CREATE TABLE `recordTypes_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `icon` TEXT NOT NULL, `color` INTEGER NOT NULL, `color_int` TEXT NOT NULL, `hidden` INTEGER NOT NULL)",
                )
                // Copy the data
                database.execSQL(
                    "INSERT INTO recordTypes_new (id, name, icon, color, color_int, hidden) SELECT id, name, icon, color, color_int, hidden FROM recordTypes",
                )
                // Remove the old table
                database.execSQL("DROP TABLE recordTypes")
                // Change the table name to the correct one
                database.execSQL("ALTER TABLE recordTypes_new RENAME TO recordTypes")
            }
        }

        private val migration_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypeGoals ADD COLUMN category_id INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val migration_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypeGoals ADD COLUMN days_of_week TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        private val migration_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN icon TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN icon_color_source INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordTypeToTag` (`record_type_id` INTEGER NOT NULL, `record_tag_id` INTEGER NOT NULL, PRIMARY KEY(`record_type_id`, `record_tag_id`))",
                )
                database.execSQL(
                    "INSERT INTO recordTypeToTag (record_type_id, record_tag_id) SELECT type_id, id from recordTags WHERE type_id != 0",
                )
                // Just in case. Better leave tags with no colors and icons than loose all data.
                runCatching {
                    database.execSQL(
                        "UPDATE recordTags SET icon_color_source = type_id",
                    )
                    database.execSQL(
                        "UPDATE recordTags SET color = (SELECT recordTypes.color FROM recordTypes WHERE recordTypes.id = recordTags.type_id), icon = (SELECT recordTypes.icon FROM recordTypes WHERE recordTypes.id = recordTags.type_id) WHERE EXISTS (SELECT * FROM recordTypes WHERE recordTypes.id = recordTags.type_id)",
                    )
                }
            }
        }

        private val migration_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordTypeToDefaultTag` (`record_type_id` INTEGER NOT NULL, `record_tag_id` INTEGER NOT NULL, PRIMARY KEY(`record_type_id`, `record_tag_id`))",
                )
            }
        }

        private val migration_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favouriteIcons` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `icon` TEXT NOT NULL)",
                )
            }
        }

        private val migration_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `complexRules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `disabled` INTEGER NOT NULL, `actionType` INTEGER NOT NULL, `actionSetTagIds` TEXT NOT NULL, `conditionStartingTypeIds` TEXT NOT NULL, `conditionCurrentTypeIds` TEXT NOT NULL, `conditionDaysOfWeek` TEXT NOT NULL)",
                )
            }
        }

        private val migration_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN instant INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val migration_20_21 = object : Migration(20, 21) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN instantDuration INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val migration_21_22 = object : Migration(21, 22) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN note TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN note TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE categories ADD COLUMN note TEXT NOT NULL DEFAULT ''",
                )
            }
        }

        private val migration_22_23 = object : Migration(22, 23) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favouriteColors` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `color_int` TEXT NOT NULL)",
                )
            }
        }

        private val migration_23_24 = object : Migration(23, 24) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypeGoals ADD COLUMN goalType INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        private val migration_24_25 = object : Migration(24, 25) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `activitySuggestion` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `forTypeId` INTEGER NOT NULL, `suggestionIds` TEXT NOT NULL)",
                )
            }
        }

        private val migration_25_26 = object : Migration(25, 26) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `durationSuggestions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `valueSeconds` INTEGER NOT NULL)",
                )
            }
        }

        private val migration_26_27 = object : Migration(26, 27) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN `value_type` INTEGER",
                )
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN `value_suffix` TEXT",
                )
                database.execSQL(
                    "ALTER TABLE recordToRecordTag ADD COLUMN `record_tag_numeric_value` REAL",
                )
                database.execSQL(
                    "ALTER TABLE runningRecordToRecordTag ADD COLUMN `record_tag_numeric_value` REAL",
                )
            }
        }

        private val migration_27_28 = object : Migration(27, 28) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordShortcuts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type_id` INTEGER NOT NULL, `comment` TEXT NOT NULL)",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordShortcutToRecordTag` (`shortcut_id` INTEGER NOT NULL, `record_tag_id` INTEGER NOT NULL, `record_tag_numeric_value` REAL, PRIMARY KEY(`shortcut_id`, `record_tag_id`))",
                )
            }
        }

        private val migration_28_29 = object : Migration(28, 29) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favouriteRecordFilters` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)",
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favouriteRecordFilter` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `owner_id` INTEGER NOT NULL, `type` INTEGER NOT NULL, `common_items_ids` TEXT, `comment_items_ids` TEXT, `comment_items_text` TEXT, `duplication_items_ids` TEXT, `manually_filtered_items_ids` TEXT, `daysOfWeek` TEXT, `range_time_started` INTEGER, `range_time_ended` INTEGER, `range_length_type` INTEGER, `range_length_last_days` INTEGER, `range_length_position` INTEGER, `range_length_custom_range_time_started` INTEGER, `range_length_custom_range_time_ended` INTEGER, FOREIGN KEY(`owner_id`) REFERENCES `favouriteRecordFilters`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
                )
            }
        }

        private val migration_29_30 = object : Migration(29, 30) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table for goals
                database.execSQL(
                    "CREATE TABLE `recordTypeGoals_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `owner_id` INTEGER NOT NULL, `owner_type` INTEGER NOT NULL, `range` INTEGER NOT NULL, `type` INTEGER NOT NULL, `goalType` INTEGER NOT NULL, `value` INTEGER NOT NULL, `days_of_week` TEXT NOT NULL)",
                )
                // Migrate goals
                // Owner type 0 - activity, 1 - category
                database.execSQL(
                    "INSERT INTO recordTypeGoals_new (id, owner_id, owner_type, range, type, goalType, value, days_of_week) SELECT id, type_id, 0, range, type, goalType, value, days_of_week FROM recordTypeGoals WHERE type_id != 0",
                )
                database.execSQL(
                    "INSERT INTO recordTypeGoals_new (id, owner_id, owner_type, range, type, goalType, value, days_of_week) SELECT id, category_id, 1, range, type, goalType, value, days_of_week FROM recordTypeGoals WHERE category_id != 0",
                )
                // Remove the old table
                database.execSQL("DROP TABLE recordTypeGoals")
                // Change the table name to the correct one
                database.execSQL("ALTER TABLE recordTypeGoals_new RENAME TO recordTypeGoals")
            }
        }
    }
}