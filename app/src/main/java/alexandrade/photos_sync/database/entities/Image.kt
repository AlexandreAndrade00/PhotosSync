package alexandrade.photos_sync.database.entities

import alexandrade.photos_sync.database.type_converters.UriTypeConverter
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.UUID

enum class SyncStatus {
    LOCAL, REMOTE, BOTH
}

@Entity(tableName = "images", indices = [Index(value = ["local_path"], unique = true)])
@TypeConverters(UriTypeConverter::class)
data class Image(
    @PrimaryKey val uuid: UUID,
    val status: SyncStatus,
    @ColumnInfo(name = "local_path") val localPath: Uri?,
    val name: String,
)
