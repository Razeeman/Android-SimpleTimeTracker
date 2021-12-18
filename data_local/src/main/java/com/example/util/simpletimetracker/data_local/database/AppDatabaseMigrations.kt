package com.example.util.simpletimetracker.data_local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AppDatabaseMigrations {

    companion object {
        val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL(
                    "CREATE TABLE recordTypes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, icon TEXT NOT NULL, color INTEGER NOT NULL, hidden INTEGER NOT NULL)"
                )

                // Copy the data
                database.execSQL(
                    "INSERT INTO recordTypes_new (id, name, icon, color, hidden) SELECT id, name, '', color, hidden FROM recordTypes"
                )

                // Remove the old table
                database.execSQL("DROP TABLE recordTypes")

                // Change the table name to the correct one
                database.execSQL("ALTER TABLE recordTypes_new RENAME TO recordTypes")
            }
        }

        val migration_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL)"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordTypeCategory` (`record_type_id` INTEGER NOT NULL, `category_id` INTEGER NOT NULL, PRIMARY KEY(`record_type_id`, `category_id`))"
                )
            }
        }

        val migration_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN goal_time INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE records ADD COLUMN comment TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val migration_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE runningRecords ADD COLUMN comment TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        val migration_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordTags` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type_id` INTEGER NOT NULL, `name` TEXT NOT NULL, `archived` INTEGER NOT NULL)"
                )
                database.execSQL(
                    "ALTER TABLE records ADD COLUMN tag_id INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE runningRecords ADD COLUMN tag_id INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val migration_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN color INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recordToRecordTag` (`record_id` INTEGER NOT NULL, `record_tag_id` INTEGER NOT NULL, PRIMARY KEY(`record_id`, `record_tag_id`))"
                )
                database.execSQL(
                    "INSERT INTO recordToRecordTag (record_id, record_tag_id) SELECT id, tag_id from records WHERE tag_id != 0"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `runningRecordToRecordTag` (`running_record_id` INTEGER NOT NULL, `record_tag_id` INTEGER NOT NULL, PRIMARY KEY(`running_record_id`, `record_tag_id`))"
                )
            }
        }

        val migration_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE recordTypes ADD COLUMN color_int TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE categories ADD COLUMN color_int TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE recordTags ADD COLUMN color_int TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}