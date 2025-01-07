package alexandrade.photos_sync.repository

import alexandrade.photos_sync.database.SqliteDatabase
import alexandrade.photos_sync.database.entities.Remote
import android.content.Context
import kotlinx.coroutines.flow.Flow

class RemotesRepository(val context: Context) {
    private val database = SqliteDatabase.getDatabase(context)

    fun getRemotesFlow(): Flow<List<Remote>> {
        return database.remotesDao().getRemotesFlow()
    }

    suspend fun getRemotes(): List<Remote> {
        return database.remotesDao().getRemotes()
    }

    suspend fun addRemote(remote: Remote) {
        val remoteExists = database.remotesDao().remoteExists()

        if (!remoteExists) {
            val principalRemote = Remote(
                remote.name,
                remote.provider,
                remote.apiKeyId,
                remote.apiKey,
                remote.bucketId,
                true
            )

            database.remotesDao().addRemote(principalRemote)
        } else {
            database.remotesDao().addRemote(remote)
        }
    }

    fun getPrincipalRemote(): Remote? {
        return database.remotesDao().getPrincipalRemote()
    }
}