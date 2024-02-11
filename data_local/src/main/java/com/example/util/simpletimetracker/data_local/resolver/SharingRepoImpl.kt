package com.example.util.simpletimetracker.data_local.resolver

import android.content.Context
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.domain.resolver.SharingRepo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class SharingRepoImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val applicationDataProvider: ApplicationDataProvider,
) : SharingRepo {

    override suspend fun saveBitmap(
        bitmap: Any,
        filename: String,
    ): SharingRepo.Result = withContext(Dispatchers.IO) {
        if (bitmap !is Bitmap) return@withContext SharingRepo.Result.Error

        var outputStream: BufferedOutputStream? = null
        try {
            val file = File(context.cacheDir, "$filename.png")
            outputStream = FileOutputStream(file).buffered()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val uri = FileProvider.getUriForFile(
                context,
                applicationDataProvider.getPackageName() + ".provider",
                file,
            )
            SharingRepo.Result.Success(uri.toString())
        } catch (e: Exception) {
            Timber.e(e)
            SharingRepo.Result.Error
        } finally {
            try {
                outputStream?.flush()
                outputStream?.close()
            } catch (e: IOException) {
                // Do nothing
            }
        }
    }
}