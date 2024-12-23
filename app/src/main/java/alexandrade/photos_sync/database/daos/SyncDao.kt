package alexandrade.photos_sync.database.daos

import alexandrade.photos_sync.database.entities.SyncHistory
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_history ORDER BY id DESC LIMIT 1;")
    suspend fun getLastSync() : SyncHistory?

    @Insert
    suspend fun insertSyncHistory(syncHistory: SyncHistory)
}