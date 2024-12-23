package alexandrade.photos_sync.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.util.Date


fun getImagesFromMediaStore(context: Context, date: Date?): List<Pair<Uri, String>> {
    val imageMeta = mutableListOf<Pair<Uri, String>>()
    val contentResolver: ContentResolver = context.contentResolver
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)

    val selection: String?
    val selectionArgs: Array<String>?

    if (date != null) {
        // Format the date and time for the selection clause (seconds since epoch)
        val dateTimeInSeconds = date.time / 1000 // Convert milliseconds to seconds

        // Selection clause to filter images by date and time added
        selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        selectionArgs = arrayOf(dateTimeInSeconds.toString())

    } else {
        selection = null
        selectionArgs = null
    }

    val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC" // Sort by date added

    try {
        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                val imageUri = Uri.withAppendedPath(uri, id.toString())

                imageMeta.add(Pair(imageUri, name))
            }
        }
    } catch (e: Exception) {
        // Handle exceptions appropriately (e.g., log the error)
        e.printStackTrace()
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
    try {
        contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val dateAddedInSeconds =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                date = Date(dateAddedInSeconds * 1000L) // Convert seconds to milliseconds
            }
        }
    } catch (e: Exception) {
        e.printStackTrace() // Handle exceptions appropriately
    }

    return date
}