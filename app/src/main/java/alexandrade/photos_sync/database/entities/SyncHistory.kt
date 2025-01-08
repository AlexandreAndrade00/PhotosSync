package alexandrade.photos_sync.database.entities

import alexandrade.photos_sync.database.type_converters.DateConverter
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

enum class SyncType { LOCAL, REMOTE }

@Entity(tableName = "sync_history")
@TypeConverters(DateConverter::class)
data class SyncHistory(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val date: Date,
    val syncType: SyncType
)
