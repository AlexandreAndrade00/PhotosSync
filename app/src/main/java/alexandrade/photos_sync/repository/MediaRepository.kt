package alexandrade.photos_sync.repository

import alexandrade.photos_sync.database.SqliteDatabase
import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.database.entities.SyncHistory
import alexandrade.photos_sync.database.entities.SyncStatus
import alexandrade.photos_sync.utils.getImagesFromMediaStore
import alexandrade.photos_sync.utils.getMostRecentImageDate
import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

class MediaRepository(val context: Context) {
    private val database = SqliteDatabase.getDatabase(context)

    fun getImages(): Flow<List<Image>> {
        return database.imageDao().getImages()
    }


    suspend fun syncExtenalStorage() {
        val lastSync: Date? = database.syncDao().getLastSync()?.date
        val recentImageDate: Date? = getMostRecentImageDate(context)

        if (lastSync?.after(recentImageDate) == true || lastSync?.equals(recentImageDate) == true) return

        val imagesUris = getImagesFromMediaStore(context, lastSync)

        if (imagesUris.isEmpty()) return

        val images =
            imagesUris.map { value ->
                Image(
                    UUID.randomUUID(),
                    SyncStatus.LOCAL,
                    value.first,
                    value.second
                )
            }

        database.imageDao().insertImages(images)
        database.syncDao().insertSyncHistory(
            SyncHistory(
                date = recentImageDate!!
            )
        )
    }
}