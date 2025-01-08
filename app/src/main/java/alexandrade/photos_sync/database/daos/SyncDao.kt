package alexandrade.photos_sync.database.daos

import alexandrade.photos_sync.database.entities.SyncHistory
import alexandrade.photos_sync.database.entities.SyncType
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_history WHERE syncType = :syncType;")
    suspend fun getSyncHistory(syncType: SyncType) : List<SyncHistory>

    @Query("SELECT * FROM sync_history WHERE syncType = :syncType;")
    fun getSyncHistoryFlow(syncType: SyncType) : Flow<List<SyncHistory>>

    @Insert
    suspend fun insertSyncHistory(syncHistory: SyncHistory)
}