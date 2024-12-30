package alexandrade.photos_sync.repository

import alexandrade.photos_sync.database.SqliteDatabase
import alexandrade.photos_sync.database.entities.Remote
import android.content.Context
import kotlinx.coroutines.flow.Flow

class RemotesRepository(val context: Context) {
    private val database = SqliteDatabase.getDatabase(context)

    fun getRemotes(): Flow<List<Remote>> {
        return database.remotesDao().getRemotes()
    }
}