package alexandrade.photos_sync.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CloudProviders { BACKBLAZE }

@Entity(tableName = "remotes")
data class Remote(
    @PrimaryKey val name: String,
    val provider: CloudProviders,
    val apiKeyId: String,
    val apiKey: String,
    val bucketId: String
)
