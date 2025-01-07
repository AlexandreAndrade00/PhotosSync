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

    suspend fun addImage(image: Image) {
        database.imageDao().insertImages(listOf(image))
    }

    fun updateImageStatus(imageId: UUID, status: SyncStatus) {
        database.imageDao().updateImageStatus(imageId, status)
    }

    fun getImagesFlow(): Flow<List<Image>> {
        return database.imageDao().getImagesFlow()
    }

    fun getImagesByStatus(status: SyncStatus): List<Image> {
        return database.imageDao().getImagesByStatus(status)
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
                    value.uri,
                    value.name,
                    value.contentType
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