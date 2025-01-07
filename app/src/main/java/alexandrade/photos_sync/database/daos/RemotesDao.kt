package alexandrade.photos_sync.database.daos

import alexandrade.photos_sync.database.entities.Remote
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RemotesDao {
    @Query("SELECT * FROM remotes")
    fun getRemotesFlow(): Flow<List<Remote>>

    @Query("SELECT * FROM remotes")
    suspend fun getRemotes(): List<Remote>

    @Query("SELECT EXISTS(SELECT 1 FROM remotes)")
    suspend fun remoteExists(): Boolean

    @Query("SELECT * FROM remotes WHERE principal = TRUE")
    fun getPrincipalRemote(): Remote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addRemote(remote: Remote)
}