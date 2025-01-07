package alexandrade.photos_sync.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import alexandrade.photos_sync.R
import alexandrade.photos_sync.cloud_providers.BackblazeB2
import alexandrade.photos_sync.cloud_providers.CloudProvider

enum class CloudProviders(
    val logoId: Int,
    val displayName: String,
    val getStrategy: (Remote) -> CloudProvider
) {
    BACKBLAZE(
        logoId = R.mipmap.ic_backblaze_logo,
        displayName = "BackBlaze",
        getStrategy = { remote -> BackblazeB2(remote) }
    )
}

@Entity(tableName = "remotes")
data class Remote(
    @PrimaryKey val name: String,
    val provider: CloudProviders,
    val apiKeyId: String,
    val apiKey: String,
    val bucketId: String,
    val principal: Boolean
) {
    fun getStrategy(): CloudProvider {
        return provider.getStrategy(this)
    }
}
