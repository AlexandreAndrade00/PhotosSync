package alexandrade.photos_sync.database.daos

import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.database.entities.SyncStatus
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImages(images: List<Image>)

    @Query("UPDATE images SET status = :status WHERE uuid = :imageId")
    fun updateImageStatus(imageId: UUID, status: SyncStatus)

    @Query("SELECT * FROM images")
    fun getImagesFlow(): Flow<List<Image>>

    @Query("SELECT * FROM images WHERE status = :status")
    fun getImagesByStatus(status: SyncStatus): List<Image>
}
