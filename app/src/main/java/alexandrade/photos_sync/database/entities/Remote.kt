package alexandrade.photos_sync.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import alexandrade.photos_sync.R
import alexandrade.photos_sync.utils.BackblazeConfigRoute
import kotlin.reflect.KClass

enum class CloudProviders(
    val logoId: Int,
    val displayName: String,
    val route: KClass<*>
) {
    BACKBLAZE(
        logoId = R.mipmap.backblaze_logo,
        displayName = "BackBlaze",
        route = BackblazeConfigRoute::class
    )
}

@Entity(tableName = "remotes")
data class Remote(
    @PrimaryKey val name: String,
    val provider: CloudProviders,
    val apiKeyId: String,
    val apiKey: String,
    val bucketId: String
)
