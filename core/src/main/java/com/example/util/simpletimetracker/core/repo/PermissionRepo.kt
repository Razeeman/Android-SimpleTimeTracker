package com.example.util.simpletimetracker.core.repo

import android.app.AlarmManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.example.util.simpletimetracker.domain.extension.orFalse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PermissionRepo @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
) {

    private val alarmManager get() = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms().orFalse()
        } else {
            true
        }
    }

    fun releasePersistableUriPermissions(excludeUri: String) = runCatching {
        // There is a limit of how many persisted permissions could be taken (512 on android 11, 128 previously).
        // Release all not needed just in case.
        contentResolver.persistedUriPermissions.forEach {
            if (it.uri.toString() == excludeUri) return@forEach
            contentResolver.releasePersistableUriPermission(
                it.uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
    }

    fun takePersistableUriPermission(uri: String): Boolean = runCatching {
        // Take persisted permission to be able to use this uri later, for automatic backups.
        contentResolver.takePersistableUriPermission(
            Uri.parse(uri),
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }.fold(
        onSuccess = { true },
        onFailure = { false },
    )
}