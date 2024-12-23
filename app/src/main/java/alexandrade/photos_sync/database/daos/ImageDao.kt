package alexandrade.photos_sync.database.daos

import alexandrade.photos_sync.database.entities.Image
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImages(images: List<Image>)

    @Query("SELECT * FROM images")
    fun getImages(): Flow<List<Image>>
}
