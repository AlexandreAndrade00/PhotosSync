package alexandrade.photos_sync.database.daos

import alexandrade.photos_sync.database.entities.Remote
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RemotesDao {
    @Query("SELECT * FROM remotes")
    fun getRemotes(): Flow<List<Remote>>
}