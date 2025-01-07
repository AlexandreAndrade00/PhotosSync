package alexandrade.photos_sync.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

data class MediaStoreImage(val uri: Uri, val name: String, val contentType: String)

fun getImagesFromMediaStore(context: Context, date: Date?): List<MediaStoreImage> {
    val imageMeta = mutableListOf<MediaStoreImage>()
    val contentResolver: ContentResolver = context.contentResolver
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)

    val selection: String?
    val selectionArgs: Array<String>?

    if (date != null) {
        val dateTimeInSeconds = date.time / 1000
        selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        selectionArgs = arrayOf(dateTimeInSeconds.toString())
    } else {
        selection = null
        selectionArgs = null
    }

    val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"

    try {
        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                val imageUri = ContentUris.withAppendedId(uri, id)
                val contentType = context.contentResolver.getType(imageUri)

                imageMeta.add(MediaStoreImage(imageUri, name, contentType!!))
            }
        }
    } catch (e: Exception) {
        Log.e("MediaStore", "Error querying MediaStore: ${e.message}", e)
    }

    return imageMeta
}

fun getMostRecentImageDate(context: Context): Date? {
    val contentResolver: ContentResolver = context.contentResolver
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.Media.DATE_ADDED)
    val selection = null
    val selectionArgs = null
    val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC" // Descending order

    var date: Date? = null

    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val dateAddedInSeconds =
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
            date = Date(dateAddedInSeconds * 1000L) // Convert seconds to milliseconds
        }
    }


    return date
}

suspend fun saveImageToMediaStore(
    context: Context,
    imageBytes: ByteArray,
    displayName: String,
    mimeType: String
): Uri? = withContext(Dispatchers.IO) {
    val contentResolver = context.contentResolver

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        put(MediaStore.Images.Media.RELATIVE_PATH, "PhotosSync")
    }

    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

    uri?.let {
        contentResolver.openOutputStream(it)?.use { outputStream ->
            outputStream.write(imageBytes)
        }
    }

    return@withContext uri;
}