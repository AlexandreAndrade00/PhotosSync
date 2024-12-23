package alexandrade.photos_sync.database

import alexandrade.photos_sync.database.daos.ImageDao
import alexandrade.photos_sync.database.daos.SyncDao
import alexandrade.photos_sync.database.entities.Image
import alexandrade.photos_sync.database.entities.Remote
import alexandrade.photos_sync.database.entities.SyncHistory
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Image::class, SyncHistory::class, Remote::class], version = 1)
abstract class SqliteDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun syncDao(): SyncDao

    companion object {
        private const val DATABASE_NAME = "my_database"

        @Volatile
        private var INSTANCE: SqliteDatabase? = null

        fun getDatabase(context: Context): SqliteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SqliteDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}